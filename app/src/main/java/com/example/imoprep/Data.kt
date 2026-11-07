package com.example.imoprep

import android.content.Context
import org.json.JSONArray

const val SEC_LOGICAL = "Logical Reasoning"
const val SEC_MATH_REASONING = "Mathematical Reasoning"
const val SEC_EVERYDAY = "Everyday Mathematics"
const val SEC_ACHIEVERS = "Achievers Section"

// Chapter lists taken from the chapterwise analysis of the IMO Class 7 book.
val LOGICAL_CHAPTERS = listOf(
    "Series Completion and Inserting",
    "Analogy and Classification",
    "Coding-Decoding",
    "Blood Relations",
    "Direction Sense Test",
    "Logical Venn Diagrams",
    "Alpha-Numeric Sequence",
    "Mathematical Operations",
    "Analytical Reasoning",
    "Mirror and Water Images",
    "Embedded Figures",
    "Figure Matrix",
    "Paper Folding and Paper Cutting",
    "Cubes and Dice",
    "Dot Situation",
    "Clock and Calendar",
)

val MATH_CHAPTERS = listOf(
    "Integers",
    "Fractions and Decimals",
    "Data Handling",
    "Simple Equations",
    "Lines and Angles",
    "The Triangle and Its Properties",
    "Congruence of Triangles",
    "Comparing Quantities",
    "Rational Numbers",
    "Practical Geometry",
    "Perimeter and Area",
    "Algebraic Expressions",
    "Exponents and Powers",
    "Symmetry",
    "Visualising Solid Shapes",
    "Amalgam",
)

data class Question(
    val id: String,
    val section: String,
    val chapter: String,
    val text: String,
    val options: List<String>,
    val answer: Int, // index into options (0 = A)
    val explanation: String,
)

/** Reads assets/questions.json. Add new papers there; see README. */
fun loadQuestions(context: Context): List<Question> {
    val raw = context.assets.open("questions.json").bufferedReader().use { it.readText() }
    val items = JSONArray(raw)
    return (0 until items.length()).map { i ->
        val o = items.getJSONObject(i)
        val opts = o.getJSONArray("options")
        Question(
            id = o.getString("id"),
            section = o.getString("section"),
            chapter = o.getString("chapter"),
            text = o.getString("text"),
            options = (0 until opts.length()).map { opts.getString(it) },
            answer = o.getString("answer").first() - 'A',
            explanation = o.optString("explanation", ""),
        )
    }
}

/** Syllabus chapters first, then any extra chapter that only exists in the question bank. */
fun allChapters(questions: List<Question>): List<String> =
    (LOGICAL_CHAPTERS + MATH_CHAPTERS + questions.map { it.chapter }).distinct()
