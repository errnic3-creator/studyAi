package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.ui.StudyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizBuilderScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val allQuizzes by viewModel.allQuizzes.collectAsStateWithLifecycle()
    val activeQuizId by viewModel.activeQuizId.collectAsStateWithLifecycle()
    val activeQuestions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val currentQuizIndex by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()

    var showGenerateDialog by remember { mutableStateOf(false) }

    if (activeQuizId != null && activeQuestions.isNotEmpty()) {
        // --- ACTIVE QUIZ TAKING FLOW ---
        var currentQIndex by remember(activeQuizId) { mutableIntStateOf(0) }
        val question = activeQuestions.getOrNull(currentQIndex) ?: activeQuestions.first()
        val currentQuiz = allQuizzes.find { it.id == activeQuizId }

        val hasAnswered = question.selectedAnswerIndex != -1
        val isCorrect = question.selectedAnswerIndex == question.correctAnswerIndex

        val totalQuestions = activeQuestions.size
        val answeredCount = activeQuestions.count { it.selectedAnswerIndex != -1 }
        val currentScore = activeQuestions.count { it.selectedAnswerIndex == it.correctAnswerIndex }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.closeQuiz() },
                        modifier = Modifier.testTag("exit_quiz_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Quiz")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentQuiz?.title ?: "Practice Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentQIndex + 1} of $totalQuestions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$currentScore / $totalQuestions",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Progress Bar
            item {
                LinearProgressIndicator(
                    progress = { (currentQIndex + 1).toFloat() / totalQuestions.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Indigo500,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            // Question Box Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Indigo500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "QUESTION ${currentQIndex + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Indigo500,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }

            // Options List
            item {
                val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEachIndexed { index, optionText ->
                        val isSelected = question.selectedAnswerIndex == index
                        val isThisCorrect = index == question.correctAnswerIndex

                        val borderCol = when {
                            hasAnswered && isThisCorrect -> Emerald500
                            hasAnswered && isSelected && !isThisCorrect -> Rose500
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }

                        val bgCol = when {
                            hasAnswered && isThisCorrect -> Emerald500.copy(alpha = 0.12f)
                            hasAnswered && isSelected && !isThisCorrect -> Rose500.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCol),
                            border = androidx.compose.foundation.BorderStroke(if (hasAnswered && (isThisCorrect || isSelected)) 2.dp else 1.dp, borderCol),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !hasAnswered) {
                                    viewModel.answerQuizQuestion(question.id, index)
                                }
                                .testTag("quiz_option_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hasAnswered && isThisCorrect) Emerald500
                                            else if (hasAnswered && isSelected) Rose500
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + index).toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasAnswered && (isThisCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered && isThisCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Emerald500)
                                } else if (hasAnswered && isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = Rose500)
                                }
                            }
                        }
                    }
                }
            }

            // Explanation Feedback Box (After answering)
            if (hasAnswered) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Emerald500.copy(alpha = 0.1f) else Rose500.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isCorrect) Emerald500 else Rose500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isCorrect) "Correct! Explanation:" else "Incorrect. Rationale:",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Emerald500 else Rose500
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Navigation Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentQIndex > 0) currentQIndex -= 1 },
                        enabled = currentQIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    if (currentQIndex < totalQuestions - 1) {
                        Button(
                            onClick = { currentQIndex += 1 },
                            enabled = hasAnswered
                        ) {
                            Text("Next Question")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.finishQuiz(activeQuizId!!, currentScore)
                                viewModel.closeQuiz()
                            },
                            enabled = answeredCount == totalQuestions,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish Quiz")
                        }
                    }
                }
            }
        }
    } else {
        // --- QUIZ BROWSER VIEW ---
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Practice Quizzes",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "5-question diagnostic quizzes with instant explanations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showGenerateDialog = true },
                        modifier = Modifier.testTag("open_generate_quiz_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Quiz")
                    }
                }
            }

            // Quizzes List
            if (allQuizzes.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No quizzes created yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build 5-question multiple choice quizzes instantly with Gemini AI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = { showGenerateDialog = true }) {
                                Text("Generate Practice Quiz")
                            }
                        }
                    }
                }
            } else {
                items(allQuizzes, key = { it.id }) { quiz ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openQuiz(quiz.id) }
                            .testTag("quiz_item_${quiz.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = quiz.courseName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteQuiz(quiz.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete quiz",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = quiz.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${quiz.totalQuestions} Questions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (quiz.isCompleted) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald500.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Score: ${quiz.score} / ${quiz.totalQuestions}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Emerald500,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Amber500.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Start Quiz",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Amber500,
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
        }
    }

    // AI Quiz Generation Modal
    if (showGenerateDialog) {
        var quizTitle by remember { mutableStateOf("") }
        var quizTopic by remember { mutableStateOf("") }
        var selectedCourse by remember { mutableStateOf(allCourses.firstOrNull()?.name ?: "General Study") }

        AlertDialog(
            onDismissRequest = { if (!isGenerating) showGenerateDialog = false },
            title = { Text("Generate 5-Question AI Quiz") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = quizTitle,
                        onValueChange = { quizTitle = it },
                        label = { Text("Quiz Title") },
                        placeholder = { Text("e.g. Graph Algorithms Diagnostic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quizTopic,
                        onValueChange = { quizTopic = it },
                        label = { Text("Topic or Study Material") },
                        placeholder = { Text("Enter topic or paste notes to generate questions from...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quizTopic.isNotBlank() || quizTitle.isNotBlank()) {
                            viewModel.generateAIQuiz(
                                title = quizTitle.ifBlank { quizTopic.take(30) },
                                topicOrNotes = if (quizTopic.isNotBlank()) quizTopic else quizTitle,
                                courseName = selectedCourse
                            )
                            showGenerateDialog = false
                        }
                    },
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generating...")
                    } else {
                        Text("Create Quiz")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }, enabled = !isGenerating) {
                    Text("Cancel")
                }
            }
        )
    }
}
