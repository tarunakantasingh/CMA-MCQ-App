package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "questions",
  indices = [
    Index(value = ["section", "isAnswered"]),
    Index(value = ["section", "paperCode", "isAnswered"]),
    Index(value = ["isBookmarked"]),
    Index(value = ["isAnswered", "isCorrectLast"]),
  ],
)
data class QuestionEntity(
  @PrimaryKey val id: String,
  val section: String,
  val paperCode: String,
  val paperName: String,
  val paperSortOrder: Int,
  val questionNumber: Int,
  val question: String,
  val option1: String,
  val option2: String,
  val option3: String,
  val option4: String,
  val correctIndex: Int,
  val isAnswered: Boolean = false,
  val isCorrectLast: Boolean? = null,
  val selectedIndexLast: Int? = null,
  val isBookmarked: Boolean = false,
  val lastAttemptedAt: Long? = null,
) {
  val options: List<String>
    get() = listOf(option1, option2, option3, option4)
}
