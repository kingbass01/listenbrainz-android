package org.listenbrainz.android.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventDeletionData
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.FeedEventType.Companion.isActionDelete
import org.listenbrainz.android.model.FeedEventVisibilityData
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.ui.screens.feed.FeedUiEventData
import org.listenbrainz.android.ui.screens.feed.FeedUiEventItem
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.MyFeedPagingSource
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val socialRepository: SocialRepository,
    private val appPreferences: AppPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
): ViewModel() {
    
    private val loadingStateFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<ResponseError?>(null)
    private val isHiddenMap: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    private val isDeletedMap: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    
    private val myFeedPager: Flow<PagingData<FeedUiEventItem>> = Pager(
        PagingConfig(pageSize = 30),
        initialKey = null
    ) { createNewMyFeedPagingSource() }
        .flow
        .cachedIn(viewModelScope)
    
    private val myFeedFlow = MutableStateFlow(FeedUiEventData(isHiddenMap, isDeletedMap, myFeedPager))
    
    val uiState = createUiStateFlow()
    
    private fun createUiStateFlow(): StateFlow<FeedUiState> {
        return combine(
            myFeedFlow,
            loadingStateFlow,
            errorFlow
        ){ feedData, isLoading, error ->
            FeedUiState(feedData, isLoading, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            FeedUiState()
        )
    }
    
    private fun createNewMyFeedPagingSource(): MyFeedPagingSource =
        MyFeedPagingSource(
            username = appPreferences.username ?: "",
            addEntryToMap = { id, value ->
                isHiddenMap[id] = value
            },
            onError =  { error ->
                viewModelScope.launch(defaultDispatcher) {
                    errorFlow.emit(error)
                }
            },
            feedRepository = feedRepository,
            ioDispatcher = ioDispatcher
        )
    
    fun hideOrDeleteEvent(event: FeedEvent, eventType: FeedEventType, parentUser: String) {
        
        viewModelScope.launch(defaultDispatcher) {
            if (isActionDelete(event, eventType, parentUser)){
                deleteEvent(event.id!!, event.type)
            } else if (isHiddenMap[event.id] == true){
                println("UNHIDE")
                unhideEvent(data = FeedEventVisibilityData(event.type, event.id.toString()))
            } else {
                println("HIDE")
                // null means false
                hideEvent(data = FeedEventVisibilityData(event.type, event.id.toString()))
            }
            
        }
    }
    
    private suspend fun deleteEvent(id: Int, type: String) {
    
        val username = appPreferences.username ?: return
    
        // Optimistically inverting state
        isDeletedMap[id] = true
    
        val result = withContext(ioDispatcher) {
            if (type == FeedEventType.RECORDING_PIN.type) {
                socialRepository.deletePin(id)
            } else {
                feedRepository.deleteEvent(username, FeedEventDeletionData(eventId = id.toString(), eventType = type))
            }
        }
    
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                isDeletedMap[id] = false
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    
    private suspend fun hideEvent(data: FeedEventVisibilityData) {
        
        val username = appPreferences.username ?: return
        
        // Optimistically toggle
        toggleHiddenStatus(data)
        
        val result = withContext(ioDispatcher) {
            feedRepository.hideEvent(username, data)
        }
        
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                toggleHiddenStatus(data)
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    private suspend fun unhideEvent(data: FeedEventVisibilityData) {
            
        val username = appPreferences.username ?: return
        
        // Optimistically toggle
        toggleHiddenStatus(data)
        
        val result = withContext(ioDispatcher) {
            feedRepository.unhideEvent(username, data)
        }
        
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                toggleHiddenStatus(data)
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    private suspend fun toggleHiddenStatus(data: FeedEventVisibilityData) {
        try {
            val currentState = isHiddenMap[data.eventId!!.toInt()]
            isHiddenMap[data.eventId!!.toInt()] = currentState == null || currentState == false
        } catch (e: Exception) {
            errorFlow.emit(ResponseError.BAD_REQUEST.apply { actualResponse = "Internal Error occurred." })
            e.printStackTrace()
        }
    }
    
    fun clearError() {
        viewModelScope.launch(defaultDispatcher) {
            errorFlow.emit(null)
        }
    }
    
}