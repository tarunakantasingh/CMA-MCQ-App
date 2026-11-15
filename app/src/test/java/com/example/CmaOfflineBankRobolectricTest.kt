package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AssetQuestionLoader
import com.example.data.CmaDatabase
import com.example.data.CmaRepository
import com.example.data.ExamSection
import com.example.data.QuizMode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CmaOfflineBankRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: CmaDatabase

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, CmaDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `bundled json loads all three sections with valid options`() {
    val items = AssetQuestionLoader.loadFromAssets(context)
    assertTrue("Expected > 5000 MCQs, got ${items.size}", items.size > 5000)

    val sections = items.map { it.section }.toSet()
    assertEquals(setOf("Foundation", "Inter", "Final"), sections)

    val uniqueIds = items.map { it.id }.toSet()
    assertEquals(items.size, uniqueIds.size)

    items.forEach { q ->
      assertTrue(q.question.isNotBlank())
      assertEquals(4, q.options.size)
      assertTrue(q.options.all { it.isNotBlank() })
      assertTrue(q.correctIndex in 0..3)
    }
  }

  @Test
  fun `answered questions never repeat until paper is reset`() = runTest {
    val repo = CmaRepository(context, db.questionDao())
    val firstBatch = repo.startQuizQuestions(
      mode = QuizMode.PAPER,
      section = ExamSection.FOUNDATION,
      paperCode = "Paper 4",
      limit = 60,
    )
    assertEquals(60, firstBatch.size)

    firstBatch.forEach { q ->
      repo.recordAnswer(q.id, q.correctIndex, true)
    }

    val secondBatch = repo.startQuizQuestions(
      mode = QuizMode.PAPER,
      section = ExamSection.FOUNDATION,
      paperCode = "Paper 4",
      limit = 100,
    )
    val firstIds = firstBatch.map { it.id }.toSet()
    assertTrue(secondBatch.none { it.id in firstIds })
    assertEquals(57, secondBatch.size) // Paper 4 has 117 questions: 117 - 60 = 57 remaining

    secondBatch.forEach { q ->
      repo.recordAnswer(q.id, q.correctIndex, false)
    }

    val emptyBatch = repo.startQuizQuestions(
      mode = QuizMode.PAPER,
      section = ExamSection.FOUNDATION,
      paperCode = "Paper 4",
      limit = 10,
    )
    assertTrue(emptyBatch.isEmpty())

    repo.resetPaperProgress(ExamSection.FOUNDATION, "Paper 4")
    val afterReset = repo.startQuizQuestions(
      mode = QuizMode.PAPER,
      section = ExamSection.FOUNDATION,
      paperCode = "Paper 4",
      limit = 25,
    )
    assertEquals(25, afterReset.size)
  }

  @Test
  fun `bank upgrade refreshes leaked question text and preserves progress`() = runTest {
    val dao = db.questionDao()
    val firstBatch = AssetQuestionLoader.loadFromAssets(context)
    val sample = firstBatch.first()
    val staleRow = sample.copy(
      question = "${sample.question} COST ACCOUNTING (PAPER - 8)",
      isAnswered = true,
      selectedIndexLast = sample.correctIndex,
      isCorrectLast = true,
      isBookmarked = true,
    )
    val obsoleteRow = sample.copy(id = "inter_paper7_231_2")
    dao.insertAll(listOf(staleRow, obsoleteRow))

    context.getSharedPreferences("cma_bank_meta", Context.MODE_PRIVATE)
      .edit()
      .putInt("bank_version", 1)
      .commit()

    val repo = CmaRepository(context, dao)
    repo.ensureSeeded()

    val refreshed = dao.getAllOnce().associateBy { it.id }
    assertTrue("Expected obsolete row removed", !refreshed.containsKey("inter_paper7_231_2"))
    val updatedSample = refreshed[sample.id]!!
    assertEquals(sample.question, updatedSample.question)
    assertTrue(updatedSample.isAnswered)
    assertTrue(updatedSample.isBookmarked)
    assertEquals(firstBatch.size, refreshed.size)
  }
}
