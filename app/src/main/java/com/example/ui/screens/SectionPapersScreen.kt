package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExamSection
import com.example.data.PaperProgressSummary
import com.example.ui.components.accentColor
import com.example.ui.components.accentOnColor
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionPapersScreen(
  section: ExamSection,
  papers: List<PaperProgressSummary>,
  onBack: () -> Unit,
  onStartMixedQuiz: (Int) -> Unit,
  onStartPaperQuiz: (PaperProgressSummary, Int) -> Unit,
  onResetPaper: (String) -> Unit,
  onResetSection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  var selectedQuizSize by remember { mutableIntStateOf(25) }
  var pendingResetPaper by remember { mutableStateOf<PaperProgressSummary?>(null) }
  var showSectionResetDialog by remember { mutableStateOf(false) }

  val accent = section.accentColor()
  val totalRemaining = papers.sumOf { it.remainingCount }
  val totalQuestions = papers.sumOf { it.totalCount }

  Column(modifier = modifier.fillMaxSize()) {
    TopAppBar(
      title = {
        Column {
          Text(
            text = "${section.displayName} Level",
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text = "${section.papersSubtitle} • $totalRemaining of $totalQuestions left",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
      navigationIcon = {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("section_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
          )
        }
      },
      actions = {
        IconButton(
          onClick = { showSectionResetDialog = true },
          modifier = Modifier.testTag("section_reset_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Reset section progress",
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
      ),
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          QuizLengthSelectorCard(
            accent = accent,
            onAccent = section.accentOnColor(),
            selectedSize = selectedQuizSize,
            totalRemaining = totalRemaining,
            onSelectSize = { selectedQuizSize = it },
            onStartMixed = {
              val count = if (selectedQuizSize == -1) 500 else selectedQuizSize
              onStartMixedQuiz(count)
            },
          )
        }

        item {
          Text(
            text = "Papers in ${section.displayName}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 6.dp),
          )
        }

        items(papers, key = { it.paperCode }) { paper ->
          PaperCard(
            paper = paper,
            accent = accent,
            onAccent = section.accentOnColor(),
            quizSize = selectedQuizSize,
            onPractice = {
              val count = if (selectedQuizSize == -1) paper.remainingCount.coerceAtLeast(1) else selectedQuizSize
              onStartPaperQuiz(paper, count)
            },
            onReset = { pendingResetPaper = paper },
          )
        }

        item { Spacer(Modifier.height(16.dp)) }
      }
    }
  }

  pendingResetPaper?.let { paper ->
    AlertDialog(
      onDismissRequest = { pendingResetPaper = null },
      title = { Text("Reset ${paper.paperCode}?") },
      text = {
        Text(
          "This marks all ${paper.totalCount} questions in ${paper.paperName} as unattempted so you can practice them again without repeats."
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            onResetPaper(paper.paperCode)
            pendingResetPaper = null
          },
          modifier = Modifier.testTag("confirm_reset_paper_button"),
        ) {
          Text("Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingResetPaper = null }) {
          Text("Cancel")
        }
      },
    )
  }

  if (showSectionResetDialog) {
    AlertDialog(
      onDismissRequest = { showSectionResetDialog = false },
      title = { Text("Reset ${section.displayName} Progress?") },
      text = {
        Text("All answered progress across ${section.papersSubtitle} will be cleared so questions can be practiced again.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onResetSection()
            showSectionResetDialog = false
          },
          modifier = Modifier.testTag("confirm_reset_section_button"),
        ) {
          Text("Reset All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSectionResetDialog = false }) {
          Text("Cancel")
        }
      },
    )
  }
}

@Composable
private fun QuizLengthSelectorCard(
  accent: Color,
  onAccent: Color,
  selectedSize: Int,
  totalRemaining: Int,
  onSelectSize: (Int) -> Unit,
  onStartMixed: () -> Unit,
) {
  val sizes = listOf(10 to "10 Qs", 25 to "25 Qs", 50 to "50 Qs", -1 to "All Left")

  Card(
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Text(
        text = "Quiz Session Length",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = "Questions already solved never repeat until you reset progress.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        sizes.forEach { (value, label) ->
          val selected = selectedSize == value
          FilterChip(
            selected = selected,
            onClick = { onSelectSize(value) },
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = accent.copy(alpha = 0.20f),
              selectedLabelColor = accent,
            ),
            modifier = Modifier.weight(1f).testTag("quiz_size_chip_$value"),
          )
        }
      }
      Spacer(Modifier.height(12.dp))
      Button(
        onClick = onStartMixed,
        enabled = totalRemaining > 0,
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_mixed_section_quiz_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = accent,
          contentColor = onAccent,
        ),
      ) {
        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
          text = if (totalRemaining > 0) "Start Mixed Section Quiz" else "Section Completed — Reset to Replay",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun PaperCard(
  paper: PaperProgressSummary,
  accent: Color,
  onAccent: Color,
  quizSize: Int,
  onPractice: () -> Unit,
  onReset: () -> Unit,
) {
  val progress = if (paper.totalCount == 0) 0f else paper.answeredCount.toFloat() / paper.totalCount.toFloat()
  val isDone = paper.remainingCount == 0 && paper.totalCount > 0

  Card(
    modifier = Modifier.fillMaxWidth().testTag("paper_card_${paper.paperCode.replace(" ", "_").lowercase()}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = accent.copy(alpha = 0.15f),
            ) {
              Text(
                text = paper.paperCode,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            if (isDone) {
              Surface(
                shape = RoundedCornerShape(50),
                color = EmeraldCorrect.copy(alpha = 0.15f),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Completed",
                    tint = EmeraldCorrect,
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldCorrect,
                  )
                }
              }
            }
          }
          Spacer(Modifier.height(6.dp))
          Text(
            text = paper.paperName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Spacer(Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "${paper.remainingCount} unattempted • ${paper.answeredCount}/${paper.totalCount}",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = MonoFontFamily,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (paper.answeredCount > 0) {
          Text(
            text = "${paper.accuracyPercent}% acc",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = MonoFontFamily,
            color = EmeraldCorrect,
          )
        }
      }

      Spacer(Modifier.height(6.dp))

      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
        color = if (isDone) EmeraldCorrect else accent,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        strokeCap = StrokeCap.Round,
      )

      Spacer(Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (paper.answeredCount > 0) {
          OutlinedButton(
            onClick = onReset,
            modifier = Modifier.height(48.dp).testTag("reset_paper_${paper.paperCode.replace(" ", "_").lowercase()}"),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Rounded.Refresh, contentDescription = "Reset paper", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Reset")
          }
        }

        Button(
          onClick = onPractice,
          enabled = paper.remainingCount > 0,
          modifier = Modifier.weight(1f).height(48.dp).testTag("start_paper_${paper.paperCode.replace(" ", "_").lowercase()}"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = onAccent,
          ),
        ) {
          Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          val nextCount = if (quizSize == -1) paper.remainingCount else minOf(quizSize, paper.remainingCount)
          Text(
            text = if (paper.remainingCount > 0) "Practice ($nextCount)" else "All Completed",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
