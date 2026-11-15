package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.QuizSessionState
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.RoseIncorrect

@Composable
fun QuizScreen(
  session: QuizSessionState,
  soundEnabled: Boolean,
  hapticEnabled: Boolean,
  onSelectOption: (Int) -> Unit,
  onNextQuestion: () -> Unit,
  onToggleBookmark: (String, Boolean) -> Unit,
  onToggleSound: (Boolean) -> Unit,
  onCloseQuiz: () -> Unit,
  onContinueMore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showExitConfirm by remember { mutableStateOf(false) }

  BackHandler {
    if (session.isCompleted || session.answers.isEmpty()) {
      onCloseQuiz()
    } else {
      showExitConfirm = true
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
      contentAlignment = Alignment.TopCenter,
    ) {
      if (session.isCompleted) {
        QuizResultView(
          session = session,
          onDone = onCloseQuiz,
          onContinueMore = onContinueMore,
          modifier = Modifier.fillMaxSize().widthIn(max = 800.dp),
        )
      } else {
        QuizActiveView(
          session = session,
          soundEnabled = soundEnabled,
          hapticEnabled = hapticEnabled,
          onAttemptClose = {
            if (session.answers.isEmpty()) onCloseQuiz() else showExitConfirm = true
          },
          onSelectOption = onSelectOption,
          onNextQuestion = onNextQuestion,
          onToggleBookmark = onToggleBookmark,
          onToggleSound = onToggleSound,
          modifier = Modifier.fillMaxSize().widthIn(max = 800.dp),
        )
      }
    }
  }

  if (showExitConfirm) {
    AlertDialog(
      onDismissRequest = { showExitConfirm = false },
      title = { Text("End Quiz Session?") },
      text = {
        Text(
          "Your ${session.answers.size} answered question(s) are already saved and won't repeat later."
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showExitConfirm = false
            onCloseQuiz()
          },
          modifier = Modifier.testTag("confirm_exit_quiz_button"),
        ) {
          Text("Exit")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitConfirm = false }) {
          Text("Resume")
        }
      },
    )
  }
}

