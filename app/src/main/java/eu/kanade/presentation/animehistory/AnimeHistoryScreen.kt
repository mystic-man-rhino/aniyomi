package eu.kanade.presentation.animehistory

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import eu.kanade.domain.animehistory.model.AnimeHistoryWithRelations
import eu.kanade.presentation.animehistory.components.AnimeHistoryContent
import eu.kanade.presentation.animehistory.components.AnimeHistoryToolbar
import eu.kanade.presentation.components.EmptyScreen
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.components.Scaffold
import eu.kanade.presentation.history.components.HistoryDeleteAllDialog
import eu.kanade.presentation.history.components.HistoryDeleteDialog
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.recent.HistoryTabsController.Companion.isCurrentHistoryTabManga
import eu.kanade.tachiyomi.ui.recent.animehistory.AnimeHistoryPresenter
import eu.kanade.tachiyomi.ui.recent.animehistory.AnimeHistoryPresenter.Dialog
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.widget.TachiyomiBottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import uy.kohesive.injekt.api.get
import java.util.Date

@Composable
fun AnimeHistoryScreen(
    presenter: AnimeHistoryPresenter,
    onClickCover: (AnimeHistoryWithRelations) -> Unit,
    onClickResume: (AnimeHistoryWithRelations) -> Unit,
) {
    val context = LocalContext.current

    isCurrentHistoryTabManga = false

    Scaffold(
        topBar = { scrollBehavior ->
            AnimeHistoryToolbar(
                state = presenter,
                incognitoMode = presenter.isIncognitoMode,
                downloadedOnlyMode = presenter.isDownloadOnly,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        val items by presenter.getAnimeHistory().collectAsState(initial = null)
        val contentPaddingWithNavBar =
            TachiyomiBottomNavigationView.withBottomNavPadding(contentPadding)
        items.let {
            if (it == null) {
                LoadingScreen()
            } else if (it.isEmpty()) {
                EmptyScreen(
                    textResource = R.string.information_no_recent_manga,
                    modifier = Modifier.padding(contentPaddingWithNavBar),
                )
            } else {
                AnimeHistoryContent(
                    history = it,
                    contentPadding = contentPaddingWithNavBar,
                    onClickCover = onClickCover,
                    onClickResume = onClickResume,
                    onClickDelete = { item -> presenter.dialog = Dialog.Delete(item) },
                )
            }
        }
        LaunchedEffect(items) {
            if (items != null) {
                (presenter.context as? MainActivity)?.ready = true
            }
        }
    }

    val onDismissRequest = { presenter.dialog = null }
    when (val dialog = presenter.dialog) {
        is Dialog.Delete -> {
            HistoryDeleteDialog(
                onDismissRequest = onDismissRequest,
                onDelete = { all ->
                    if (all) {
                        presenter.removeAllFromHistory(dialog.history.animeId)
                    } else {
                        presenter.removeFromHistory(dialog.history)
                    }
                },
            )
        }
        is Dialog.DeleteAll -> {
            HistoryDeleteAllDialog(
                onDismissRequest = onDismissRequest,
                onDelete = {
                    presenter.deleteAllAnimeHistory()
                },
            )
        }
        null -> {}
    }
    LaunchedEffect(Unit) {
        presenter.events.collectLatest { event ->
            when (event) {
                AnimeHistoryPresenter.Event.InternalError -> context.toast(R.string.internal_error)
                AnimeHistoryPresenter.Event.NoNextEpisodeFound -> context.toast(R.string.no_next_episode)
                is AnimeHistoryPresenter.Event.OpenEpisode -> {
                    presenter.openEpisode(event.episode)
                }
            }
        }
    }
}

sealed class AnimeHistoryUiModel {
    data class Header(val date: Date) : AnimeHistoryUiModel()
    data class Item(val item: AnimeHistoryWithRelations) : AnimeHistoryUiModel()
}