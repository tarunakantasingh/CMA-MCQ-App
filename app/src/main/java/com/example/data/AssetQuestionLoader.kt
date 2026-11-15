package com.example.data

import android.content.Context
import android.util.JsonReader
import java.io.InputStreamReader

object AssetQuestionLoader {
  fun loadFromAssets(context: Context): List<QuestionEntity> {
    val result = ArrayList<QuestionEntity>(5500)
    context.assets.open("cma_mcqs.json").use { input ->
      JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
        reader.beginArray()
        while (reader.hasNext()) {
          readQuestion(reader)?.let { result.add(it) }
        }
        reader.endArray()
      }
    }
    return result
  }

  private fun readQuestion(reader: JsonReader): QuestionEntity? {
    var id = ""
    var section = ""
    var paperCode = ""
    var paperName = ""
    var questionNumber = 0
    var question = ""
    val options = ArrayList<String>(4)
    var correctIndex = 0

    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        "id" -> id = reader.nextString()
        "section" -> section = reader.nextString()
        "paperCode" -> paperCode = reader.nextString()
        "paperName" -> paperName = reader.nextString()
        "questionNumber" -> questionNumber = reader.nextInt()
        "question" -> question = reader.nextString()
        "options" -> {
          reader.beginArray()
          while (reader.hasNext()) {
            options.add(reader.nextString())
          }
          reader.endArray()
        }
        "correctIndex" -> correctIndex = reader.nextInt()
        else -> reader.skipValue()
      }
    }
    reader.endObject()

    if (id.isBlank() || question.isBlank() || options.size < 4 || correctIndex !in 0..3) {
      return null
    }

    return QuestionEntity(
      id = id,
      section = section,
      paperCode = paperCode,
      paperName = paperName,
      paperSortOrder = computePaperSortOrder(paperCode),
      questionNumber = questionNumber,
      question = question,
      option1 = options[0],
      option2 = options[1],
      option3 = options[2],
      option4 = options[3],
      correctIndex = correctIndex,
    )
  }

  private fun computePaperSortOrder(paperCode: String): Int {
    val cleaned = paperCode.replace("Paper", "", ignoreCase = true).trim()
    val digits = cleaned.takeWhile { it.isDigit() }.toIntOrNull() ?: 99
    val suffix = cleaned.dropWhile { it.isDigit() }.trim().uppercase()
    val suffixScore = when (suffix) {
      "A" -> 1
      "B" -> 2
      "C" -> 3
      else -> 0
    }
    return digits * 10 + suffixScore
  }
}
