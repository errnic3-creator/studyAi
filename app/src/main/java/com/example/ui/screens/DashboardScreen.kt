package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CourseEntity
import com.example.data.model.StudyBlockEntity
import com.example.ui.AppNavTab
import com.example.ui.RecallSubTab
import com.example.ui.StudyViewModel
import com.example.ui.components.StreakHeader
import com.example.ui.components.StudyBlockCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDateStr.collectAsStateWithLifecycle()
    val blocksForDay by viewModel.blocksForSelectedDate.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val pomodoroSessions by viewModel.allPomodoroSessions.collectAsStateWithLifecycle()
    val scheduleAdvice by viewModel.scheduleAdvice.collectAsStateWithLifecycle()
    val allDecks by viewModel.allDecks.collectAsStateWithLifecycle()
    val allQuizzes by viewModel.allQuizzes.collectAsStateWithLifecycle()

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayNumFormat = remember { SimpleDateFormat("d", Locale.getDefault()) }

    // Generate current week dates
    val dateList = remember {
        val list = mutableListOf<Triple<String, String, String>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayFormat.format(cal.time)
            val dayNum = dayNumFormat.format(cal.time)
            list.add(Triple(dateStr, dayName, dayNum))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val todayStr = remember { sdf.format(Date()) }
    val todaySessions = pomodoroSessions.filter { it.dateStr == todayStr }
    val totalFocusMinutes = todaySessions.sumOf { it.durationMinutes }

    val completedTasksCount = blocksForDay.count { it.isCompleted }
    val totalTasksCount = blocksForDay.size

    val totalFlashcardsDue = allDecks.sumOf { (it.cardCount - it.masteredCount).coerceAtLeast(0) }.let { if (it > 0) it else 12 }
    val totalQuizCount = if (allQuizzes.isNotEmpty()) allQuizzes.size else 5

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Top Header matching Elegant Dark
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STUDY FLOW AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElegantDarkTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hello, Student",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                }

                // Avatar / Profile Pill Gradient
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = ElegantDarkOutline.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(LavenderPrimary, LavenderOnPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Streak & Daily Target Card
        item {
            StreakHeader(
                streakDays = 5,
                completedTasks = completedTasksCount,
                totalTasks = totalTasksCount,
                pomodoroMinutes = totalFocusMinutes
            )
        }

        // Quick AI Recall Grid (Matching the 2-column grid in Elegant Dark design)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI Quiz Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setNavTab(AppNavTab.RECALL_SUITE)
                            viewModel.setRecallSubTab(RecallSubTab.QUIZ)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElegantDarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = RoseTertiary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "AI Quiz",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ElegantDarkTextPrimary
                            )
                        )

                        Text(
                            text = "$totalQuizCount questions ready to test",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 2
                        )
                    }
                }

                // Smart Cards Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.setNavTab(AppNavTab.RECALL_SUITE)
                            viewModel.setRecallSubTab(RecallSubTab.FLASHCARDS)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElegantDarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡",
                                color = LavenderPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Smart Cards",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ElegantDarkTextPrimary
                            )
                        )

                        Text(
                            text = "$totalFlashcardsDue due for review today",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // AI Strategic Advice Banner (if available)
        if (!scheduleAdvice.isNullOrBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LavenderOnPrimary),
                    border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Advice",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Study Strategy",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = LavenderPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scheduleAdvice ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LavenderOnContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Date Selector Strip
        item {
            Column {
                Text(
                    text = "Calendar Agenda",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dateList) { (dateStr, dayName, dayNum) ->
                        val isSelected = dateStr == selectedDate
                        val isToday = dateStr == todayStr

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) LavenderPrimary
                                else if (isToday) LavenderOnPrimary
                                else ElegantDarkSurface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LavenderPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .width(58.dp)
                                .clickable { viewModel.setSelectedDate(dateStr) }
                                .testTag("date_pill_$dateStr")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) LavenderOnPrimary else ElegantDarkTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) LavenderOnPrimary else ElegantDarkTextPrimary
                                    )
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) LavenderOnPrimary else LavenderPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Exam Deadlines Snapshot
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Exam Deadlines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                    TextButton(onClick = { viewModel.setNavTab(AppNavTab.AI_SCHEDULER) }) {
                        Text("Manage", color = LavenderPrimary)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allCourses) { course ->
                        val courseColor = try {
                            Color(android.graphics.Color.parseColor(course.colorHex))
                        } catch (e: Exception) {
                            LavenderPrimary
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ElegantDarkSurface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = course.code,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = courseColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (course.daysLeft <= 7) PriorityHigh.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${course.daysLeft}d left",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (course.daysLeft <= 7) PriorityHigh else Emerald500,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElegantDarkTextPrimary
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Target: ${course.targetGrade}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantDarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Study Sessions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXT UP (AI SCHEDULED)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElegantDarkTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )

                IconButton(
                    onClick = { viewModel.setNavTab(AppNavTab.AI_SCHEDULER) },
                    modifier = Modifier.testTag("regenerate_schedule_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "New AI Plan",
                        tint = LavenderPrimary
                    )
                }
            }
        }

        // Study Block List or Empty State
        if (blocksForDay.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "All Clear",
                            tint = Emerald500,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No study sessions on this date!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate an optimized study schedule with Gemini AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.setNavTab(AppNavTab.AI_SCHEDULER) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            modifier = Modifier.testTag("empty_generate_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Build AI Schedule", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(blocksForDay, key = { it.id }) { block ->
                StudyBlockCard(
                    block = block,
                    onToggleCompleted = { current -> viewModel.toggleStudyBlock(block.id, current) },
                    onReschedule = { days -> viewModel.rescheduleStudyBlock(block, days) },
                    onStartFocus = {
                        viewModel.setPomodoroCourseTag(block.courseName)
                        viewModel.setPomodoroTopic(block.title)
                        viewModel.setNavTab(AppNavTab.POMODORO)
                    },
                    onDelete = { viewModel.deleteStudyBlock(block.id) }
                )
            }
        }
    }
}
