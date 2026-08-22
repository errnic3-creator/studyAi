# StudyAI — AI-Powered Study Planner

An intelligent productivity mobile application designed for students to master complex coursework, automate study schedules, dynamically adapt to upcoming exam deadlines, and supercharge retention with active recall (AI flashcards, practice quizzes, and concept synthesizers).

---

## 📱 Key Features & Modules

### 1. Smart AI Scheduler
- **Intelligent Schedule Generator**: Integrates with Gemini 3.5 Flash to transform course lists, exam dates, daily free hours, and peak productivity windows (morning/night) into prioritized 5-day study blocks.
- **Dynamic Task Breakdown**: Automatically decomposes broad exam subjects into bite-sized daily action items with designated cognitive study techniques (*Active Recall*, *Feynman Technique*, *Practice Quizzes*, *Flashcards Drills*).
- **Exam Countdown Tracking**: Live badges tracking days remaining until each exam, target grade goals, and urgent priority markers.

### 2. Interactive Dashboard & Agenda
- **7-Day Calendar Strip**: Quick switching across days with visual indicators for scheduled study sessions.
- **Streak & Target System**: Dynamic streak counter with animated flame indicator, daily task completion percentage, and focus hours tracking.
- **Task Controls**: One-tap completion checkboxes, 1-day rescheduling, session deletion, and direct Pomodoro focus triggers.

### 3. Pomodoro Focus Mode
- **Animated Circular Arc Timer**: Visual countdown with breathing pulse effect and glowing gradients.
- **Interval Presets**: *Deep Work* (25 min), *Short Break* (5 min), and *Long Break* (15 min).
- **Course & Topic Tagging**: Tag study sessions to specific subjects with audio chime notification on completion.
- **Session History Log**: Local database persistence tracking all completed intervals and daily totals.

### 4. AI Active-Recall Suite
- **Interactive 3D Flip Flashcards**:
  - Gemini-powered flashcard generator from syllabus or raw lecture notes.
  - Realistic 3D Y-axis flip animation revealing answers, core concept anchors, and memory mnemonics.
  - "Needs Review" vs "Mastered" spaced repetition feedback loops with live mastery percentages.
- **AI Practice Quiz Builder**:
  - Generates 5-question multiple-choice diagnostic tests on any topic.
  - Instant visual feedback with calibrated correct/incorrect indications and comprehensive reasoning rationales.
- **Concept Synthesizer & Summarizer**:
  - Distills lengthy textbook excerpts and lecture notes into Executive Summaries, Key Takeaways, Actionable Tasks, and Memory Anchors.

### 5. Study Velocity & Analytics
- **Weekly Study Velocity Bar Chart**: Compares daily logged focus hours against target benchmarks.
- **Subject Preparedness**: Progress indicators for every enrolled course based on completed study sessions.

---

## 🛠️ Architecture & Tech Stack

- **Framework**: Android / Kotlin / Jetpack Compose
- **Design System**: Material Design 3 (M3) with tailored Indigo, Cyan, and Emerald palettes supporting Light & Dark themes
- **Architecture**: Clean MVVM (Model-View-ViewModel) with Kotlin Coroutines and StateFlow
- **Local Persistence**: Android Room Database (`StudyDatabase`) with DAOs for Courses, Study Blocks, Flashcards, Quizzes, Pomodoro Sessions, and Concept Summaries
- **AI Integration**: Gemini 3.5 Flash (`gemini-2.5-flash`) REST API via OkHttp with intelligent fallback generators
- **Security**: Android Secrets Gradle plugin with `BuildConfig` API key injection

---

## 🚀 Setup & API Configuration

1. **Gemini API Key Setup**:
   Add your Gemini API Key in Google AI Studio or in the root `.env` file:
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
2. **Build and Run**:
   ```bash
   gradle assembleDebug
   ```
