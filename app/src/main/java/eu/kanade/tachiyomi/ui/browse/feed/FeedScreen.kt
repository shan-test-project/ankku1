package eu.kanade.tachiyomi.ui.browse.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.domain.source.interactor.GetFeedSavedSearchGlobal
import tachiyomi.domain.source.interactor.GetRemoteAnime
import tachiyomi.domain.source.interactor.GetSavedSearchGlobalFeed
import tachiyomi.domain.source.model.SavedSearch
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun FeedScreen(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onOpenFeed: (sourceId: Long, query: String?) -> Unit,
) {
    val sourceManager = remember { Injekt.get<SourceManager>() }
    val getSavedSearchGlobalFeed = remember { Injekt.get<GetSavedSearchGlobalFeed>() }
    val getFeedSavedSearchGlobal = remember { Injekt.get<GetFeedSavedSearchGlobal>() }
    var savedSearches by remember { mutableStateOf<List<SavedSearch>>(emptyList()) }
    val sources = remember { sourceManager.getVisibleCatalogueSources() }

    LaunchedEffect(Unit) {
        savedSearches = getSavedSearchGlobalFeed.await()
        getFeedSavedSearchGlobal.await()
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 12.dp),
    ) {
        item {
            Text(
                text = "Latest from your sources",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        items(sources, key = { "source-${it.id}" }) { source ->
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(source.name) },
                    supportingContent = { Text(source.lang) },
                    leadingContent = {
                        Icon(Icons.Outlined.NewReleases, contentDescription = null)
                    },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = {
                                onOpenFeed(source.id, GetRemoteAnime.QUERY_LATEST)
                            }) {
                                Text("Latest")
                            }
                            Button(onClick = {
                                onOpenFeed(source.id, GetRemoteAnime.QUERY_POPULAR)
                            }) {
                                Text("Popular")
                            }
                        }
                    },
                )
            }
        }
        item {
            Text(
                text = "Saved searches",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        if (savedSearches.isEmpty()) {
            item {
                Text(
                    text = "Hold a saved search in a source to add it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        } else {
            items(savedSearches, key = { "search-${it.id}" }) { search ->
                Card(
                    onClick = { onOpenFeed(search.source, search.query) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(
                        headlineContent = { Text(search.name) },
                        supportingContent = { Text(search.query.orEmpty()) },
                        leadingContent = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}
