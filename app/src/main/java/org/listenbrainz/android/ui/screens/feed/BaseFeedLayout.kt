package org.listenbrainz.android.ui.screens.feed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.FeedEventType.Companion.getTimeStringForFeed
import org.listenbrainz.android.model.FeedEventType.Companion.isActionDelete
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BaseFeedLayout(
    modifier: Modifier = Modifier,
    eventType: FeedEventType,
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    isHidden: Boolean = event.hidden == true,
    content: @Composable () -> Unit
) {
    
    // Content that is to be measured for horizontal line.
    @Composable
    fun MainContent() {
        // Since width of the event icon is 19 dp, 19/2 = 9.5 dp
        Column(Modifier.padding(start = ListenBrainzTheme.paddings.insideCard + 9.5.dp)) {
            AnimatedVisibility(
                visible = !isHidden,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                content()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Date(
                event = event,
                eventType = eventType,
                parentUser = parentUser,
                onActionClick = onDeleteOrHide
            )
        }
    }
    
    // Actual content
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ListenBrainzTheme.paddings.horizontal,
                top = ListenBrainzTheme.paddings.vertical,
                end = ListenBrainzTheme.paddings.horizontal
            ),
    ) {
    
        // Icon and Tagline
        Row {
        
            EventIcon(eventType)
    
            Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.insideCard))
            
            AnimatedContent(
                targetState = isHidden,
                label = "tagline"
            ) {
                if (!it){
                    eventType.Tagline(
                        event = event,
                        parentUser = parentUser
                    )
                } else {
                    Text(
                        text = "This event is hidden.",
                        color = ListenBrainzTheme.colorScheme.text,
                        fontWeight = FontWeight.Light
                    )
                }
                
            }
            
        }
        
        Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideCard))
        
        // Horizontal line and Main Content
        Row {
            
            DynamicHorizontalLine {
                MainContent()
            }
            
        }
    
    }
    
}

@Composable
private fun Date(
    event: FeedEvent,
    parentUser: String,
    eventType: FeedEventType,
    height: Dp = 24.dp,
    onActionClick: () -> Unit
) {
    
    val timeString = remember(event.created) {
        getTimeStringForFeed(event.created.toLong())
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.TopEnd
    ) {
    
        Row(verticalAlignment = Alignment.CenterVertically) {
            
            // Date
            Text(
                text = timeString,
                color = ListenBrainzTheme.colorScheme.hint,
                fontSize = 12.sp
            )
    
            // Delete or hide button
            if (eventType.isDeletable || eventType.isHideable){
                Icon(
                    modifier = Modifier
                        .size(height)
                        .padding(start = 4.dp)
                        .clickable {
                            onActionClick()
                        },
                    // TODO: USE CUSTOM ICONS HERE.
                    imageVector = if (isActionDelete(event, eventType, parentUser))
                        Icons.Rounded.Delete
                    else
                        Icons.Rounded.HideSource,
                    tint = ListenBrainzTheme.colorScheme.lbSignature,
                    contentDescription = if (isActionDelete(event, eventType, parentUser)) "Delete this event." else "Hide this event."
                )
            }
        }
    }
    
}


/** Line that automatically adjusts to match its height according to the content passed.
 * @param Content Content to match height of. */
@Composable
private fun DynamicHorizontalLine(Content: @Composable () -> Unit) {
    
    SubcomposeLayout(modifier = Modifier.padding(start = (9.5).dp)) { constraints: Constraints ->
        
        val mainPlaceables: List<Placeable> = subcompose("content", Content)
            .map {
                it.measure(constraints)
            }
        
        var height = 0
        var width = 0
        
        // Get height of card and any other composables.
        mainPlaceables.forEach {
            height += it.height
            width += it.width
        }
        
        val dependentPlaceables: List<Placeable> = subcompose("line") {
            HorizontalLine(height = height.toDp())
        }
            .map { measurable: Measurable ->
                measurable.measure(constraints)
            }
        
        dependentPlaceables.forEach {
            width += it.width
        }
        
        layout(height = height, width = width /* width of line */) {
            
            dependentPlaceables.forEach { placeable: Placeable ->
                /** Our icon is 19 dp and line width is 2 dp.*/
                placeable.placeRelative(0, 0)
            }
            
            mainPlaceables.forEach { placeable: Placeable ->
                placeable.placeRelative(0, 0)
            }
            
        }
    }
}


@Composable
private fun EventIcon(eventType: FeedEventType) {
    Image(
        modifier = Modifier.size(19.dp),
        painter = painterResource(id = eventType.icon),
        contentDescription = eventType.name
    )
}


@Composable
private fun HorizontalLine(
    height: Dp,
    color: Color = ListenBrainzTheme.colorScheme.hint
) {
    Canvas(
        modifier = Modifier.height(height)
    ) {
        drawLine(
            color = color,
            start = Offset.Zero,
            end = Offset(0f, this.size.height),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}


@Preview
@Composable
private fun BaseFeedCardPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            val event = FeedEventType.RECORDING_PIN
            BaseFeedLayout(
                eventType = event,
                parentUser = "Jasjeet",
                event = FeedEvent(
                    id = 0,
                    created = System.currentTimeMillis().toInt(),
                    type = "like",
                    hidden = false, metadata = Metadata(user1 = "JasjeetTest"),
                    username = "Jasjeet"
                ),
                onDeleteOrHide = {}
            ) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp),
                        text = "Content"
                    )
                }
            }
        }
    }
}