package com.example.data.repository

import com.example.data.local.StudyDatabase
import com.example.data.model.*
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StudyRepository(private val database: StudyDatabase) {

    private val courseDao = database.courseDao()
    private val studyBlockDao = database.studyBlockDao()
    private val flashcardDao = database.flashcardDao()
    private val quizDao = database.quizDao()
    private val pomodoroDao = database.pomodoroDao()
    private val summaryDao = database.conceptSummaryDao()

    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val allStudyBlocks: Flow<List<StudyBlockEntity>> = studyBlockDao.getAllStudyBlocks()
    val allDecks: Flow<List<FlashcardDeckEntity>> = flashcardDao.getAllDecks()
    val allQuizzes: Flow<List<QuizEntity>> = quizDao.getAllQuizzes()
    val allPomodoroSessions: Flow<List<PomodoroSessionEntity>> = pomodoroDao.getAllSessions()
    val allSummaries: Flow<List<ConceptSummaryEntity>> = summaryDao.getAllSummaries()

    fun getStudyBlocksForDate(dateStr: String): Flow<List<StudyBlockEntity>> {
        return studyBlockDao.getStudyBlocksForDate(dateStr)
    }

    fun getFlashcardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> {
        return flashcardDao.getFlashcardsForDeck(deckId)
    }

    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>> {
        return quizDao.getQuestionsForQuiz(quizId)
    }

    suspend fun insertCourse(course: CourseEntity): Long = withContext(Dispatchers.IO) {
        courseDao.insertCourse(course)
    }

    suspend fun deleteCourse(id: Long) = withContext(Dispatchers.IO) {
        courseDao.deleteCourseById(id)
    }

    suspend fun toggleStudyBlock(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        val completedAt = if (completed) System.currentTimeMillis() else 0L
        studyBlockDao.toggleBlockCompletion(id, completed, completedAt)
    }

    suspend fun rescheduleBlock(block: StudyBlockEntity, newDateStr: String, newSlot: String) = withContext(Dispatchers.IO) {
        val updated = block.copy(dateStr = newDateStr, timeSlot = newSlot)
        studyBlockDao.updateStudyBlock(updated)
    }

    suspend fun insertStudyBlock(block: StudyBlockEntity): Long = withContext(Dispatchers.IO) {
        studyBlockDao.insertStudyBlock(block)
    }

    suspend fun deleteStudyBlock(id: Long) = withContext(Dispatchers.IO) {
        studyBlockDao.deleteStudyBlockById(id)
    }

    suspend fun generateAndSaveAISchedule(
        courses: List<CourseEntity>,
        freeHours: Float,
        peakTime: String,
        extraNotes: String
    ): GeneratedScheduleResult = withContext(Dispatchers.IO) {
        val result = GeminiStudyService.generateStudySchedule(courses, freeHours, peakTime, extraNotes)
        if (result.blocks.isNotEmpty()) {
            studyBlockDao.clearAllStudyBlocks()
            studyBlockDao.insertStudyBlocks(result.blocks)
        }
        result
    }

    suspend fun generateAndSaveFlashcardDeck(
        title: String,
        courseName: String,
        topicOrNotes: String,
        cardCount: Int
    ): Long = withContext(Dispatchers.IO) {
        val cards = GeminiStudyService.generateFlashcards(topicOrNotes, cardCount)
        val deckId = flashcardDao.insertDeck(
            FlashcardDeckEntity(
                title = title.ifBlank { "Flashcard Deck: ${topicOrNotes.take(25)}" },
                courseName = courseName.ifBlank { "General" },
                description = "AI Generated deck on $topicOrNotes",
                cardCount = cards.size,
                masteredCount = 0
            )
        )
        val flashcardEntities = cards.map {
            FlashcardEntity(
                deckId = deckId,
                question = it.question,
                answer = it.answer,
                keyConcept = it.keyConcept,
                mnemonic = it.mnemonic
            )
        }
        flashcardDao.insertFlashcards(flashcardEntities)
        deckId
    }

    suspend fun updateFlashcardMastery(deckId: Long, cardId: Long, isMastered: Boolean) = withContext(Dispatchers.IO) {
        flashcardDao.updateCardMastery(cardId, isMastered)
        flashcardDao.refreshDeckMasteryCount(deckId)
    }

    suspend fun deleteDeck(deckId: Long) = withContext(Dispatchers.IO) {
        flashcardDao.deleteCardsForDeck(deckId)
        flashcardDao.deleteDeckById(deckId)
    }

    suspend fun generateAndSaveQuiz(
        title: String,
        topicOrNotes: String,
        courseName: String
    ): Long = withContext(Dispatchers.IO) {
        val quizItems = GeminiStudyService.generateQuiz(topicOrNotes, 5)
        val quizId = quizDao.insertQuiz(
            QuizEntity(
                title = title.ifBlank { "Quiz: ${topicOrNotes.take(30)}" },
                topic = topicOrNotes,
                courseName = courseName.ifBlank { "General" },
                totalQuestions = quizItems.size,
                isCompleted = false
            )
        )
        val questions = quizItems.mapIndexed { index, item ->
            QuizQuestionEntity(
                quizId = quizId,
                questionNumber = index + 1,
                question = item.question,
                optionA = item.options.getOrElse(0) { "Option A" },
                optionB = item.options.getOrElse(1) { "Option B" },
                optionC = item.options.getOrElse(2) { "Option C" },
                optionD = item.options.getOrElse(3) { "Option D" },
                correctAnswerIndex = item.correctIndex,
                explanation = item.explanation,
                selectedAnswerIndex = -1
            )
        }
        quizDao.insertQuizQuestions(questions)
        quizId
    }

    suspend fun answerQuizQuestion(questionId: Long, selectedIndex: Int) = withContext(Dispatchers.IO) {
        quizDao.answerQuestion(questionId, selectedIndex)
    }

    suspend fun completeQuiz(quizId: Long, score: Int) = withContext(Dispatchers.IO) {
        quizDao.finishQuiz(quizId, score)
    }

    suspend fun deleteQuiz(quizId: Long) = withContext(Dispatchers.IO) {
        quizDao.deleteQuizById(quizId)
    }

    suspend fun logPomodoroSession(session: PomodoroSessionEntity) = withContext(Dispatchers.IO) {
        pomodoroDao.insertSession(session)
    }

    suspend fun generateAndSaveSummary(
        topicOrText: String
    ): GeneratedSummaryResult = withContext(Dispatchers.IO) {
        val result = GeminiStudyService.summarizeConcept(topicOrText)
        val takeawaysStr = result.keyTakeaways.joinToString("\n• ") { it }
        val actionItemsStr = result.actionItems.joinToString("\n✓ ") { it }
        summaryDao.insertSummary(
            ConceptSummaryEntity(
                title = result.title,
                topic = topicOrText.take(50),
                originalText = topicOrText,
                summaryText = result.summary,
                keyTakeaways = "• $takeawaysStr",
                actionItems = "✓ $actionItemsStr",
                memoryAnchors = result.memoryAnchors
            )
        )
        result
    }

    suspend fun deleteSummary(id: Long) = withContext(Dispatchers.IO) {
        summaryDao.deleteSummaryById(id)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
        val tomorrow = sdf.format(cal1.time)
        val dayAfter = sdf.format(cal2.time)

        // Seed Courses
        val csCourse = CourseEntity(
            name = "Data Structures & Algorithms",
            code = "CS 201",
            colorHex = "#6366F1",
            targetGrade = "A",
            examDate = "2026-09-02",
            daysLeft = 8,
            topics = "Binary Search Trees, Heaps, Graph BFS/DFS, Dynamic Programming",
            priority = "HIGH"
        )
        val chemCourse = CourseEntity(
            name = "Organic Chemistry II",
            code = "CHEM 220",
            colorHex = "#06B6D4",
            targetGrade = "A-",
            examDate = "2026-09-08",
            daysLeft = 14,
            topics = "Aldol Condensation, Carboxylic Acid Derivatives, Proton NMR",
            priority = "HIGH"
        )
        val econCourse = CourseEntity(
            name = "Microeconomics",
            code = "ECON 102",
            colorHex = "#10B981",
            targetGrade = "A",
            examDate = "2026-09-14",
            daysLeft = 20,
            topics = "Nash Equilibrium, Oligopoly Models, Deadweight Loss",
            priority = "MEDIUM"
        )

        val csId = courseDao.insertCourse(csCourse)
        val chemId = courseDao.insertCourse(chemCourse)
        val econId = courseDao.insertCourse(econCourse)

        // Seed Study Blocks
        val blocks = listOf(
            StudyBlockEntity(
                courseId = csId,
                courseName = "Data Structures & Algorithms",
                courseColor = "#6366F1",
                title = "CS 201: Graph Traversal & Dijkstra",
                subtopics = "Shortest path algorithm analysis, adjacency list implementations.",
                dateStr = today,
                timeSlot = "09:00 AM - 10:15 AM",
                durationMinutes = 75,
                priority = "HIGH",
                technique = "Active Recall",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 3600000
            ),
            StudyBlockEntity(
                courseId = chemId,
                courseName = "Organic Chemistry II",
                courseColor = "#06B6D4",
                title = "CHEM 220: Aldol Reactions & Mechanisms",
                subtopics = "Enolate ion resonance, nucleophilic carbonyl attacks.",
                dateStr = today,
                timeSlot = "11:00 AM - 12:00 PM",
                durationMinutes = 60,
                priority = "HIGH",
                technique = "Feynman Technique",
                isCompleted = false
            ),
            StudyBlockEntity(
                courseId = econId,
                courseName = "Microeconomics",
                courseColor = "#10B981",
                title = "ECON 102: Game Theory & Nash Equilibrium",
                subtopics = "Prisoner's Dilemma matrix payoff calculations.",
                dateStr = today,
                timeSlot = "04:30 PM - 05:30 PM",
                durationMinutes = 60,
                priority = "MEDIUM",
                technique = "Practice Quiz",
                isCompleted = false
            ),
            StudyBlockEntity(
                courseId = csId,
                courseName = "Data Structures & Algorithms",
                courseColor = "#6366F1",
                title = "CS 201: Dynamic Programming Memoization",
                subtopics = "Knapsack 0/1 and Longest Common Subsequence recursion trees.",
                dateStr = tomorrow,
                timeSlot = "09:30 AM - 11:00 AM",
                durationMinutes = 90,
                priority = "HIGH",
                technique = "Active Recall",
                isCompleted = false
            ),
            StudyBlockEntity(
                courseId = chemId,
                courseName = "Organic Chemistry II",
                courseColor = "#06B6D4",
                title = "CHEM 220: Proton NMR Multiplicity",
                subtopics = "Splitting patterns, chemical shifts (ppm), integration curves.",
                dateStr = dayAfter,
                timeSlot = "02:00 PM - 03:15 PM",
                durationMinutes = 75,
                priority = "HIGH",
                technique = "Flashcards Drill",
                isCompleted = false,
                isMilestone = true
            )
        )
        studyBlockDao.insertStudyBlocks(blocks)

        // Seed Flashcard Deck
        val deckId = flashcardDao.insertDeck(
            FlashcardDeckEntity(
                title = "Cognitive Psychology & Learning Science",
                courseName = "Learning Strategies",
                description = "High-retention active recall drills on spaced repetition and mental models.",
                cardCount = 5,
                masteredCount = 2
            )
        )
        flashcardDao.insertFlashcards(
            listOf(
                FlashcardEntity(
                    deckId = deckId,
                    question = "What is the Spacing Effect in cognitive learning?",
                    answer = "Information is remembered significantly better when study sessions are spaced out over time rather than massed together in one cramming session.",
                    keyConcept = "Distributed Practice",
                    mnemonic = "Space your repetitions to space-proof your memory.",
                    isMastered = true
                ),
                FlashcardEntity(
                    deckId = deckId,
                    question = "How does Active Retrieval strengthen synaptic pathways?",
                    answer = "Forcing the brain to retrieve information from memory reconstructs neural pathways and signals that the knowledge is critical to keep.",
                    keyConcept = "Synaptic Plasticity",
                    mnemonic = "Retrieval = Brain lifting weights; Reading = Watching gym videos.",
                    isMastered = true
                ),
                FlashcardEntity(
                    deckId = deckId,
                    question = "What are the 4 core steps of the Feynman Technique?",
                    answer = "1. Choose a concept. 2. Teach it to a 10-year-old in plain language. 3. Identify knowledge gaps. 4. Simplify and refine with analogies.",
                    keyConcept = "Simplification Method",
                    mnemonic = "F-E-Y-N: Frame, Explain, Yield gaps, Novel analogies.",
                    isMastered = false
                ),
                FlashcardEntity(
                    deckId = deckId,
                    question = "What is the Zeigarnik Effect in task productivity?",
                    answer = "The psychological tendency for uncompleted or interrupted tasks to occupy cognitive focus more intensely than finished tasks.",
                    keyConcept = "Task Closure Tension",
                    mnemonic = "Zeigarnik = Z-open loops keep brain awake.",
                    isMastered = false
                ),
                FlashcardEntity(
                    deckId = deckId,
                    question = "Why does Interleaving outperform Blocked Practice in math and science?",
                    answer = "Interleaving forces the brain to classify problem types and choose strategies rather than executing repetitive algorithmic steps automatically.",
                    keyConcept = "Discriminative Contrast",
                    mnemonic = "Shuffle your deck to sharpen your wits.",
                    isMastered = false
                )
            )
        )

        // Seed Initial Quiz
        val quizId = quizDao.insertQuiz(
            QuizEntity(
                title = "Cognitive Science & Study Tactics Diagnostic",
                topic = "Active Recall & Spaced Repetition",
                courseName = "Study Tactics",
                score = 4,
                totalQuestions = 5,
                isCompleted = true
            )
        )
        quizDao.insertQuizQuestions(
            listOf(
                QuizQuestionEntity(
                    quizId = quizId,
                    questionNumber = 1,
                    question = "Which study technique provides the highest long-term retention according to Dunlosky et al. (2013)?",
                    optionA = "Summarizing textbook chapters into notes",
                    optionB = "Practice testing and distributed practice",
                    optionC = "Highlighting key sentences with bright markers",
                    optionD = "Re-reading notes multiple times",
                    correctAnswerIndex = 1,
                    explanation = "Practice testing and spaced distribution are rated with highest empirical utility for retention.",
                    selectedAnswerIndex = 1
                ),
                QuizQuestionEntity(
                    quizId = quizId,
                    questionNumber = 2,
                    question = "What is the primary cognitive mechanism behind the Feynman Technique?",
                    optionA = "Memorizing definitions word-for-word",
                    optionB = "Simplifying ideas to expose illusion of competence",
                    optionC = "Writing at least 2000 words per session",
                    optionD = "Studying with classical music in the background",
                    correctAnswerIndex = 1,
                    explanation = "Translating concepts into plain language strips away false comprehension.",
                    selectedAnswerIndex = 1
                ),
                QuizQuestionEntity(
                    quizId = quizId,
                    questionNumber = 3,
                    question = "What is the standard recommended Pomodoro interval cycle?",
                    optionA = "10 min work, 10 min break",
                    optionB = "25 min work, 5 min break; 15-30 min break after 4 cycles",
                    optionC = "60 min work, 30 min break",
                    optionD = "90 min continuous work with no breaks",
                    correctAnswerIndex = 1,
                    explanation = "The classic 25/5 rhythm optimizes dopamine and prefrontal cortex sustained attention.",
                    selectedAnswerIndex = 1
                ),
                QuizQuestionEntity(
                    quizId = quizId,
                    questionNumber = 4,
                    question = "When should the second repetition of a difficult flashcard occur?",
                    optionA = "1 month later",
                    optionB = "Within 24 to 48 hours before exponential memory decay",
                    optionC = "Never review mastered cards",
                    optionD = "Only on the day of the final exam",
                    correctAnswerIndex = 1,
                    explanation = "Combatting the early steep slope of the Ebbinghaus curve yields maximum long-term consolidation.",
                    selectedAnswerIndex = 1
                ),
                QuizQuestionEntity(
                    quizId = quizId,
                    questionNumber = 5,
                    question = "What is 'Interleaving' in problem solving?",
                    optionA = "Practicing different related problem categories in mixed sequences",
                    optionB = "Doing 100 identical quadratic equations in a row",
                    optionC = "Reading 5 books simultaneously",
                    optionD = "Alternating between studying and sleeping every 15 minutes",
                    correctAnswerIndex = 0,
                    explanation = "Mixing problem types builds categorization skills and prevents mechanical autopilot.",
                    selectedAnswerIndex = 0
                )
            )
        )

        // Seed Pomodoro Session Logs
        pomodoroDao.insertSession(
            PomodoroSessionEntity(
                dateStr = today,
                courseName = "Data Structures & Algorithms",
                topic = "Graph BFS & Dijkstra Drills",
                durationMinutes = 25,
                sessionType = "WORK",
                completedAt = System.currentTimeMillis() - 7200000
            )
        )
        pomodoroDao.insertSession(
            PomodoroSessionEntity(
                dateStr = today,
                courseName = "Organic Chemistry II",
                topic = "Aldol Condensation Mechanism",
                durationMinutes = 25,
                sessionType = "WORK",
                completedAt = System.currentTimeMillis() - 3600000
            )
        )

        // Seed Concept Summary
        summaryDao.insertSummary(
            ConceptSummaryEntity(
                title = "Dynamic Programming Paradigm",
                topic = "Computer Science / Algorithms",
                originalText = "Dynamic programming is both a mathematical optimization method and a computer programming method. It breaks complex problems down into simpler subproblems and stores results to prevent redundant calculations.",
                summaryText = "Dynamic Programming solves optimization problems by exploiting overlapping subproblems and optimal substructure, caching sub-solutions via memoization (top-down) or tabulation (bottom-up).",
                keyTakeaways = "• Optimal Substructure: Global optimal solution is built from optimal solutions to subproblems.\n• Overlapping Subproblems: The same subproblems are solved repeatedly.\n• Top-Down Memoization: Recursive with cache dictionary.\n• Bottom-Up Tabulation: Iterative table filling from base cases.",
                actionItems = "✓ Identify state transition recurrence formula.\n✓ Check base case initializations.\n✓ Analyze time complexity vs spatial footprint (O(N) vs O(1) space optimization).",
                memoryAnchors = "DP = Divide -> Memoize -> Tabulate. Never calculate the same sub-tree twice!"
            )
        )
    }
}
