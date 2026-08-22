package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val targetGrade: String = "A",
    val examDate: String = "", // e.g. "2026-09-15"
    val daysLeft: Int = 0,
    val topics: String = "", // Comma-separated or short syllabus summary
    val priority: String = "HIGH" // HIGH, MEDIUM, LOW
)

@Entity(tableName = "study_blocks")
data class StudyBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long = 0,
    val courseName: String,
    val courseColor: String,
    val title: String,
    val subtopics: String = "",
    val dateStr: String, // "YYYY-MM-DD"
    val timeSlot: String, // e.g. "09:00 AM - 10:30 AM"
    val durationMinutes: Int = 45,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val technique: String = "Active Recall", // Active Recall, Feynman Technique, Practice Quiz, Flashcards Drill, Deep Reading
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val isMilestone: Boolean = false
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val courseName: String,
    val description: String = "",
    val cardCount: Int = 0,
    val masteredCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val question: String,
    val answer: String,
    val keyConcept: String = "",
    val mnemonic: String = "",
    val isMastered: Boolean = false,
    val reviewCount: Int = 0
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val topic: String,
    val courseName: String,
    val score: Int = 0,
    val totalQuestions: Int = 5,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val questionNumber: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int, // 0..3
    val explanation: String,
    val selectedAnswerIndex: Int = -1 // -1 means unattempted
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // "YYYY-MM-DD"
    val courseName: String,
    val topic: String = "",
    val durationMinutes: Int = 25,
    val sessionType: String = "WORK", // WORK, SHORT_BREAK, LONG_BREAK
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "concept_summaries")
data class ConceptSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val topic: String,
    val originalText: String,
    val summaryText: String,
    val keyTakeaways: String, // JSON or bullet lines
    val actionItems: String, // JSON or bullet lines
    val memoryAnchors: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
