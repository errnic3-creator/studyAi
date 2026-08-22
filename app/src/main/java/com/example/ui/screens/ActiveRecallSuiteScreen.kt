package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RecallSubTab
import com.example.ui.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRecallSuiteScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSubTab by viewModel.recallSubTab.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        // Sub-Tab Navigation Bar
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            RecallSubTab.values().forEach { tab ->
                Tab(
                    selected = selectedSubTab == tab,
                    onClick = { viewModel.setRecallSubTab(tab) },
                    text = { Text(tab.label) },
                    icon = {
                        when (tab) {
                            RecallSubTab.FLASHCARDS -> Icon(Icons.Default.Style, contentDescription = null)
                            RecallSubTab.QUIZ -> Icon(Icons.Default.Quiz, contentDescription = null)
                            RecallSubTab.SUMMARIZER -> Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        }
                    }
                )
            }
        }

        when (selectedSubTab) {
            RecallSubTab.FLASHCARDS -> FlashcardDeckScreen(viewModel = viewModel)
            RecallSubTab.QUIZ -> QuizBuilderScreen(viewModel = viewModel)
            RecallSubTab.SUMMARIZER -> ConceptSummarizerScreen(viewModel = viewModel)
        }
    }
}
