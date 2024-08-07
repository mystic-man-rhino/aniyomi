package mihon.feature.upcoming.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.entries.anime.AnimeScreen

class UpcomingAnimeScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = rememberScreenModel { UpcomingAnimeScreenModel() }
        val state by screenModel.state.collectAsState()

        UpcomingAnimeScreenContent(
            state = state,
            setSelectedYearMonth = screenModel::setSelectedYearMonth,
            onClickUpcoming = { navigator.push(AnimeScreen(it.id)) },
        )
    }
}
