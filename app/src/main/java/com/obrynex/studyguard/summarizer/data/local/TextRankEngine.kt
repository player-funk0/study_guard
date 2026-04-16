package com.obrynex.studyguard.summarizer.data.local

import com.obrynex.studyguard.summarizer.domain.model.SummaryLevel
import kotlin.math.sqrt

/**
 * TextRank extractive summarizer — supports both Arabic and English.
 *
 * Builds a sentence-similarity graph using cosine similarity on TF token vectors,
 * then applies a PageRank-style power iteration to score each sentence.
 * Top-scoring sentences are returned in their original order.
 *
 * 100% local — no network, no external library.
 */
object TextRankEngine {

    private val STOP_WORDS = setOf(
        // English
        "a","an","the","is","it","its","in","on","at","to","for","of","and","or","but","not",
        "with","as","by","from","was","are","were","be","been","being","have","has","had",
        "do","does","did","will","would","could","should","may","might","shall","can",
        "that","this","these","those","i","you","he","she","we","they","my","your","his",
        "her","our","their","what","which","who","how","when","where","why","all","each",
        "every","both","few","more","most","other","some","such","than","then","so","if",
        "about","after","before","between","into","through","during","also","just","because",
        // Arabic
        "في","من","إلى","على","عن","مع","هذا","هذه","ذلك","التي","الذي","الذين",
        "وهو","وهي","وهم","كان","كانت","يكون","ان","أن","لا","لم","لن","قد",
        "هو","هي","هم","نحن","انت","أنت","و","أو","ثم","إذ","إذا","لكن",
        "بل","حتى","عند","بعد","قبل","أي","كل","بين","ما","هل","لقد",
        "كما","أيضا","حيث","منذ","عبر","خلال","حول","رغم","بدون","لأن",
        "التي","هذه","تلك","ولا","فلا","مما","عما","إنما","وإن","فإن"
    )

    fun summarize(text: String, level: SummaryLevel): String {
        val sentences = splitSentences(text)
        if (sentences.size <= 2) return text.trim()

        val targetCount = (sentences.size * level.ratio).toInt().coerceAtLeast(1)
        val vectors     = sentences.map { tokenVector(it) }
        val scores      = textRank(vectors, iterations = 30, damping = 0.85)

        return sentences
            .mapIndexed { i, _ -> i to scores[i] }
            .sortedByDescending { it.second }
            .take(targetCount)
            .sortedBy { it.first }
            .joinToString(" ") { sentences[it.first] }
            .trim()
    }

    // ── Private helpers ───────────────────────────────────────────

    private fun splitSentences(text: String): List<String> =
        text.replace("\n", " ")
            // Handles English (. ! ?) and Arabic (؟) sentence endings
            .split(Regex("(?<=[.!?؟])\\s+"))
            .map { it.trim() }
            .filter { it.split(Regex("\\s+")).size >= 4 }

    private fun tokenize(s: String): List<String> =
        s.lowercase()
            // Keep Arabic (U+0600–U+06FF), English alphanumeric, and spaces — strip the rest
            .replace(Regex("[^a-z0-9\\u0600-\\u06FF\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 1 && it !in STOP_WORDS }

    private fun tokenVector(sentence: String): Map<String, Int> =
        tokenize(sentence).groupingBy { it }.eachCount()

    private fun cosineSimilarity(a: Map<String, Int>, b: Map<String, Int>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val dot   = a.entries.sumOf { (w, c) -> c.toLong() * (b[w] ?: 0) }.toDouble()
        val normA = sqrt(a.values.sumOf { it.toLong() * it }.toDouble())
        val normB = sqrt(b.values.sumOf { it.toLong() * it }.toDouble())
        return if (normA == 0.0 || normB == 0.0) 0.0 else dot / (normA * normB)
    }

    private fun textRank(
        vectors   : List<Map<String, Int>>,
        iterations: Int,
        damping   : Double
    ): DoubleArray {
        val n = vectors.size
        val graph = Array(n) { i ->
            val row = DoubleArray(n) { j ->
                if (i == j) 0.0 else cosineSimilarity(vectors[i], vectors[j])
            }
            val sum = row.sum().takeIf { it > 0 } ?: 1.0
            DoubleArray(n) { j -> row[j] / sum }
        }
        var scores = DoubleArray(n) { 1.0 / n }
        repeat(iterations) {
            scores = DoubleArray(n) { i ->
                (1.0 - damping) / n + damping * (0 until n).sumOf { j -> graph[j][i] * scores[j] }
            }
        }
        return scores
    }
}
