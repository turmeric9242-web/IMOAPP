package com.example.imoprep

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.imoprep.ui.theme.Brand
import com.example.imoprep.ui.theme.BrandDark
import com.example.imoprep.ui.theme.Violet

sealed interface Screen {
    data object Home : Screen
    data object Notes : Screen
    data object Syllabus : Screen
    data object ChapterPick : Screen
    data class Chapter(val name: String) : Screen
    data object Mock : Screen
    data object Progress : Screen
}

@Composable
fun ImoPrepApp() {
    val context = LocalContext.current
    val questions = remember { loadQuestions(context) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    BackHandler(enabled = screen != Screen.Home) {
        screen = when (screen) {
            is Screen.Chapter -> Screen.ChapterPick
            else -> Screen.Home
        }
    }

    val inQuiz = screen is Screen.Chapter || screen == Screen.Mock

    Scaffold(
        bottomBar = {
            if (!inQuiz) {
                NavigationBar {
                    NavTab("Home", "🏠", selected = screen == Screen.Home || screen == Screen.Notes) {
                        screen = Screen.Home
                    }
                    NavTab("Syllabus", "📚", selected = screen == Screen.Syllabus) {
                        screen = Screen.Syllabus
                    }
                    NavTab("Quiz", "✏️", selected = screen == Screen.ChapterPick) {
                        screen = Screen.ChapterPick
                    }
                    NavTab("Mock", "⏱", selected = false) {
                        screen = Screen.Mock
                    }
                    NavTab("Progress", "📊", selected = screen == Screen.Progress) {
                        screen = Screen.Progress
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val s = screen) {
                Screen.Home -> HomeScreen(onNavigate = { screen = it })
                Screen.Notes -> NotesScreen()
                Screen.Syllabus -> SyllabusScreen()
                Screen.ChapterPick -> ChapterPickScreen(
                    questions = questions,
                    onPick = { screen = Screen.Chapter(it) },
                )
                is Screen.Chapter -> ChapterQuiz(
                    title = s.name,
                    questions = questions.filter { it.chapter == s.name },
                    onBack = { screen = Screen.ChapterPick },
                    onFinished = { score, total ->
                        ProgressStore.record(context, "chapter:${s.name}", score, total)
                        screen = Screen.ChapterPick
                    },
                )
                Screen.Mock -> MockTest(
                    questions = questions,
                    // Same pace as the real paper: 60 minutes for 50 questions.
                    timeLimitSeconds = questions.size * 72,
                    onExit = { screen = Screen.Home },
                    onFinished = { score, total ->
                        ProgressStore.record(context, "mock", score, total)
                        screen = Screen.Progress
                    },
                )
                Screen.Progress -> ProgressScreen(questions = questions)
            }
        }
    }
}

@Composable
private fun RowScope.NavTab(label: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(icon) },
        label = { Text(label) },
    )
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Gradient hero, like the website's header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(BrandDark, Brand, Violet)))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "IMO Class 7 Prep",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                "Self-study practice for the SOF International Mathematics Olympiad. Unofficial helper.",
                color = Color.White,
            )
            Text(
                "50 questions · 60 marks · 1 hour",
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Exam pattern", fontWeight = FontWeight.Bold)
                Text("Logical Reasoning (15) · Mathematical Reasoning (20) · Everyday Mathematics (10) · Achievers (5)")
            }
        }

        MenuCard("Syllabus", "Chapters for every section") { onNavigate(Screen.Syllabus) }
        MenuCard("Study Notes", "Quick tips for common question types") { onNavigate(Screen.Notes) }
        MenuCard("Chapter Quiz", "Practise one chapter at a time") { onNavigate(Screen.ChapterPick) }
        MenuCard("Full Mock Test", "All questions with a timer") { onNavigate(Screen.Mock) }
        MenuCard("My Progress", "Scores saved on this phone") { onNavigate(Screen.Progress) }
    }
}

@Composable
fun MenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val STUDY_NOTES = listOf(
    "Coding-decoding: write the letters side by side and check the shift for each position. Many codes repeat a shift pattern.",
    "'A fraction of a quantity' means multiply. Example: 3/7 of 147 = 63.",
    "Average (mean) = sum of the values divided by how many values there are. Use it to find a missing value.",
    "When comparing two big calculations, work out each side fully before choosing <, > or =.",
    "Odd multiples of 3 are 3, 9, 15, 21, ... Count only the ones inside the range the question gives.",
    "For figure questions, check rotation, mirror images and counts of dots or sides before choosing.",
)

@Composable
fun NotesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Study Notes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        STUDY_NOTES.forEach { note ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(note, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun SyllabusScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Syllabus", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Chapters from your chapterwise analysis.")

        Text("Logical Reasoning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LOGICAL_CHAPTERS.forEachIndexed { i, name -> Text("${i + 1}. $name") }

        Text("Mathematics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        MATH_CHAPTERS.forEachIndexed { i, name -> Text("${i + 1}. $name") }
    }
}

@Composable
fun ChapterPickScreen(questions: List<Question>, onPick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Chapter Quiz", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Pick a chapter. You get instant feedback after each answer.")

        allChapters(questions).forEach { name ->
            val count = questions.count { it.chapter == name }
            Card(
                onClick = { onPick(name) },
                enabled = count > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(name, fontWeight = FontWeight.Bold)
                    Text(if (count > 0) "$count question(s) ready" else "No questions added yet")
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(questions: List<Question>) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("My Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Results are saved on this phone only.")

        val rows = listOf("mock" to "Full Mock Test") +
            allChapters(questions).map { "chapter:$it" to it }

        rows.forEach { (key, label) ->
            val stat = ProgressStore.read(context, key)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(label, fontWeight = FontWeight.Bold)
                    if (stat == null) {
                        Text("Not attempted yet")
                    } else {
                        Text("Best: ${stat.best} / ${stat.total}  ·  Last: ${stat.last} / ${stat.total}")
                        Text("Attempts: ${stat.attempts}")
                    }
                }
            }
        }
    }
}
