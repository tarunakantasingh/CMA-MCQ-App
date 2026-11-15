package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldCorrect
import com.example.ui.theme.RoseIncorrect

private const val CREATOR_EMAIL = "tarunakantasingh8@gmail.com"
private const val URL_GITHUB = "https://github.com/tarunakantasingh"
private const val URL_LINKEDIN = "https://www.linkedin.com/in/tarunakantasingh/"
private const val URL_INSTAGRAM = "https://www.instagram.com/tarunakantasingh/"
private const val URL_WHATSAPP = "https://whatsapp.com/channel/0029VaBMLlGFi8xh88JeJW22"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorScreen(
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val categories = listOf("Feedback", "Question Correction", "Feature Request", "Say Hello")
  var selectedCategory by remember { mutableStateOf(categories.first()) }
  var senderName by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("") }
  var message by remember { mutableStateOf("") }
  var formError by remember { mutableStateOf<String?>(null) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Text(
          text = "About the Creator",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      item {
        Card(
          shape = MaterialTheme.shapes.extraLarge,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        ) {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    listOf(
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                      MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                      Color.Transparent,
                    )
                  )
                )
                .padding(20.dp)
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                  border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                  modifier = Modifier.size(64.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Rounded.Person,
                      contentDescription = "Tarunakanta Singh",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(34.dp),
                    )
                  }
                }
                Column {
                  Text(
                    text = "Tarunakanta Singh",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "App Creator & CMA Community Builder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                  )
                }
              }

              Spacer(Modifier.height(14.dp))

              Text(
                text = "Built to give CMA Foundation, Intermediate, and Final aspirants an offline-first, distraction-free MCQ practice companion with instant audio feedback and zero-repeat tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      item {
        Text(
          text = "Connect & Follow",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SocialLinkCard(
              title = "GitHub",
              subtitle = "@tarunakantasingh",
              icon = Icons.Rounded.Code,
              accent = MaterialTheme.colorScheme.primary,
              testTag = "creator_github_button",
              onClick = { openExternalUrl(context, URL_GITHUB, onShowMessage) },
              modifier = Modifier.weight(1f),
            )
            SocialLinkCard(
              title = "LinkedIn",
              subtitle = "tarunakantasingh",
              icon = Icons.Rounded.Work,
              accent = MaterialTheme.colorScheme.secondary,
              testTag = "creator_linkedin_button",
              onClick = { openExternalUrl(context, URL_LINKEDIN, onShowMessage) },
              modifier = Modifier.weight(1f),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SocialLinkCard(
              title = "Instagram",
              subtitle = "@tarunakantasingh",
              icon = Icons.Rounded.PhotoCamera,
              accent = RoseIncorrect,
              testTag = "creator_instagram_button",
              onClick = { openExternalUrl(context, URL_INSTAGRAM, onShowMessage) },
              modifier = Modifier.weight(1f),
            )
            SocialLinkCard(
              title = "WhatsApp",
              subtitle = "CMA Channel",
              icon = Icons.Rounded.Forum,
              accent = EmeraldCorrect,
              testTag = "creator_whatsapp_button",
              onClick = { openExternalUrl(context, URL_WHATSAPP, onShowMessage) },
              modifier = Modifier.weight(1f),
            )
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
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.AlternateEmail,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
              Column {
                Text(
                  text = "Direct Contact Form",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = CREATOR_EMAIL,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              categories.forEach { cat ->
                FilterChip(
                  selected = selectedCategory == cat,
                  onClick = { selectedCategory = cat },
                  label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                )
              }
            }

            OutlinedTextField(
              value = senderName,
              onValueChange = {
                senderName = it
                formError = null
              },
              label = { Text("Your Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("contact_name_input"),
              shape = RoundedCornerShape(14.dp),
            )

            OutlinedTextField(
              value = subject,
              onValueChange = {
                subject = it
                formError = null
              },
              label = { Text("Subject") },
              placeholder = { Text("e.g., Paper 8 Q#42 feedback") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("contact_subject_input"),
              shape = RoundedCornerShape(14.dp),
            )

            OutlinedTextField(
              value = message,
              onValueChange = {
                message = it
                formError = null
              },
              label = { Text("Message") },
              minLines = 4,
              maxLines = 6,
              modifier = Modifier.fillMaxWidth().testTag("contact_message_input"),
              shape = RoundedCornerShape(14.dp),
            )

            if (formError != null) {
              Text(
                text = formError!!,
                style = MaterialTheme.typography.bodySmall,
                color = RoseIncorrect,
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("Creator Email", CREATOR_EMAIL))
                  onShowMessage("Email copied: $CREATOR_EMAIL")
                },
                modifier = Modifier.height(48.dp).testTag("contact_copy_email_button"),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Email")
              }

              Button(
                onClick = {
                  when {
                    senderName.isBlank() -> formError = "Please enter your name."
                    message.isBlank() -> formError = "Please enter a message."
                    else -> {
                      val finalSubject =
                        "[CMA Prep - $selectedCategory] " +
                          (subject.ifBlank { "Message from ${senderName.trim()}" })
                      val finalBody = buildString {
                        appendLine("Name: ${senderName.trim()}")
                        appendLine("Category: $selectedCategory")
                        appendLine()
                        appendLine(message.trim())
                      }
                      launchEmailIntent(context, finalSubject, finalBody, onShowMessage)
                    }
                  }
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("contact_send_email_button"),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Send Message", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      item { Spacer(Modifier.height(16.dp)) }
    }
  }
}

@Composable
private fun SocialLinkCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
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
    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accent.copy(alpha = 0.16f),
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
          }
        }
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
        contentDescription = "Open $title",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

private fun openExternalUrl(
  context: Context,
  url: String,
  onShowMessage: (String) -> Unit,
) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
  } catch (_: Throwable) {
    onShowMessage("No browser available to open link.")
  }
}

private fun launchEmailIntent(
  context: Context,
  subject: String,
  body: String,
  onShowMessage: (String) -> Unit,
) {
  val uri = Uri.parse(
    "mailto:$CREATOR_EMAIL?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
  )
  val intent = Intent(Intent.ACTION_SENDTO, uri)
  try {
    context.startActivity(Intent.createChooser(intent, "Send message to Tarunakanta Singh"))
  } catch (_: Throwable) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Creator Email", CREATOR_EMAIL))
    onShowMessage("No email app found. Copied $CREATOR_EMAIL to clipboard.")
  }
}
