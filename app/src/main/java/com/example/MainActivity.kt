package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ExamSection
import com.example.ui.MainViewModel
import com.example.ui.screens.CreatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SectionPapersScreen
import com.example.ui.theme.CmaPrepTheme
import kotlinx.coroutines.launch

private enum class TopTab(
  val label: String,
  val icon: ImageVector,
  val tag: String,
) {
  PRACTICE("Practice", Icons.Rounded.Quiz, "nav_tab_practice"),
  REVIEW("Review", Icons.Rounded.Analytics, "nav_tab_review"),
  CREATOR("Creator", Icons.Rounded.Person, "nav_tab_creator"),
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CmaPrepApp()
    }
  }
}

@Composable
fun CmaPrepApp(viewModel: MainViewModel = viewModel()) {
  val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
  val paperSummaries by viewModel.paperSummaries.collectAsStateWithLifecycle()
  val quizSession by viewModel.quizSession.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val emptyMessage by viewModel.quizEmptyMessage.collectAsStateWithLifecycle()

  var currentTab by rememberSaveable { mutableStateOf(TopTab.PRACTICE) }
  var openedSectionKey by rememberSaveable { mutableStateOf<String?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(emptyMessage) {
    emptyMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearQuizEmptyMessage()
    }
  }

  CmaPrepTheme(darkTheme = homeState.settings.darkModeEnabled) {
    val activeQuiz = quizSession
    if (activeQuiz != null) {
      QuizScreen(
        session = activeQuiz,
        soundEnabled = homeState.settings.soundEnabled,
        hapticEnabled = homeState.settings.hapticEnabled,
        onSelectOption = viewModel::submitOption,
        onNextQuestion = viewModel::nextQuestion,
        onToggleBookmark = viewModel::toggleBookmark,
        onToggleSound = viewModel::toggleSound,
        onCloseQuiz = viewModel::closeQuiz,
        onContinueMore = viewModel::continueMoreQuestions,
      )
      return@CmaPrepTheme
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavRail = maxWidth >= 600.dp

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (!useNavRail && openedSectionKey == null) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
            ) {
              TopTab.entries.forEach { tab ->
                NavigationBarItem(
                  selected = currentTab == tab,
                  onClick = {
                    currentTab = tab
                    openedSectionKey = null
                  },
                  icon = { Icon(tab.icon, contentDescription = tab.label) },
                  label = { Text(tab.label) },
                  modifier = Modifier.testTag(tab.tag),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (useNavRail && openedSectionKey == null) {
            NavigationRail(
              containerColor = MaterialTheme.colorScheme.surface,
            ) {
              TopTab.entries.forEach { tab ->
                NavigationRailItem(
                  selected = currentTab == tab,
                  onClick = {
                    currentTab = tab
                    openedSectionKey = null
                  },
                  icon = { Icon(tab.icon, contentDescription = tab.label) },
                  label = { Text(tab.label) },
                  modifier = Modifier.testTag("${tab.tag}_rail"),
                )
              }
            }
          }

          Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            val openSection = openedSectionKey?.let { ExamSection.fromKey(it) }
            LaunchedEffect(openSection) {
              if (openSection != null) {
                viewModel.selectSection(openSection)
              }
            }
            if (currentTab == TopTab.PRACTICE && openSection != null) {
              SectionPapersScreen(
                section = openSection,
                papers = paperSummaries,
                onBack = { openedSectionKey = null },
                onStartMixedQuiz = { limit -> viewModel.startSectionQuiz(openSection, limit) },
                onStartPaperQuiz = { paper, limit ->
                  viewModel.startPaperQuiz(openSection, paper.paperCode, paper.paperName, limit)
                },
                onResetPaper = { code -> viewModel.resetPaperProgress(openSection, code) },
                onResetSection = { viewModel.resetSectionProgress(openSection) },
              )
            } else {
              when (currentTab) {
                TopTab.PRACTICE -> HomeScreen(
                  state = homeState,
                  onOpenSection = { section ->
                    viewModel.selectSection(section)
                    openedSectionKey = section.key
                  },
                  onQuickStartSection = { section ->
                    val summary = homeState.sectionSummaries[section]
                    if ((summary?.remainingCount ?: 0) > 0) {
                      viewModel.startSectionQuiz(section, 25)
                    } else {
                      viewModel.selectSection(section)
                      openedSectionKey = section.key
                    }
                  },
                  onStartMistakesDrill = { viewModel.startMistakesQuiz(25) },
                  onStartBookmarksDrill = { viewModel.startBookmarksQuiz(25) },
                  onToggleSound = viewModel::toggleSound,
                  onToggleDarkMode = viewModel::toggleDarkMode,
                )

                TopTab.REVIEW -> ReviewScreen(
                  state = homeState,
                  searchQuery = searchQuery,
                  searchResults = searchResults,
                  onSearchQueryChange = viewModel::updateSearchQuery,
                  onStartMistakesDrill = { viewModel.startMistakesQuiz(25) },
                  onStartBookmarksDrill = { viewModel.startBookmarksQuiz(25) },
                  onToggleBookmark = viewModel::toggleBookmark,
                  onResetAllProgress = viewModel::resetAllProgress,
                )

                TopTab.CREATOR -> CreatorScreen(
                  onShowMessage = { msg ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}