@Composable
private fun QuizActiveView(
  session: QuizSessionState,
  soundEnabled: Boolean,
  hapticEnabled: Boolean,
  onAttemptClose: () -> Unit,
  onSelectOption: (Int) -> Unit,
  onNextQuestion: () -> Unit,
  onToggleBookmark: (String, Boolean) -> Unit,
  onToggleSound: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val question = session.currentQuestion ?: return
  val haptics = LocalHapticFeedback.current
  val hasAnswered = session.selectedOptionIndex != null
  val progress = (session.currentIndex + 1).toFloat() / session.questions.size.coerceAtLeast(1).toFloat()
  val optionLabels = listOf("A", "B", "C", "D")

  Column(
    modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
  ) {
    // Top Quiz Header Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onAttemptClose,
        modifier = Modifier.size(48.dp).testTag("quiz_close_button"),
      ) {
        Icon(Icons.Rounded.Close, contentDescription = "Close quiz")
      }

      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = session.title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "Question ${session.currentIndex + 1} of ${session.questions.size}",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = MonoFontFamily,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (session.currentStreak > 0) {
          Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
            modifier = Modifier.padding(end = 4.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "${session.currentStreak}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
        IconButton(
          onClick = { onToggleSound(!soundEnabled) },
          modifier = Modifier.size(48.dp).testTag("quiz_sound_toggle_button"),
        ) {
          Icon(
            imageVector =
              if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
              else Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription = if (soundEnabled) "Mute sound" else "Enable sound",
            tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = { onToggleBookmark(question.id, !question.isBookmarked) },
          modifier = Modifier.size(48.dp).testTag("quiz_bookmark_button"),
        ) {
          Icon(
            imageVector =
              if (question.isBookmarked) Icons.Rounded.Bookmark
              else Icons.Rounded.BookmarkBorder,
            contentDescription = if (question.isBookmarked) "Remove bookmark" else "Bookmark question",
            tint = if (question.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    LinearProgressIndicator(
      progress = { progress.coerceIn(0f, 1f) },
      modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
      color = MaterialTheme.colorScheme.primary,
      trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      strokeCap = StrokeCap.Round,
    )

    Spacer(Modifier.height(16.dp))

    // Scrollable Question + Options Area
    Column(
      modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                text = "${question.section} • ${question.paperCode}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            Text(
              text = "Q#${question.questionNumber}",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = MonoFontFamily,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(Modifier.height(12.dp))
          Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      question.options.forEachIndexed { idx, optText ->
        val isSelected = session.selectedOptionIndex == idx
        val isCorrectOpt = idx == question.correctIndex

        val targetBg = when {
          !hasAnswered -> MaterialTheme.colorScheme.surfaceContainer
          isCorrectOpt -> EmeraldCorrect.copy(alpha = 0.16f)
          isSelected && !isCorrectOpt -> RoseIncorrect.copy(alpha = 0.16f)
          else -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
        }
        val targetBorder = when {
          !hasAnswered -> MaterialTheme.colorScheme.outlineVariant
          isCorrectOpt -> EmeraldCorrect
          isSelected && !isCorrectOpt -> RoseIncorrect
          else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        }

        val animatedBg by animateColorAsState(targetBg, animationSpec = tween(220), label = "opt_bg")
        val animatedBorder by animateColorAsState(targetBorder, animationSpec = tween(220), label = "opt_border")

        Card(
          onClick = {
            if (!hasAnswered) {
              if (hapticEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
              onSelectOption(idx)
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("quiz_option_$idx"),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = animatedBg),
          border = BorderStroke(if (hasAnswered && (isCorrectOpt || isSelected)) 1.5.dp else 1.dp, animatedBorder),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Surface(
              shape = CircleShape,
              color = when {
                hasAnswered && isCorrectOpt -> EmeraldCorrect
                hasAnswered && isSelected && !isCorrectOpt -> RoseIncorrect
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
              },
              modifier = Modifier.size(32.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = optionLabels.getOrElse(idx) { "${idx + 1}" },
                  style = MaterialTheme.typography.labelLarge,
                  fontFamily = MonoFontFamily,
                  color =
                    if (hasAnswered && (isCorrectOpt || isSelected)) Color.White
                    else MaterialTheme.colorScheme.onSurface,
                )
              }
            }

            Text(
              text = optText,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f),
            )

            if (hasAnswered) {
              if (isCorrectOpt) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = "Correct option",
                  tint = EmeraldCorrect,
                  modifier = Modifier.size(22.dp),
                )
              } else if (isSelected) {
                Icon(
                  imageVector = Icons.Rounded.Cancel,
                  contentDescription = "Incorrect option",
                  tint = RoseIncorrect,
                  modifier = Modifier.size(22.dp),
                )
              }
            }
          }
        }
      }

      AnimatedVisibility(
        visible = hasAnswered,
        enter = fadeIn(tween(200)) + slideInVertically { it / 3 },
      ) {
        val isCorrect = session.selectedOptionIndex == question.correctIndex
        val bannerColor = if (isCorrect) EmeraldCorrect else RoseIncorrect
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = bannerColor.copy(alpha = 0.13f),
          border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
              contentDescription = null,
              tint = bannerColor,
              modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isCorrect) "Correct Answer!" else "Incorrect",
                style = MaterialTheme.typography.labelLarge,
                color = bannerColor,
              )
              if (!isCorrect) {
                val correctLabel = optionLabels.getOrElse(question.correctIndex) { "${question.correctIndex + 1}" }
                val correctText = question.options.getOrNull(question.correctIndex).orEmpty()
                Text(
                  text = "Correct: $correctLabel. $correctText",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }

      Spacer(Modifier.height(8.dp))
    }

    // Bottom Next / Finish Action
    AnimatedVisibility(visible = hasAnswered) {
      val isLast = session.currentIndex + 1 >= session.questions.size
      Button(
        onClick = onNextQuestion,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp).testTag("quiz_next_button"),
        shape = RoundedCornerShape(16.dp),
      ) {
        Text(
          text = if (isLast) "View Result Summary" else "Next Question",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun QuizResultView(
  session: QuizSessionState,
  onDone: () -> Unit,
  onContinueMore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // 0 = All, 1 = Incorrect, 2 = Correct
  var filterMode by remember { mutableIntStateOf(0) }
  val filtered = remember(session.answers, filterMode) {
    when (filterMode) {
      1 -> session.answers.filter { !it.isCorrect }
      2 -> session.answers.filter { it.isCorrect }
      else -> session.answers
    }
  }

  LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
            modifier = Modifier.size(64.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = "Session completed",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(34.dp),
              )
            }
          }
          Spacer(Modifier.height(12.dp))
          Text(
            text = "Session Complete",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = session.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            ResultStatItem("Score", "${session.correctCount}/${session.answers.size}", EmeraldCorrect)
            ResultStatItem("Accuracy", "${session.accuracyPercent}%", MaterialTheme.colorScheme.primary)
            ResultStatItem("Wrong", "${session.answers.size - session.correctCount}", RoseIncorrect)
          }
          Spacer(Modifier.height(18.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = onDone,
              modifier = Modifier.weight(1f).height(48.dp).testTag("result_done_button"),
              shape = RoundedCornerShape(14.dp),
            ) {
              Text("Back")
            }
            Button(
              onClick = onContinueMore,
              modifier = Modifier.weight(1.2f).height(48.dp).testTag("result_continue_button"),
              shape = RoundedCornerShape(14.dp),
            ) {
              Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Practice Next")
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
          selected = filterMode == 0,
          onClick = { filterMode = 0 },
          label = { Text("All (${session.answers.size})") },
        )
        FilterChip(
          selected = filterMode == 1,
          onClick = { filterMode = 1 },
          label = { Text("Wrong (${session.answers.count { !it.isCorrect }})") },
        )
        FilterChip(
          selected = filterMode == 2,
          onClick = { filterMode = 2 },
          label = { Text("Correct (${session.correctCount})") },
        )
      }
    }

    itemsIndexed(filtered, key = { _, item -> item.question.id }) { idx, rec ->
      val border = if (rec.isCorrect) EmeraldCorrect.copy(alpha = 0.35f) else RoseIncorrect.copy(alpha = 0.35f)
      Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, border),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "#${idx + 1} • ${rec.question.paperCode}",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = MonoFontFamily,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
              imageVector = if (rec.isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
              contentDescription = if (rec.isCorrect) "Correct" else "Incorrect",
              tint = if (rec.isCorrect) EmeraldCorrect else RoseIncorrect,
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(Modifier.height(6.dp))
          Text(
            text = rec.question.question,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = "Your answer: ${rec.question.options.getOrNull(rec.selectedIndex).orEmpty()}",
            style = MaterialTheme.typography.bodySmall,
            color = if (rec.isCorrect) EmeraldCorrect else RoseIncorrect,
          )
          if (!rec.isCorrect) {
            Text(
              text = "Correct answer: ${rec.question.options.getOrNull(rec.question.correctIndex).orEmpty()}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = EmeraldCorrect,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ResultStatItem(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge,
      fontFamily = MonoFontFamily,
      color = color,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
