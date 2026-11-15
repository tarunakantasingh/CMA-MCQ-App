package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExamSection
import com.example.data.QuestionEntity
import com.example.ui.HomeUiState
import com.example.ui.components.accentColor
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.RoseIncorrect

@Composable
fun ReviewScreen(
  state: HomeUiState,
  searchQuery: String,
  searchResults: List<QuestionEntity>,
  onSearchQueryChange: (String) -> Unit,
  onStartMistakesDrill: () -> Unit,
  onStartBookmarksDrill: () -> Unit,
  onToggleBookmark: (String, Boolean) -> Unit,
  onResetAllProgress: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // 0 = Mistakes, 1 = Bookmarks, 2 = Offline Search
  var selectedTab by remember { mutableIntStateOf(0) }
  var showResetAllDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Review & Analytics",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Offline mastery across all 22 CMA papers",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (state.totalAnswered > 0) {
            OutlinedButton(
              onClick = { showResetAllDialog = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("review_reset_all_button"),
            ) {
              Icon(Icons.Rounded.DeleteSweep, contentDescription = "Reset all progress", modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(4.dp))
              Text("Reset")
            }
          }
        }
      }

      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            ExamSection.entries.forEach { sec ->
              val summary = state.sectionSummaries[sec]
              val total = summary?.totalCount ?: 0
              val answered = summary?.answeredCount ?: 0
              val acc = summary?.accuracyPercent ?: 0
              val progress = if (total == 0) 0f else answered.toFloat() / total.toFloat()
              val accent = sec.accentColor()

              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = "${sec.displayName} (${sec.papersSubtitle})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "$answered/$total • ${if (answered > 0) "$acc%" else "—"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = MonoFontFamily,
                    color = accent,
                  )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                  progress = { progress.coerceIn(0f, 1f) },
                  modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                  color = accent,
                  trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                  strokeCap = StrokeCap.Round,
                )
              }
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            label = { Text("Mistakes (${state.incorrectQuestions.size})") },
            modifier = Modifier.testTag("review_tab_mistakes"),
          )
          FilterChip(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            label = { Text("Saved (${state.bookmarkedQuestions.size})") },
            modifier = Modifier.testTag("review_tab_bookmarks"),
          )
          FilterChip(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            label = { Text("Search") },
            modifier = Modifier.testTag("review_tab_search"),
          )
        }
      }

      when (selectedTab) {
        0 -> {
          if (state.incorrectQuestions.isNotEmpty()) {
            item {
              Button(
                onClick = onStartMistakesDrill,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("review_start_mistakes_quiz"),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Practice Mistake Bank (${minOf(25, state.incorrectQuestions.size)} Qs)")
              }
            }
            items(state.incorrectQuestions, key = { it.id }) { q ->
              ReviewQuestionCard(
                question = q,
                onToggleBookmark = { onToggleBookmark(q.id, !q.isBookmarked) },
              )
            }
          } else {
            item {
              EmptyReviewNotice(
                title = "No mistakes logged yet",
                body = "Any question you miss during quizzes appears here for focused offline revision.",
              )
            }
          }
        }

        1 -> {
          if (state.bookmarkedQuestions.isNotEmpty()) {
            item {
              Button(
                onClick = onStartBookmarksDrill,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("review_start_bookmarks_quiz"),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Quiz Bookmarked Questions (${minOf(25, state.bookmarkedQuestions.size)} Qs)")
              }
            }
            items(state.bookmarkedQuestions, key = { it.id }) { q ->
              ReviewQuestionCard(
                question = q,
                onToggleBookmark = { onToggleBookmark(q.id, !q.isBookmarked) },
              )
            }
          } else {
            item {
              EmptyReviewNotice(
                title = "No bookmarked questions",
                body = "Tap the bookmark icon during any quiz to save tricky MCQs for quick revision.",
              )
            }
          }
        }

        2 -> {
          item {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              modifier = Modifier.fillMaxWidth().testTag("review_search_input"),
              placeholder = { Text("Search keywords across all offline MCQs…") },
              leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(16.dp),
            )
          }

          if (searchQuery.isBlank()) {
            item {
              EmptyReviewNotice(
                title = "Offline Question Lookup",
                body = "Type any term (e.g., GST, Ind AS, CAPM, Standard Costing) to search offline.",
              )
            }
          } else if (searchResults.isEmpty()) {
            item {
              EmptyReviewNotice(
                title = "No matches found",
                body = "Try a shorter keyword or paper name.",
              )
            }
          } else {
            items(searchResults, key = { it.id }) { q ->
              ReviewQuestionCard(
                question = q,
                onToggleBookmark = { onToggleBookmark(q.id, !q.isBookmarked) },
              )
            }
          }
        }
      }

      item { Spacer(Modifier.height(16.dp)) }
    }
  }

  if (showResetAllDialog) {
    AlertDialog(
      onDismissRequest = { showResetAllDialog = false },
      title = { Text("Reset All Study Progress?") },
      text = {
        Text("This clears answered history across Foundation, Inter, and Final so all questions become available again. Bookmarks are preserved.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onResetAllProgress()
            showResetAllDialog = false
          },
          modifier = Modifier.testTag("confirm_reset_all_button"),
        ) {
          Text("Reset All", color = RoseIncorrect)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetAllDialog = false }) {
          Text("Cancel")
        }
      },
    )
  }
}

@Composable
private fun ReviewQuestionCard(
  question: QuestionEntity,
  onToggleBookmark: () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        ) {
          Text(
            text = "${question.section} • ${question.paperCode} • Q#${question.questionNumber}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        IconButton(
          onClick = onToggleBookmark,
          modifier = Modifier.size(48.dp),
        ) {
          Icon(
            imageVector = if (question.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = if (question.isBookmarked) "Remove bookmark" else "Bookmark",
            tint = if (question.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Spacer(Modifier.height(6.dp))
      Text(
        text = question.question,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = "Answer: ${question.options.getOrNull(question.correctIndex).orEmpty()}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = EmeraldCorrect,
      )
    }
  }
}

@Composable
private fun EmptyReviewNotice(title: String, body: String) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
