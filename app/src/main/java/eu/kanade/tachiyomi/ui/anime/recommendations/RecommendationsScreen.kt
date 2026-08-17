package eu.kanade.tachiyomi.ui.anime.recommendations

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.anime.model.toSAnime
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import tachiyomi.domain.anime.interactor.GetAnimeWithEpisodes
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.track.interactor.GetTracks
import tachiyomi.domain.track.model.Track
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

data class RecommendationsScreen(private val animeId: Long) : Screen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val getAnimeWithEpisodes = remember { Injekt.get<GetAnimeWithEpisodes>() }
        val sourceManager = remember { Injekt.get<SourceManager>() }
        val getTracks = remember { Injekt.get<GetTracks>() }
        val trackerManager = remember { Injekt.get<TrackerManager>() }
        var anime by remember { mutableStateOf<tachiyomi.domain.anime.model.Anime?>(null) }
        var sourceSuggestions by remember {
            mutableStateOf < List<Pair<String, List<eu.kanade.tachiyomi.animesource.model.SAnime>>>(emptyList())
        }
        var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

        LaunchedEffect(animeId) {
            anime = getAnimeWithEpisodes.awaitManga(animeId)
            tracks = getTracks.await(animeId)
            val current = anime ?: return@LaunchedEffect
            val source = sourceManager.get(current.source) ?: return@LaunchedEffect
            if (!current.isLocal()) {
                source.getRelatedAnimeList(
                    current.toSAnime(),
                    exceptionHandler = {},
                    pushResults = { result, _ ->
                        sourceSuggestions = sourceSuggestions + result
                    },
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Recommendations") },
                    navigationIcon = {
                        TextButton(onClick = { navigator.pop() }) { Text("Back") }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding),
            ) {
                item {
                    Text(
                        text = "Community recommendations",
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                items(
                    tracks.mapNotNull { track ->
                        val tracker = trackerManager.get(track.trackerId) ?: return@mapNotNull null
                        val url = when (tracker.name.lowercase()) {
                            "anilist" -> "https://anilist.co/anime/${track.remoteId}/recommendations"
                            "myanimelist" -> "https://myanimelist.net/anime/${track.remoteId}/recommendation"
                            else -> null
                        } ?: return@mapNotNull null
                        tracker.name to url
                    },
                    key = { it.second },
                ) { (name, url) ->
                    Card(
                        onClick = { uriHandler.openUri(url) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ListItem(
                            headlineContent = { Text("$name community recommendations") },
                            supportingContent = { Text(Uri.parse(url).host.orEmpty()) },
                        )
                    }
                }
                item {
                    Text(
                        text = "Source suggestions",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                }
                sourceSuggestions.forEach { (label, suggestions) ->
                    item(key = "header-$label") {
                        Text(label, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(suggestions, key = { "$label-${it.url}" }) { suggestion ->
                        Card(
                            onClick = {
                                anime?.let {
                                    navigator.push(BrowseSourceScreen(it.source, suggestion.title))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ListItem(
                                headlineContent = { Text(suggestion.title) },
                                supportingContent = { Text(suggestion.url) },
                            )
                        }
                    }
                }
            }
        }
    }
}
