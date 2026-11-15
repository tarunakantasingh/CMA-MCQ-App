package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExamSection
import com.example.data.SectionProgressSummary
import com.example.ui.HomeUiState
import com.example.ui.components.OfflineStatusPill
import com.example.ui.components.accentColor
import com.example.ui.components.accentOnColor
import com.example.ui.components.icon
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.RoseIncorrect

@Composable
fun HomeScreen(
  state: HomeUiState,
  onOpenSection: (ExamSection) -> Unit,
  onQuickStartSection: (ExamSection) -> Unit,
  onStartMistakesDrill: () -> Unit,
  onStartBookmarksDrill: () -> Unit,
  onToggleSound: (Boolean) -> Unit,
  onToggleDarkMode: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    if (state.isSeeding && state.totalQuestions == 0) {
      Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
          text = "Preparing offline CMA question bank…",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
          text = "Indexing Foundation, Inter & Final papers for instant offline study",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      return
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        HomeTopHeader(
          totalCount = state.totalQuestions,
          soundEnabled = state.settings.soundEnabled,
          darkModeEnabled = state.settings.darkModeEnabled,
          onToggleSound = onToggleSound,
          onToggleDarkMode = onToggleDarkMode,
        )
      }

      item {
        OverallProgressBanner(state = state)
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Exam Levels",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "Zero-repeat tracking",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      items(ExamSection.entries, key = { it.key }) { section ->
        val summary = state.sectionSummaries[section]
        ExamSectionCard(
          section = section,
          summary = summary,
          onOpenPapers = { onOpenSection(section) },
          onQuickStart = { onQuickStartSection(section) },
        )
      }

      item {
        Text(
          text = "Smart Drills",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickDrillCard(
            title = "Mistake Bank",
            count = state.incorrectQuestions.size,
            subtitle = "Retry wrong answers",
            icon = Icons.Rounded.ErrorOutline,
            accent = RoseIncorrect,
            testTag = "home_mistakes_drill_card",
            onClick = onStartMistakesDrill,
            modifier = Modifier.weight(1f),
          )
          QuickDrillCard(
            title = "Bookmarks",
            count = state.bookmarkedQuestions.size,
            subtitle = "Saved MCQs",
            icon = Icons.Rounded.Bookmark,
            accent = MaterialTheme.colorScheme.tertiary,
            testTag = "home_bookmarks_drill_card",
            onClick = onStartBookmarksDrill,
            modifier = Modifier.weight(1f),
          )
        }
      }

      item { Spacer(Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun HomeTopHeader(
  totalCount: Int,
  soundEnabled: Boolean,
  darkModeEnabled: Boolean,
  onToggleSound: (Boolean) -> Unit,
  onToggleDarkMode: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "CMA Prep",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(Modifier.height(6.dp))
      OfflineStatusPill(totalCount = totalCount)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        IconButton(
          onClick = { onToggleSound(!soundEnabled) },
          modifier = Modifier.size(48.dp).testTag("toggle_sound_button"),
        ) {
          Icon(
            imageVector =
              if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
              else Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription = if (soundEnabled) "Mute quiz sound" else "Enable quiz sound",
            tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        IconButton(
          onClick = { onToggleDarkMode(!darkModeEnabled) },
          modifier = Modifier.size(48.dp).testTag("toggle_theme_button"),
        ) {
          Icon(
            imageVector = if (darkModeEnabled) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = if (darkModeEnabled) "Switch to light mode" else "Switch to dark mode",
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun OverallProgressBanner(state: HomeUiState) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                Color.Transparent,
              )
            )
          )
          .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Study Progress",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${state.totalAnswered}",
              style = MaterialTheme.typography.headlineMedium,
              fontFamily = MonoFontFamily,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = " / ${state.totalQuestions} solved",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 3.dp),
            )
          }
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBadge(
              label = "Accuracy",
              value = "${state.overallAccuracy}%",
              color = EmeraldCorrect,
            )
            StatBadge(
              label = "Best Streak",
              value = "${state.settings.bestStreak}",
              color = MaterialTheme.colorScheme.tertiary,
              icon = Icons.Rounded.LocalFireDepartment,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatBadge(
  label: String,
  value: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = color.copy(alpha = 0.13f),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(14.dp),
        )
      }
      Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelMedium,
        fontFamily = MonoFontFamily,
        color = color,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun ExamSectionCard(
  section: ExamSection,
  summary: SectionProgressSummary?,
  onOpenPapers: () -> Unit,
  onQuickStart: () -> Unit,
) {
  val accent = section.accentColor()
  val total = summary?.totalCount ?: 0
  val answered = summary?.answeredCount ?: 0
  val remaining = summary?.remainingCount ?: total
  val accuracy = summary?.accuracyPercent ?: 0
  val progress = if (total == 0) 0f else answered.toFloat() / total.toFloat()

  Card(
    onClick = onOpenPapers,
    modifier = Modifier.fillMaxWidth().animateContentSize().testTag("section_card_${section.key.lowercase()}"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = accent.copy(alpha = 0.16f),
            modifier = Modifier.size(48.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = section.icon(),
                contentDescription = section.displayName,
                tint = accent,
                modifier = Modifier.size(26.dp),
              )
            }
          }
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = section.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Surface(
                shape = RoundedCornerShape(50),
                color = accent.copy(alpha = 0.14f),
              ) {
                Text(
                  text = section.papersSubtitle,
                  style = MaterialTheme.typography.labelSmall,
                  color = accent,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }
            Spacer(Modifier.height(2.dp))
            Text(
              text = section.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Spacer(Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "$remaining left • $answered/$total done",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = MonoFontFamily,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = if (answered > 0) "$accuracy% acc" else "$total MCQs",
          style = MaterialTheme.typography.labelMedium,
          fontFamily = MonoFontFamily,
          color = if (answered > 0) EmeraldCorrect else accent,
        )
      }

      Spacer(Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
        color = accent,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        strokeCap = StrokeCap.Round,
      )

      Spacer(Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedButton(
          onClick = onOpenPapers,
          modifier = Modifier.weight(1f).height(48.dp).testTag("open_papers_${section.key.lowercase()}"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Text("Papers", style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }

        Button(
          onClick = onQuickStart,
          modifier = Modifier.weight(1.15f).height(48.dp).testTag("quick_quiz_${section.key.lowercase()}"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = section.accentOnColor(),
          ),
        ) {
          Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(4.dp))
          Text(
            text = if (remaining > 0) "Quick 25" else "View Papers",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
private fun QuickDrillCard(
  title: String,
  count: Int,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accent: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier.testTag(testTag),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accent.copy(alpha = 0.15f),
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = title,
              tint = accent,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Text(
          text = "$count",
          style = MaterialTheme.typography.titleLarge,
          fontFamily = MonoFontFamily,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Spacer(Modifier.height(10.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
