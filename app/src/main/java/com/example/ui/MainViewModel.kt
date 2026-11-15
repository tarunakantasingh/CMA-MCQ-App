package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.QuizSoundManager
import com.example.data.CmaDatabase
import com.example.data.CmaRepository
import com.example.data.ExamSection
import com.example.data.PaperProgressSummary
import com.example.data.QuestionEntity
import com.example.data.QuizMode
import com.example.data.SectionProgressSummary
import com.example.data.UserPreferencesRepository
import com.example.data.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizAnswerRecord(
  val question: QuestionEntity,
  val selectedIndex: Int,
  val isCorrect: Boolean,
)

data class QuizSessionState(
  val title: String,
  val subtitle: String,
  val mode: QuizMode,
  val section: ExamSection?,
  val paperCode: String?,
  val requestedLimit: Int,
  val questions: List<QuestionEntity>,
  val currentIndex: Int = 0,
  val selectedOptionIndex: Int? = null,
  val answers: List<QuizAnswerRecord> = emptyList(),
  val currentStreak: Int = 0,
  val isCompleted: Boolean = false,
) {
  val currentQuestion: QuestionEntity?
    get() = questions.getOrNull(currentIndex)

  val correctCount: Int
    get() = answers.count { it.isCorrect }

  val accuracyPercent: Int
    get() = if (answers.isEmpty()) 0 else ((correctCount * 100f) / answers.size).toInt()
}

