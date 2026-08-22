package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PomodoroSessionEntity
import com.example.ui.PomodoroMode
import com.example.ui.StudyViewModel
import com.example.ui.components.CircularTimerProgress
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.pomodoroMode.collectAsStateWithLifecycle()
    val totalMinutes by viewModel.pomodoroDurationMinutes.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.pomodoroSecondsRemaining.collectAsStateWithLifecycle()
    val isRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val completedCycles by viewModel.pomodoroCycles.collectAsStateWithLifecycle()
    val courseTag by viewModel.pomodoroCourseTag.collectAsStateWithLifecycle()
    val topic by viewModel.pomodoroTopic.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val sessions by viewModel.allPomodoroSessions.collectAsStateWithLifecycle()

    var showCustomDialog by remember { mutableStateOf(false) }
    var customMinInput by remember { mutableStateOf("45") }

    val timeSdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "FOCUS POMODORO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElegantDarkTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Deep Focus Timer",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )
                )
                Text(
                    text = "Boost retention and stamina using structured deep work intervals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantDarkTextSecondary
                )
            }
        }

        // Mode Switcher Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PomodoroMode.values().forEach { m ->
                    val isSelected = mode == m
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) LavenderOnPrimary else ElegantDarkSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) LavenderPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setPomodoroMode(m) }
                            .testTag("mode_tab_${m.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = m.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) LavenderPrimary else ElegantDarkTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${m.defaultMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) LavenderPrimary else ElegantDarkTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Course & Topic Tagging Strip
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Focus Objective",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { viewModel.setPomodoroTopic(it) },
                            placeholder = { Text("e.g. Cognitive Psychology • Unit 4", color = ElegantDarkTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = ElegantDarkOutline,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pomodoro_topic_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Main Animated Circular Progress Timer Container
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularTimerProgress(
                        totalSeconds = totalMinutes * 60,
                        secondsRemaining = remainingSeconds,
                        isRunning = isRunning,
                        modeTitle = mode.label,
                        onTogglePlay = { viewModel.togglePomodoroTimer() },
                        onReset = { viewModel.resetTimer() },
                        onSkip = { viewModel.setPomodoroMode(if (mode == PomodoroMode.WORK) PomodoroMode.SHORT_BREAK else PomodoroMode.WORK) }
                    )
                }
            }
        }

        // Session Stats Summary Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cycles Done", style = MaterialTheme.typography.bodySmall, color = ElegantDarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedCycles 🍅",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Logged", style = MaterialTheme.typography.bodySmall, color = ElegantDarkTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${sessions.sumOf { it.durationMinutes }} mins",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = LavenderPrimary
                            )
                        )
                    }
                }
            }
        }

        // Focus Session History Log Header
        item {
            Text(
                text = "Recent Focus Logs (${sessions.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextPrimary
                )
            )
        }

        if (sessions.isEmpty()) {
            item {
                Text(
                    text = "No focus sessions logged yet. Complete a Pomodoro interval to start tracking!",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextSecondary
                )
            }
        } else {
            items(sessions.take(8), key = { it.id }) { session ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (session.sessionType == "WORK") LavenderOnPrimary else Emerald500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (session.sessionType == "WORK") Icons.Default.CheckCircle else Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = if (session.sessionType == "WORK") LavenderPrimary else Emerald500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = session.topic.ifBlank { session.courseName },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElegantDarkTextPrimary
                                    )
                                )
                                Text(
                                    text = "${session.courseName} • ${session.dateStr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantDarkTextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LavenderOnPrimary
                        ) {
                            Text(
                                text = "+${session.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LavenderPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
