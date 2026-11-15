package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class SectionProgressSummary(
  val section: String,
  val totalCount: Int,
  val answeredCount: Int,
  val correctCount: Int,
) {
  val remainingCount: Int get() = (totalCount - answeredCount).coerceAtLeast(0)
  val accuracyPercent: Int
    get() = if (answeredCount == 0) 0 else ((correctCount * 100f) / answeredCount).toInt()
}

data class PaperProgressSummary(
  val section: String,
  val paperCode: String,
  val paperName: String,
  val paperSortOrder: Int,
  val totalCount: Int,
  val answeredCount: Int,
  val correctCount: Int,
  val bookmarkedCount: Int,
) {
  val remainingCount: Int get() = (totalCount - answeredCount).coerceAtLeast(0)
  val accuracyPercent: Int
    get() = if (answeredCount == 0) 0 else ((correctCount * 100f) / answeredCount).toInt()
}

@Dao
interface QuestionDao {
  @Query("SELECT COUNT(*) FROM questions")
  suspend fun getTotalCount(): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(questions: List<QuestionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(questions: List<QuestionEntity>)

  @Query("SELECT * FROM questions")
  suspend fun getAllOnce(): List<QuestionEntity>

  @Query("DELETE FROM questions WHERE id IN (:ids)")
  suspend fun deleteByIds(ids: List<String>)

  @Query(
    """
    SELECT section,
           COUNT(*) AS totalCount,
           SUM(CASE WHEN isAnswered = 1 THEN 1 ELSE 0 END) AS answeredCount,
           SUM(CASE WHEN isCorrectLast = 1 THEN 1 ELSE 0 END) AS correctCount
    FROM questions
    GROUP BY section
    """
  )
  fun observeSectionSummaries(): Flow<List<SectionProgressSummary>>

  @Query(
    """
    SELECT section,
           paperCode,
           paperName,
           paperSortOrder,
           COUNT(*) AS totalCount,
           SUM(CASE WHEN isAnswered = 1 THEN 1 ELSE 0 END) AS answeredCount,
           SUM(CASE WHEN isCorrectLast = 1 THEN 1 ELSE 0 END) AS correctCount,
           SUM(CASE WHEN isBookmarked = 1 THEN 1 ELSE 0 END) AS bookmarkedCount
    FROM questions
    WHERE section = :section
    GROUP BY section, paperCode, paperName, paperSortOrder
    ORDER BY paperSortOrder ASC
    """
  )
  fun observePaperSummaries(section: String): Flow<List<PaperProgressSummary>>

  @Query("SELECT * FROM questions WHERE section = :section AND isAnswered = 0 ORDER BY RANDOM() LIMIT :limit")
  suspend fun getUnansweredBySection(section: String, limit: Int): List<QuestionEntity>

  @Query(
    "SELECT * FROM questions WHERE section = :section AND paperCode = :paperCode AND isAnswered = 0 ORDER BY RANDOM() LIMIT :limit"
  )
  suspend fun getUnansweredByPaper(section: String, paperCode: String, limit: Int): List<QuestionEntity>

  @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY RANDOM() LIMIT :limit")
  suspend fun getBookmarkedForQuiz(limit: Int): List<QuestionEntity>

  @Query("SELECT * FROM questions WHERE isAnswered = 1 AND isCorrectLast = 0 ORDER BY RANDOM() LIMIT :limit")
  suspend fun getIncorrectForQuiz(limit: Int): List<QuestionEntity>

  @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY paperSortOrder ASC, questionNumber ASC")
  fun observeBookmarkedQuestions(): Flow<List<QuestionEntity>>

  @Query("SELECT * FROM questions WHERE isAnswered = 1 AND isCorrectLast = 0 ORDER BY lastAttemptedAt DESC")
  fun observeIncorrectQuestions(): Flow<List<QuestionEntity>>

  @Query(
    """
    SELECT * FROM questions
    WHERE question LIKE '%' || :query || '%'
       OR paperName LIKE '%' || :query || '%'
       OR paperCode LIKE '%' || :query || '%'
    ORDER BY paperSortOrder ASC, questionNumber ASC
    LIMIT :limit
    """
  )
  suspend fun searchQuestions(query: String, limit: Int = 80): List<QuestionEntity>

  @Query(
    """
    UPDATE questions
    SET isAnswered = 1,
        selectedIndexLast = :selectedIndex,
        isCorrectLast = :isCorrect,
        lastAttemptedAt = :timestamp
    WHERE id = :id
    """
  )
  suspend fun recordAnswer(id: String, selectedIndex: Int, isCorrect: Boolean, timestamp: Long)

  @Query("UPDATE questions SET isBookmarked = :bookmarked WHERE id = :id")
  suspend fun setBookmarked(id: String, bookmarked: Boolean)

  @Query(
    """
    UPDATE questions
    SET isAnswered = 0,
        isCorrectLast = NULL,
        selectedIndexLast = NULL,
        lastAttemptedAt = NULL
    WHERE section = :section AND paperCode = :paperCode
    """
  )
  suspend fun resetPaperProgress(section: String, paperCode: String)

  @Query(
    """
    UPDATE questions
    SET isAnswered = 0,
        isCorrectLast = NULL,
        selectedIndexLast = NULL,
        lastAttemptedAt = NULL
    WHERE section = :section
    """
  )
  suspend fun resetSectionProgress(section: String)

  @Query(
    """
    UPDATE questions
    SET isAnswered = 0,
        isCorrectLast = NULL,
        selectedIndexLast = NULL,
        lastAttemptedAt = NULL
    """
  )
  suspend fun resetAllProgress()
}