data class HomeUiState(
  val isSeeding: Boolean = true,
  val sectionSummaries: Map<ExamSection, SectionProgressSummary> = emptyMap(),
  val bookmarkedQuestions: List<QuestionEntity> = emptyList(),
  val incorrectQuestions: List<QuestionEntity> = emptyList(),
  val settings: UserSettings = UserSettings(),
) {
  val totalQuestions: Int get() = sectionSummaries.values.sumOf { it.totalCount }
  val totalAnswered: Int get() = sectionSummaries.values.sumOf { it.answeredCount }
  val totalCorrect: Int get() = sectionSummaries.values.sumOf { it.correctCount }
  val overallAccuracy: Int
    get() = if (totalAnswered == 0) 0 else ((totalCorrect * 100f) / totalAnswered).toInt()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val db = CmaDatabase.getInstance(application)
  private val repository = CmaRepository(application, db.questionDao())
  private val prefs = UserPreferencesRepository(application)
  private val soundManager = QuizSoundManager()

  private val _isSeeding = MutableStateFlow(true)
  private val _selectedSection = MutableStateFlow(ExamSection.FOUNDATION)
  private val _paperSummaries = MutableStateFlow<List<PaperProgressSummary>>(emptyList())
  val paperSummaries: StateFlow<List<PaperProgressSummary>> = _paperSummaries.asStateFlow()

  private val _quizSession = MutableStateFlow<QuizSessionState?>(null)
  val quizSession: StateFlow<QuizSessionState?> = _quizSession.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<QuestionEntity>>(emptyList())
  val searchResults: StateFlow<List<QuestionEntity>> = _searchResults.asStateFlow()

  private val _quizEmptyMessage = MutableStateFlow<String?>(null)
  val quizEmptyMessage: StateFlow<String?> = _quizEmptyMessage.asStateFlow()

  private var paperJob: Job? = null
  private var searchJob: Job? = null

  val homeUiState: StateFlow<HomeUiState> =
    combine(
      _isSeeding,
      repository.observeSectionSummaries(),
      repository.observeBookmarkedQuestions(),
      repository.observeIncorrectQuestions(),
      prefs.settingsFlow,
    ) { seeding, sections, bookmarks, incorrects, settings ->
      soundManager.isEnabled = settings.soundEnabled
      val map = sections.associateBy { ExamSection.fromKey(it.section) }
      HomeUiState(
        isSeeding = seeding,
        sectionSummaries = map,
        bookmarkedQuestions = bookmarks,
        incorrectQuestions = incorrects,
        settings = settings,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = HomeUiState(),
    )

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
      _isSeeding.value = false
    }
    observeSectionPapers(ExamSection.FOUNDATION)
  }

  fun selectSection(section: ExamSection) {
    _selectedSection.value = section
    observeSectionPapers(section)
  }

  private fun observeSectionPapers(section: ExamSection) {
    paperJob?.cancel()
    paperJob = viewModelScope.launch {
      repository.observePaperSummaries(section).collectLatest { list ->
        _paperSummaries.value = list
      }
    }
  }

  fun toggleDarkMode(enabled: Boolean) {
    viewModelScope.launch { prefs.setDarkMode(enabled) }
  }

  fun toggleSound(enabled: Boolean) {
    soundManager.isEnabled = enabled
    viewModelScope.launch { prefs.setSoundEnabled(enabled) }
  }

  fun toggleHaptics(enabled: Boolean) {
    viewModelScope.launch { prefs.setHapticEnabled(enabled) }
  }

  fun clearQuizEmptyMessage() {
    _quizEmptyMessage.value = null
  }

  fun startSectionQuiz(section: ExamSection, limit: Int) {
    viewModelScope.launch {
      val questions = repository.startQuizQuestions(
        mode = QuizMode.SECTION_MIXED,
        section = section,
        limit = limit,
      )
      if (questions.isEmpty()) {
        _quizSession.value = null
        _quizEmptyMessage.value =
          "All ${section.displayName} questions are completed! Reset a paper or section to practice again."
        return@launch
      }
      _quizSession.value = QuizSessionState(
        title = "${section.displayName} Practice",
        subtitle = "Mixed Non-Repeating Quiz",
        mode = QuizMode.SECTION_MIXED,
        section = section,
        paperCode = null,
        requestedLimit = limit,
        questions = questions,
      )
    }
  }

  fun startPaperQuiz(section: ExamSection, paperCode: String, paperName: String, limit: Int) {
    viewModelScope.launch {
      val questions = repository.startQuizQuestions(
        mode = QuizMode.PAPER,
        section = section,
        paperCode = paperCode,
        limit = limit,
      )
      if (questions.isEmpty()) {
        _quizSession.value = null
        _quizEmptyMessage.value =
          "You've completed every question in $paperCode! Reset this paper to re-practice."
        return@launch
      }
      _quizSession.value = QuizSessionState(
        title = paperCode,
        subtitle = paperName,
        mode = QuizMode.PAPER,
        section = section,
        paperCode = paperCode,
        requestedLimit = limit,
        questions = questions,
      )
    }
  }

  fun startBookmarksQuiz(limit: Int = 25) {
    viewModelScope.launch {
      val questions = repository.startQuizQuestions(mode = QuizMode.BOOKMARKS, limit = limit)
      if (questions.isEmpty()) {
        _quizSession.value = null
        _quizEmptyMessage.value = "No bookmarked questions yet. Tap the bookmark icon during any quiz."
        return@launch
      }
      _quizSession.value = QuizSessionState(
        title = "Bookmarked Drill",
        subtitle = "Saved Questions Review",
        mode = QuizMode.BOOKMARKS,
        section = null,
        paperCode = null,
        requestedLimit = limit,
        questions = questions,
      )
    }
  }

  fun startMistakesQuiz(limit: Int = 25) {
    viewModelScope.launch {
      val questions = repository.startQuizQuestions(mode = QuizMode.MISTAKES, limit = limit)
      if (questions.isEmpty()) {
        _quizSession.value = null
        _quizEmptyMessage.value = "Great job — your mistake bank is empty right now!"
        return@launch
      }
      _quizSession.value = QuizSessionState(
        title = "Mistake Bank Drill",
        subtitle = "Retry Incorrect Answers",
        mode = QuizMode.MISTAKES,
        section = null,
        paperCode = null,
        requestedLimit = limit,
        questions = questions,
      )
    }
  }

  fun submitOption(optionIndex: Int) {
    val session = _quizSession.value ?: return
    if (session.selectedOptionIndex != null || session.isCompleted) return
    val currentQ = session.currentQuestion ?: return

    val isCorrect = optionIndex == currentQ.correctIndex
    if (isCorrect) {
      soundManager.playCorrect()
    } else {
      soundManager.playIncorrect()
    }

    val newStreak = if (isCorrect) session.currentStreak + 1 else 0
    val updatedRecord = QuizAnswerRecord(
      question = currentQ.copy(
        isAnswered = true,
        selectedIndexLast = optionIndex,
        isCorrectLast = isCorrect,
      ),
      selectedIndex = optionIndex,
      isCorrect = isCorrect,
    )

    _quizSession.update { cur ->
      cur?.copy(
        selectedOptionIndex = optionIndex,
        answers = cur.answers + updatedRecord,
        currentStreak = newStreak,
      )
    }

    viewModelScope.launch {
      repository.recordAnswer(currentQ.id, optionIndex, isCorrect)
      if (newStreak > 0) prefs.updateBestStreak(newStreak)
    }
  }

  fun nextQuestion() {
    val session = _quizSession.value ?: return
    if (session.selectedOptionIndex == null) return

    if (session.currentIndex + 1 >= session.questions.size) {
      soundManager.playComplete()
      _quizSession.update { it?.copy(isCompleted = true) }
    } else {
      _quizSession.update {
        it?.copy(
          currentIndex = it.currentIndex + 1,
          selectedOptionIndex = null,
        )
      }
    }
  }

  fun toggleBookmark(questionId: String, newState: Boolean) {
    _quizSession.update { session ->
      session?.copy(
        questions = session.questions.map { q ->
          if (q.id == questionId) q.copy(isBookmarked = newState) else q
        },
        answers = session.answers.map { rec ->
          if (rec.question.id == questionId) rec.copy(question = rec.question.copy(isBookmarked = newState))
          else rec
        },
      )
    }
    viewModelScope.launch {
      repository.setBookmarked(questionId, newState)
    }
  }

  fun closeQuiz() {
    _quizSession.value = null
  }

  fun continueMoreQuestions() {
    val session = _quizSession.value ?: return
    when (session.mode) {
      QuizMode.SECTION_MIXED -> session.section?.let { startSectionQuiz(it, session.requestedLimit) }
      QuizMode.PAPER -> {
        val sec = session.section
        val code = session.paperCode
        if (sec != null && code != null) {
          startPaperQuiz(sec, code, session.subtitle, session.requestedLimit)
        }
      }
      QuizMode.BOOKMARKS -> startBookmarksQuiz(session.requestedLimit)
      QuizMode.MISTAKES -> startMistakesQuiz(session.requestedLimit)
    }
  }

  fun resetPaperProgress(section: ExamSection, paperCode: String) {
    viewModelScope.launch { repository.resetPaperProgress(section, paperCode) }
  }

  fun resetSectionProgress(section: ExamSection) {
    viewModelScope.launch { repository.resetSectionProgress(section) }
  }

  fun resetAllProgress() {
    viewModelScope.launch { repository.resetAllProgress() }
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      _searchResults.value = repository.searchQuestions(query)
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
  }
}
