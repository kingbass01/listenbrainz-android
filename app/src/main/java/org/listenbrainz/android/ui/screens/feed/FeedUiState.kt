package org.listenbrainz.android.ui.screens.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType


/** Top most state wrapper for Feed Screen.*/
data class FeedUiState(
    val myFeedState: FeedScreenUiState = FeedScreenUiState(),
    val followListensFeedState: FeedScreenUiState = FeedScreenUiState(),
    val similarListensFeedState: FeedScreenUiState = FeedScreenUiState(),
    val searchResult: List<String> = emptyList(),
    val error: ResponseError? = null
)

/** Represents states of various screens in feed.*/
data class FeedScreenUiState(
    val data: FeedUiEventData = FeedUiEventData(),
    val isLoading: Boolean = true,
)

/** Data held by each screen.*/
data class FeedUiEventData(
    val isHiddenMap: Map<Int, Boolean> = emptyMap(),
    val isDeletedMap: Map<Int, Boolean> = emptyMap(),
    var eventList: Flow<PagingData<FeedUiEventItem>> = emptyFlow()
)

/** UI representation for one feed event.*/
data class FeedUiEventItem(
    val eventType: FeedEventType,
    val event: FeedEvent,
    val parentUser: String = ""
)