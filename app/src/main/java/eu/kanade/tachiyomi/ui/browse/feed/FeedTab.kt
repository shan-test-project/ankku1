package eu.kanade.tachiyomi.ui.browse.feed

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.TabContent
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import tachiyomi.i18n.kmk.KMR

@Composable
fun Screen.feedTab(): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    return TabContent(
        titleRes = KMR.strings.saved_searches_feeds,
        content = { contentPadding, snackbarHostState ->
            FeedScreen(
                contentPadding = contentPadding,
                snackbarHostState = snackbarHostState,
                onOpenFeed = { sourceId, query ->
                    navigator.push(BrowseSourceScreen(sourceId, query))
                },
            )
        },
    )
}
