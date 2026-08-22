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
import com.example.data.model.CourseEntity
import com.example.ui.StudyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScheduleBuilderScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingSchedule.collectAsStateWithLifecycle()

    var freeHours by remember { mutableFloatStateOf(3.5f) }
    var selectedPeakTime by remember { mutableStateOf("Morning (8am - 12pm)") }
    var focusNotes by remember { mutableStateOf("Focus heavily on dynamic programming and organic chemistry mechanisms.") }

    var showAddCourseDialog by remember { mutableStateOf(false) }

    val peakTimeOptions = listOf(
        "Morning (8am - 12pm)",
        "Afternoon (1pm - 5pm)",
        "Night (6pm - 11pm)",
        "Flexible / Distributed"
    )

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
                    text = "AI SMART SCHEDULER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ElegantDarkTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Smart Study Planner",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )
                )
                Text(
                    text = "Automates study scheduling & splits exams into prioritized daily study blocks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantDarkTextSecondary
                )
            }
        }

        // 1. Courses & Exam Deadlines Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Courses & Target Exams",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                        )
                        Button(
                            onClick = { showAddCourseDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_course_dialog_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Course", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (allCourses.isEmpty()) {
                        Text(
                            text = "No courses added. Click 'Add Course' to begin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextSecondary
                        )
                    } else {
                        allCourses.forEach { course ->
                            val courseColor = try {
                                Color(android.graphics.Color.parseColor(course.colorHex))
                            } catch (e: Exception) {
                                LavenderPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ElegantDarkSurfaceElevated,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(courseColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${course.code}: ${course.name}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ElegantDarkTextPrimary
                                                )
                                            )
                                            Text(
                                                text = "Exam in ${course.daysLeft} days • Target: ${course.targetGrade}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ElegantDarkTextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteCourse(course.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete course",
                                            tint = ElegantDarkTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Daily Free Hours Slider
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Free Study Time",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextPrimary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LavenderOnPrimary
                        ) {
                            Text(
                                text = "${String.format("%.1f", freeHours)} Hours / Day",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = LavenderPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = freeHours,
                        onValueChange = { freeHours = it },
                        valueRange = 1.0f..8.0f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = LavenderPrimary,
                            activeTrackColor = LavenderPrimary,
                            inactiveTrackColor = ElegantDarkOutline
                        ),
                        modifier = Modifier.testTag("free_hours_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1.0h", style = MaterialTheme.typography.bodySmall, color = ElegantDarkTextSecondary)
                        Text("4.0h", style = MaterialTheme.typography.bodySmall, color = ElegantDarkTextSecondary)
                        Text("8.0h", style = MaterialTheme.typography.bodySmall, color = ElegantDarkTextSecondary)
                    }
                }
            }
        }

        // 3. Peak Productivity Window Selector
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Peak Productivity Window",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    peakTimeOptions.forEach { option ->
                        val isSelected = option == selectedPeakTime
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LavenderOnPrimary else ElegantDarkSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LavenderPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPeakTime = option }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPeakTime = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = LavenderPrimary,
                                        unselectedColor = ElegantDarkOutline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) LavenderPrimary else ElegantDarkTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Focus Notes & Weak Areas
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weak Areas & Exam Syllabus Notes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = focusNotes,
                        onValueChange = { focusNotes = it },
                        placeholder = { Text("Enter difficult topics, chapters, or exam notes...", color = ElegantDarkTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("focus_notes_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 5. Main AI Generate Button
        item {
            Button(
                onClick = {
                    viewModel.generateAISchedule(
                        freeHours = freeHours,
                        peakTime = selectedPeakTime,
                        extraNotes = focusNotes
                    )
                },
                enabled = !isGenerating,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("schedule_generate_btn")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = LavenderOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Synthesizing Schedule with Gemini AI...", color = LavenderOnPrimary, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate AI Study Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Add Course Modal Dialog
    if (showAddCourseDialog) {
        var cName by remember { mutableStateOf("") }
        var cCode by remember { mutableStateOf("") }
        var cDays by remember { mutableStateOf("10") }
        var cGrade by remember { mutableStateOf("A") }
        var cTopics by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#D0BCFF") }

        val colorOptions = listOf("#D0BCFF", "#EFB8C8", "#CCC2DC", "#80D99D", "#FFB4AB", "#938F99")

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            containerColor = ElegantDarkSurface,
            title = { Text("Add New Course", color = ElegantDarkTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("Course Name (e.g. Neuroscience)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cCode,
                        onValueChange = { cCode = it },
                        label = { Text("Course Code (e.g. NEUR 101)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cDays,
                            onValueChange = { cDays = it },
                            label = { Text("Days until Exam") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = ElegantDarkOutline,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cGrade,
                            onValueChange = { cGrade = it },
                            label = { Text("Target Grade") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = ElegantDarkOutline,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = cTopics,
                        onValueChange = { cTopics = it },
                        label = { Text("Key Topics (comma-separated)") },
                        placeholder = { Text("Action potentials, Synapses") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Course Theme Color", style = MaterialTheme.typography.labelSmall, color = ElegantDarkTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorOptions.forEach { hex ->
                            val col = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable { selectedColorHex = hex }
                                    .then(
                                        if (selectedColorHex == hex) androidx.compose.foundation.BorderStroke(2.dp, Color.White).let { Modifier }
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cName.isNotBlank() && cCode.isNotBlank()) {
                            viewModel.addCourse(
                                name = cName,
                                code = cCode,
                                colorHex = selectedColorHex,
                                targetGrade = cGrade.ifBlank { "A" },
                                daysLeft = cDays.toIntOrNull() ?: 10,
                                topics = cTopics
                            )
                            showAddCourseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = LavenderOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Course", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancel", color = ElegantDarkTextSecondary)
                }
            }
        )
    }
}
