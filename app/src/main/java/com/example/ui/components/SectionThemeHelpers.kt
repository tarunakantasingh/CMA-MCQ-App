package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.OfflineBolt
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExamSection
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.AmberTertiaryDark
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoPrimaryDark
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.SkySecondaryDark

@Composable
fun ExamSection.accentColor(): Color {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  return when (this) {
    ExamSection.FOUNDATION -> if (isDark) SkySecondary else SkySecondaryDark
    ExamSection.INTER -> if (isDark) IndigoPrimary else IndigoPrimaryDark
    ExamSection.FINAL -> if (isDark) AmberTertiary else AmberTertiaryDark
  }
}

@Composable
fun ExamSection.accentOnColor(): Color {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  return if (isDark) Color(0xFF0A0E17) else Color.White
}

fun ExamSection.icon(): ImageVector =
  when (this) {
    ExamSection.FOUNDATION -> Icons.AutoMirrored.Rounded.MenuBook
    ExamSection.INTER -> Icons.Rounded.AccountBalance
    ExamSection.FINAL -> Icons.Rounded.WorkspacePremium
  }

@Composable
fun OfflineStatusPill(
  totalCount: Int,
  modifier: Modifier = Modifier,
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  val pillGreen = if (isDark) EmeraldCorrect else Color(0xFF047857)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = pillGreen.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, pillGreen.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.OfflineBolt,
        contentDescription = "Offline ready",
        tint = pillGreen,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = if (totalCount > 0) "Offline • $totalCount MCQs" else "Offline Ready",
        style = MaterialTheme.typography.labelSmall,
        color = pillGreen,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}
