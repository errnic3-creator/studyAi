package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        CourseEntity::class,
        StudyBlockEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        PomodoroSessionEntity::class,
        ConceptSummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun studyBlockDao(): StudyBlockDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizDao(): QuizDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun conceptSummaryDao(): ConceptSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
