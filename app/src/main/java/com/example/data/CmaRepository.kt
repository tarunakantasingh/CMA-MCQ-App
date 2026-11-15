package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class QuizMode {
  SECTION_MIXED,
  PAPER,
  BOOKMARKS,
  MISTAKES,
}

class CmaRepository(
  private val context: Context,
  private val dao: QuestionDao,
) {
  private val seedMutex = Mutex()
  @Volatile private var seeded = false

  suspend fun ensureSeeded() {
    if (seeded) return
    seedMutex.withLock {
      if (seeded) return
      withContext(Dispatchers.IO) {
        val bankPrefs = context.getSharedPreferences(PREF_BANK_META, Context.MODE_PRIVATE)
        val syncedVersion = bankPrefs.getInt(KEY_BANK_VERSION, 1)
        val existingCount = dao.getTotalCount()

        if (existingCount == 0) {
          val questions = AssetQuestionLoader.loadFromAssets(context)
          questions.chunked(500).forEach { chunk ->
            dao.insertAll(chunk)
          }
          bankPrefs.edit().putInt(KEY_BANK_VERSION, CURRENT_BANK_VERSION).apply()
        } else if (syncedVersion < CURRENT_BANK_VERSION) {
          val freshQuestions = AssetQuestionLoader.loadFromAssets(context)
          val existingById = dao.getAllOnce().associateBy { it.id }
          val merged = freshQuestions.map { q ->
            val prev = existingById[q.id]
            if (prev == null) q
            else q.copy(
              isAnswered = prev.isAnswered,
              isCorrectLast = prev.isCorrectLast,
              selectedIndexLast = prev.selectedIndexLast,
              isBookmarked = prev.isBookmarked,
              lastAttemptedAt = prev.lastAttemptedAt,
            )
          }
          merged.chunked(500).forEach { chunk ->
            dao.upsertAll(chunk)
          }
          val freshIds = freshQuestions.map { it.id }.toHashSet()
          val obsoleteIds = existingById.keys.filterNot { it in freshIds }
          obsoleteIds.chunked(500).forEach { chunk ->
            dao.deleteByIds(chunk)
          }
          bankPrefs.edit().putInt(KEY_BANK_VERSION, CURRENT_BANK_VERSION).apply()
        }
        seeded = true
      }
    }
  }

  private companion object {
    const val PREF_BANK_META = "cma_bank_meta"
    const val KEY_BANK_VERSION = "bank_version"
    const val CURRENT_BANK_VERSION = 2
  }

  fun observeSectionSummaries(): Flow<List<SectionProgressSummary>> = dao.observeSectionSummaries()

  fun observePaperSummaries(section: ExamSection): Flow<List<PaperProgressSummary>> =
    dao.observePaperSummaries(section.key)

  fun observeBookmarkedQuestions(): Flow<List<QuestionEntity>> = dao.observeBookmarkedQuestions()

  fun observeIncorrectQuestions(): Flow<List<QuestionEntity>> = dao.observeIncorrectQuestions()

  suspend fun startQuizQuestions(
    mode: QuizMode,
    section: ExamSection? = null,
    paperCode: String? = null,
    limit: Int = 25,
  ): List<QuestionEntity> = withContext(Dispatchers.IO) {
    ensureSeeded()
    when (mode) {
      QuizMode.SECTION_MIXED ->
        dao.getUnansweredBySection(section?.key ?: ExamSection.FOUNDATION.key, limit)
      QuizMode.PAPER ->
        dao.getUnansweredByPaper(
          section?.key ?: ExamSection.FOUNDATION.key,
          paperCode ?: "",
          limit,
        )
      QuizMode.BOOKMARKS -> dao.getBookmarkedForQuiz(limit)
      QuizMode.MISTAKES -> dao.getIncorrectForQuiz(limit)
    }
  }

  suspend fun recordAnswer(questionId: String, selectedIndex: Int, isCorrect: Boolean) {
    withContext(Dispatchers.IO) {
      dao.recordAnswer(
        id = questionId,
        selectedIndex = selectedIndex,
        isCorrect = isCorrect,
        timestamp = System.currentTimeMillis(),
      )
    }
  }

  suspend fun setBookmarked(questionId: String, bookmarked: Boolean) {
    withContext(Dispatchers.IO) {
      dao.setBookmarked(questionId, bookmarked)
    }
  }

  suspend fun searchQuestions(query: String): List<QuestionEntity> = withContext(Dispatchers.IO) {
    ensureSeeded()
    if (query.isBlank()) emptyList() else dao.searchQuestions(query.trim(), 80)
  }

  suspend fun resetPaperProgress(section: ExamSection, paperCode: String) {
    withContext(Dispatchers.IO) {
      dao.resetPaperProgress(section.key, paperCode)
    }
  }

  suspend fun resetSectionProgress(section: ExamSection) {
    withContext(Dispatchers.IO) {
      dao.resetSectionProgress(section.key)
    }
  }

  suspend fun resetAllProgress() {
    withContext(Dispatchers.IO) {
      dao.resetAllProgress()
    }
  }
}
