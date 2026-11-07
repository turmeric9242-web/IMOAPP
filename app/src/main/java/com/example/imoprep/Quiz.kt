package com.example.imoprep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Runs a list of questions one at a time with instant feedback.
 * If [timeLimitSeconds] is set, the quiz ends when the timer reaches zero.
 */
@Composable
fun QuizRunner(
    title: String,
    questions: List<Question>,
    timeLimitSeconds: Int?,
    onBack: () -> Unit,
    onFinished: (score: Int, total: Int) -> Unit,
) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var remaining by remember { mutableIntStateOf(timeLimitSeconds ?: 0) }

    LaunchedEffect(Unit) {
        if (timeLimitSeconds == null || questions.isEmpty()) return@LaunchedEffect
        while (!finished && remaining > 0) {
            delay(1000L)
            remaining -= 1
        }
        finished = true
    }

    if (questions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("No questions added for this topic yet.")
            Button(onClick = onBack) { Text("Back") }
        }
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
            if (timeLimitSeconds != null && remaining <= 0) {
                Text("Time is up.")
            }
            Button(onClick = { onFinished(score, questions.size) }) {
                Text("Save and close")
            }
        }
        return
    }

    val q = questions[index]
    val chosen = selected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.Bold)
            if (timeLimitSeconds != null) {
                Text(
                    "Time %02d:%02d".format(remaining / 60, remaining % 60),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

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
            val prefix = when {
                chosen == null -> ""
                i == q.answer -> "✓ "
                i == chosen -> "✗ "
                else -> ""
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
                Text("$prefix(${'A' + i}) $option")
            }
        }

        if (chosen != null) {
            Text(
                if (chosen == q.answer) {
                    "Correct!"
                } else {
                    "Not quite. The answer is (${'A' + q.answer})."
                },
                fontWeight = FontWeight.Bold,
            )
            if (q.explanation.isNotBlank()) {
                Text(q.explanation)
            }
            val isLast = index == questions.size - 1
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
