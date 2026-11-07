package com.example.imoprep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.imoprep.ui.theme.Danger
import com.example.imoprep.ui.theme.Mint
import kotlinx.coroutines.delay

private fun letter(index: Int): String = ('A' + index).toString()

@Composable
private fun EmptyTopic(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("No questions added for this topic yet.")
        Button(onClick = onBack) { Text("Back") }
    }
}

/** Chapter quiz: one question at a time, with the answer and explanation shown straight away. */
@Composable
fun ChapterQuiz(
    title: String,
    questions: List<Question>,
    onBack: () -> Unit,
    onFinished: (score: Int, total: Int) -> Unit,
) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        EmptyTopic(title, onBack)
        return
    }

    if (finished) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Result", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title)
            Text(
                "Score: $score / ${questions.size}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Button(onClick = { onFinished(score, questions.size) }) { Text("Save and close") }
        }
        return
    }

    val q = questions[index]
    val chosen = selected
    val isLast = index == questions.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { (index + 1f) / questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Question ${index + 1} of ${questions.size} · ${q.section}",
            style = MaterialTheme.typography.labelLarge,
        )
        Text(q.text, style = MaterialTheme.typography.bodyLarge)

        q.options.forEachIndexed { i, option ->
            val tint = when {
                chosen == null -> Color.Unspecified
                i == q.answer -> Mint
                i == chosen -> Danger
                else -> Color.Unspecified
            }
            OutlinedButton(
                onClick = {
                    if (selected == null) {
                        selected = i
                        if (i == q.answer) score += 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("(${letter(i)}) $option", color = tint)
            }
        }

        if (chosen != null) {
            Text(
                if (chosen == q.answer) "Correct!" else "Not quite. The answer is (${letter(q.answer)}).",
                fontWeight = FontWeight.Bold,
                color = if (chosen == q.answer) Mint else Danger,
            )
            if (q.explanation.isNotBlank()) {
                Text(q.explanation)
            }
            Button(
                onClick = {
                    if (isLast) {
                        finished = true
                    } else {
                        index += 1
                        selected = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isLast) "See result" else "Next")
            }
        }
    }
}

/**
 * Full mock test, like the practice site: answers are hidden until you submit,
 * you can move back and forth, and the paper submits itself when time runs out.
 */
@Composable
fun MockTest(
    questions: List<Question>,
    timeLimitSeconds: Int,
    onExit: () -> Unit,
    onFinished: (score: Int, total: Int) -> Unit,
) {
    val answers = remember {
        mutableStateListOf<Int?>().also { list -> repeat(questions.size) { list.add(null) } }
    }
    var index by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableIntStateOf(timeLimitSeconds) }
    var submitted by remember { mutableStateOf(false) }
    var confirmOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!submitted && remaining > 0) {
            delay(1000L)
            remaining -= 1
        }
        submitted = true
    }

    if (questions.isEmpty()) {
        EmptyTopic("Full Mock Test", onExit)
        return
    }

    if (submitted) {
        MockResult(
            questions = questions,
            answers = answers,
            timedOut = remaining <= 0,
            onFinished = onFinished,
        )
        return
    }

    val q = questions[index]
    val unanswered = answers.count { it == null }

    if (confirmOpen) {
        AlertDialog(
            onDismissRequest = { confirmOpen = false },
            title = { Text("Submit paper?") },
            text = {
                Text(
                    if (unanswered == 0) {
                        "All questions are answered."
                    } else {
                        "$unanswered question(s) are still unanswered. There is no negative marking."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmOpen = false
                    submitted = true
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOpen = false }) { Text("Keep working") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Full Mock Test", fontWeight = FontWeight.Bold)
            Text(
                "Time %02d:%02d".format(remaining / 60, remaining % 60),
                fontWeight = FontWeight.ExtraBold,
            )
        }
        LinearProgressIndicator(
            progress = { (index + 1f) / questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Question ${index + 1} of ${questions.size} · ${q.section}",
            style = MaterialTheme.typography.labelLarge,
        )
        Text("Answered: ${questions.size - unanswered} / ${questions.size}")
        Text(q.text, style = MaterialTheme.typography.bodyLarge)

        q.options.forEachIndexed { i, option ->
            val label = "(${letter(i)}) $option"
            if (answers[index] == i) {
                Button(onClick = { answers[index] = i }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { answers[index] = i }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { index -= 1 }, enabled = index > 0) { Text("Previous") }
            if (index < questions.size - 1) {
                Button(onClick = { index += 1 }) { Text("Next") }
            } else {
                Button(onClick = { confirmOpen = true }) { Text("Submit paper") }
            }
        }

        TextButton(onClick = onExit) { Text("Leave test (not saved)") }
    }
}

@Composable
private fun MockResult(
    questions: List<Question>,
    answers: List<Int?>,
    timedOut: Boolean,
    onFinished: (score: Int, total: Int) -> Unit,
) {
    val score = questions.indices.count { answers[it] == questions[it].answer }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Paper result", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (timedOut) {
            Text("Time is up. The paper was submitted automatically.")
        }
        Text(
            "Score: $score / ${questions.size}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        questions.map { it.section }.distinct().forEach { section ->
            val inSection = questions.indices.filter { questions[it].section == section }
            val right = inSection.count { answers[it] == questions[it].answer }
            Text("$section: $right / ${inSection.size}")
        }

        Button(onClick = { onFinished(score, questions.size) }) { Text("Save and close") }

        Text("Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        questions.forEachIndexed { i, q ->
            val mine = answers[i]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Q${i + 1}. ${q.text}")
                    Text(
                        if (mine == null) {
                            "Your answer: skipped"
                        } else {
                            "Your answer: (${letter(mine)}) ${q.options[mine]}"
                        },
                        color = if (mine == q.answer) Mint else Danger,
                    )
                    Text("Correct: (${letter(q.answer)}) ${q.options[q.answer]}")
                    if (q.explanation.isNotBlank()) {
                        Text(q.explanation)
                    }
                }
            }
        }
    }
}
