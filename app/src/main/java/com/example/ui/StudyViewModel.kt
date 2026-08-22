package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudyDatabase
import com.example.data.model.*
import com.example.data.remote.GeneratedScheduleResult
import com.example.data.remote.GeneratedSummaryResult
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppNavTab(val label: String) {
    DASHBOARD("Dashboard"),
    AI_SCHEDULER("AI Planner"),
    POMODORO("Focus Timer"),
    RECALL_SUITE("AI Recall"),
    ANALYTICS("Analytics")
}

enum class RecallSubTab(val label: String) {
    FLASHCARDS("Flashcards"),
    QUIZ("Quiz Builder"),
    SUMMARIZER("Summarizer")
}

enum class PomodoroMode(val label: String, val defaultMinutes: Int) {
    WORK("Deep Work", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudyDatabase.getDatabase(application)
    private val repository = StudyRepository(database)

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Navigation State
    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _recallSubTab = MutableStateFlow(RecallSubTab.FLASHCARDS)
    val recallSubTab: StateFlow<RecallSubTab> = _recallSubTab.asStateFlow()

    // Selected Date for Agenda
    private val _selectedDateStr = MutableStateFlow(sdf.format(Date()))
    val selectedDateStr: StateFlow<String> = _selectedDateStr.asStateFlow()

    // Database Flows
    val allCourses: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyBlocks: StateFlow<List<StudyBlockEntity>> = repository.allStudyBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecks: StateFlow<List<FlashcardDeckEntity>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPomodoroSessions: StateFlow<List<PomodoroSessionEntity>> = repository.allPomodoroSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSummaries: StateFlow<List<ConceptSummaryEntity>> = repository.allSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Blocks for Current Selected Date
    val blocksForSelectedDate: StateFlow<List<StudyBlockEntity>> = combine(
        allStudyBlocks,
        _selectedDateStr
    ) { blocks, date ->
        blocks.filter { it.dateStr == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Generation States
    private val _isGeneratingSchedule = MutableStateFlow(false)
    val isGeneratingSchedule: StateFlow<Boolean> = _isGeneratingSchedule.asStateFlow()

    private val _scheduleAdvice = MutableStateFlow<String?>(null)
    val scheduleAdvice: StateFlow<String?> = _scheduleAdvice.asStateFlow()

    private val _isGeneratingFlashcards = MutableStateFlow(false)
    val isGeneratingFlashcards: StateFlow<Boolean> = _isGeneratingFlashcards.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    private val _latestSummary = MutableStateFlow<GeneratedSummaryResult?>(null)
    val latestSummary: StateFlow<GeneratedSummaryResult?> = _latestSummary.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Active Flashcard Deck Practice State
    private val _activeDeckId = MutableStateFlow<Long?>(null)
    val activeDeckId: StateFlow<Long?> = _activeDeckId.asStateFlow()

    val activeDeckCards: StateFlow<List<FlashcardEntity>> = _activeDeckId.flatMapLatest { deckId ->
        if (deckId != null) repository.getFlashcardsForDeck(deckId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    // Active Quiz Practice State
    private val _activeQuizId = MutableStateFlow<Long?>(null)
    val activeQuizId: StateFlow<Long?> = _activeQuizId.asStateFlow()

    val activeQuizQuestions: StateFlow<List<QuizQuestionEntity>> = _activeQuizId.flatMapLatest { quizId ->
        if (quizId != null) repository.getQuestionsForQuiz(quizId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    // Pomodoro Focus Timer State
    private val _pomodoroMode = MutableStateFlow(PomodoroMode.WORK)
    val pomodoroMode: StateFlow<PomodoroMode> = _pomodoroMode.asStateFlow()

    private val _pomodoroDurationMinutes = MutableStateFlow(25)
    val pomodoroDurationMinutes: StateFlow<Int> = _pomodoroDurationMinutes.asStateFlow()

    private val _pomodoroSecondsRemaining = MutableStateFlow(25 * 60)
    val pomodoroSecondsRemaining: StateFlow<Int> = _pomodoroSecondsRemaining.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroCycles = MutableStateFlow(0)
    val pomodoroCycles: StateFlow<Int> = _pomodoroCycles.asStateFlow()

    private val _pomodoroCourseTag = MutableStateFlow("Data Structures")
    val pomodoroCourseTag: StateFlow<String> = _pomodoroCourseTag.asStateFlow()

    private val _pomodoroTopic = MutableStateFlow("")
    val pomodoroTopic: StateFlow<String> = _pomodoroTopic.asStateFlow()

    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (e: Exception) {
            // Ignore if audio permissions or device audio fails
        }

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setNavTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setRecallSubTab(subTab: RecallSubTab) {
        _recallSubTab.value = subTab
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDateStr.value = dateStr
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // --- Study Blocks & Tasks ---

    fun toggleStudyBlock(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleStudyBlock(id, !currentStatus)
        }
    }

    fun deleteStudyBlock(id: Long) {
        viewModelScope.launch {
            repository.deleteStudyBlock(id)
            _uiMessage.value = "Study session removed"
        }
    }

    fun rescheduleStudyBlock(block: StudyBlockEntity, daysToAdd: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            try {
                cal.time = sdf.parse(block.dateStr) ?: Date()
            } catch (e: Exception) {
                cal.time = Date()
            }
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val newDate = sdf.format(cal.time)
            repository.rescheduleBlock(block, newDate, block.timeSlot)
            _uiMessage.value = "Rescheduled to $newDate"
        }
    }

    // --- AI Scheduler ---

    fun generateAISchedule(
        freeHours: Float,
        peakTime: String,
        extraNotes: String
    ) {
        viewModelScope.launch {
            _isGeneratingSchedule.value = true
            try {
                val courses = allCourses.value
                val result = repository.generateAndSaveAISchedule(courses, freeHours, peakTime, extraNotes)
                _scheduleAdvice.value = result.studyAdvice
                _uiMessage.value = "AI Generated ${result.blocks.size} dynamic study sessions!"
                _currentTab.value = AppNavTab.DASHBOARD
            } catch (e: Exception) {
                _uiMessage.value = "Error generating schedule: ${e.localizedMessage}"
            } finally {
                _isGeneratingSchedule.value = false
            }
        }
    }

    fun addCourse(
        name: String,
        code: String,
        colorHex: String,
        targetGrade: String,
        daysLeft: Int,
        topics: String
    ) {
        viewModelScope.launch {
            val course = CourseEntity(
                name = name.trim(),
                code = code.trim(),
                colorHex = colorHex,
                targetGrade = targetGrade,
                daysLeft = daysLeft,
                topics = topics.trim(),
                priority = if (daysLeft <= 7) "HIGH" else if (daysLeft <= 14) "MEDIUM" else "LOW"
            )
            repository.insertCourse(course)
            _uiMessage.value = "Course added: $name"
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch {
            repository.deleteCourse(id)
            _uiMessage.value = "Course deleted"
        }
    }

    // --- Pomodoro Timer Controls ---

    fun setPomodoroMode(mode: PomodoroMode, customMinutes: Int? = null) {
        stopTimer()
        _pomodoroMode.value = mode
        val minutes = customMinutes ?: mode.defaultMinutes
        _pomodoroDurationMinutes.value = minutes
        _pomodoroSecondsRemaining.value = minutes * 60
    }

    fun setPomodoroCourseTag(tag: String) {
        _pomodoroCourseTag.value = tag
    }

    fun setPomodoroTopic(topic: String) {
        _pomodoroTopic.value = topic
    }

    fun togglePomodoroTimer() {
        if (_isPomodoroRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isPomodoroRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_pomodoroSecondsRemaining.value > 0 && _isPomodoroRunning.value) {
                delay(1000)
                _pomodoroSecondsRemaining.value -= 1
            }
            if (_pomodoroSecondsRemaining.value <= 0 && _isPomodoroRunning.value) {
                onPomodoroCompleted()
            }
        }
    }

    private fun pauseTimer() {
        _isPomodoroRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        stopTimer()
        _pomodoroSecondsRemaining.value = _pomodoroDurationMinutes.value * 60
    }

    private fun stopTimer() {
        _isPomodoroRunning.value = false
        timerJob?.cancel()
    }

    private fun onPomodoroCompleted() {
        stopTimer()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
        } catch (e: Exception) {
            // Audio fallback
        }

        val completedMinutes = _pomodoroDurationMinutes.value
        val todayStr = sdf.format(Date())
        viewModelScope.launch {
            repository.logPomodoroSession(
                PomodoroSessionEntity(
                    dateStr = todayStr,
                    courseName = _pomodoroCourseTag.value.ifBlank { "General Study" },
                    topic = _pomodoroTopic.value.ifBlank { "Deep Focus Session" },
                    durationMinutes = completedMinutes,
                    sessionType = _pomodoroMode.value.name,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        if (_pomodoroMode.value == PomodoroMode.WORK) {
            _pomodoroCycles.value += 1
            _uiMessage.value = "Great focus! 25-minute Pomodoro completed 🎉"
            // Auto switch to short break
            setPomodoroMode(PomodoroMode.SHORT_BREAK)
        } else {
            _uiMessage.value = "Break finished! Ready to resume deep work?"
            setPomodoroMode(PomodoroMode.WORK)
        }
    }

    // --- Flashcards ---

    fun openDeck(deckId: Long) {
        _activeDeckId.value = deckId
        _currentCardIndex.value = 0
    }

    fun closeDeck() {
        _activeDeckId.value = null
        _currentCardIndex.value = 0
    }

    fun markCardMastery(cardId: Long, isMastered: Boolean) {
        val deckId = _activeDeckId.value ?: return
        viewModelScope.launch {
            repository.updateFlashcardMastery(deckId, cardId, isMastered)
            val cards = activeDeckCards.value
            if (_currentCardIndex.value < cards.size - 1) {
                _currentCardIndex.value += 1
            }
        }
    }

    fun nextCard() {
        val cards = activeDeckCards.value
        if (_currentCardIndex.value < cards.size - 1) {
            _currentCardIndex.value += 1
        }
    }

    fun prevCard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
        }
    }

    fun generateAIFlashcards(
        title: String,
        courseName: String,
        topicOrNotes: String,
        count: Int
    ) {
        viewModelScope.launch {
            _isGeneratingFlashcards.value = true
            try {
                val deckId = repository.generateAndSaveFlashcardDeck(title, courseName, topicOrNotes, count)
                _activeDeckId.value = deckId
                _currentCardIndex.value = 0
                _uiMessage.value = "Generated $count AI Flashcards!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to generate flashcards: ${e.localizedMessage}"
            } finally {
                _isGeneratingFlashcards.value = false
            }
        }
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
            if (_activeDeckId.value == deckId) {
                _activeDeckId.value = null
            }
            _uiMessage.value = "Flashcard deck deleted"
        }
    }

    // --- Quizzes ---

    fun openQuiz(quizId: Long) {
        _activeQuizId.value = quizId
        _currentQuizIndex.value = 0
    }

    fun closeQuiz() {
        _activeQuizId.value = null
        _currentQuizIndex.value = 0
    }

    fun answerQuizQuestion(questionId: Long, selectedIndex: Int) {
        viewModelScope.launch {
            repository.answerQuizQuestion(questionId, selectedIndex)
        }
    }

    fun finishQuiz(quizId: Long, score: Int) {
        viewModelScope.launch {
            repository.completeQuiz(quizId, score)
            _uiMessage.value = "Quiz completed! Score: $score / 5"
        }
    }

    fun generateAIQuiz(
        title: String,
        topicOrNotes: String,
        courseName: String
    ) {
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            try {
                val quizId = repository.generateAndSaveQuiz(title, topicOrNotes, courseName)
                _activeQuizId.value = quizId
                _currentQuizIndex.value = 0
                _uiMessage.value = "5-Question Practice Quiz Ready!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to build quiz: ${e.localizedMessage}"
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    fun deleteQuiz(quizId: Long) {
        viewModelScope.launch {
            repository.deleteQuiz(quizId)
            if (_activeQuizId.value == quizId) {
                _activeQuizId.value = null
            }
            _uiMessage.value = "Quiz removed"
        }
    }

    // --- Concept Summarizer ---

    fun generateAISummary(textOrTopic: String) {
        viewModelScope.launch {
            _isGeneratingSummary.value = true
            try {
                val summary = repository.generateAndSaveSummary(textOrTopic)
                _latestSummary.value = summary
                _uiMessage.value = "Concept synthesized successfully!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to summarize: ${e.localizedMessage}"
            } finally {
                _isGeneratingSummary.value = false
            }
        }
    }

    fun deleteSummary(id: Long) {
        viewModelScope.launch {
            repository.deleteSummary(id)
            _uiMessage.value = "Summary removed"
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        toneGenerator?.release()
    }
}
