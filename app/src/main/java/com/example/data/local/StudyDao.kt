package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY daysLeft ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)
}

@Dao
interface StudyBlockDao {
    @Query("SELECT * FROM study_blocks ORDER BY dateStr ASC, id ASC")
    fun getAllStudyBlocks(): Flow<List<StudyBlockEntity>>

    @Query("SELECT * FROM study_blocks WHERE dateStr = :date ORDER BY id ASC")
    fun getStudyBlocksForDate(date: String): Flow<List<StudyBlockEntity>>

    @Query("SELECT * FROM study_blocks WHERE isCompleted = 1")
    fun getCompletedBlocks(): Flow<List<StudyBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyBlock(block: StudyBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyBlocks(blocks: List<StudyBlockEntity>)

    @Update
    suspend fun updateStudyBlock(block: StudyBlockEntity)

    @Query("UPDATE study_blocks SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun toggleBlockCompletion(id: Long, completed: Boolean, completedAt: Long)

    @Query("DELETE FROM study_blocks WHERE id = :id")
    suspend fun deleteStudyBlockById(id: Long)

    @Query("DELETE FROM study_blocks")
    suspend fun clearAllStudyBlocks()
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    fun getFlashcardsForDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Query("UPDATE flashcards SET isMastered = :isMastered, reviewCount = reviewCount + 1 WHERE id = :cardId")
    suspend fun updateCardMastery(cardId: Long, isMastered: Boolean)

    @Query("UPDATE flashcard_decks SET masteredCount = (SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND isMastered = 1) WHERE id = :deckId")
    suspend fun refreshDeckMasteryCount(deckId: Long)

    @Query("DELETE FROM flashcard_decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Long)

    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Long)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY questionNumber ASC")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>)

    @Query("UPDATE quiz_questions SET selectedAnswerIndex = :selectedIndex WHERE id = :questionId")
    suspend fun answerQuestion(questionId: Long, selectedIndex: Int)

    @Query("UPDATE quizzes SET score = :score, isCompleted = 1 WHERE id = :quizId")
    suspend fun finishQuiz(quizId: Long, score: Int)

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: Long)
}

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE dateStr = :dateStr")
    fun getSessionsForDate(dateStr: String): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long
}

@Dao
interface ConceptSummaryDao {
    @Query("SELECT * FROM concept_summaries ORDER BY createdAt DESC")
    fun getAllSummaries(): Flow<List<ConceptSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: ConceptSummaryEntity): Long

    @Query("DELETE FROM concept_summaries WHERE id = :id")
    suspend fun deleteSummaryById(id: Long)
}
