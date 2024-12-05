import Dec02.SafeResult.*
import Dec02.checkLine
import java.io.File
import kotlin.math.abs

fun main(args: Array<String>) {

    val reports = File("src/dec02.txt").readLines().map { line ->
        line.split(" ").map { it.toInt() }
    }

    val reportResults = reports.map(::checkLine)

    val safeReports = reportResults.filter { it == Increasing || it == Decreasing }

    println(safeReports.size)
    require(safeReports.size == 631)

    //Part 2
    val reportMaxLength = reports.maxBy { it.size }.max()

    val badLines = (-1..<reportMaxLength).fold(initial = reports){ acc, levelToRemove ->
        val okLines = acc.mapNotNull {
            val reportWithLevelRemoved = Dec02.getReportWithLevelRemoved(it, levelToRemove)
            reportWithLevelRemoved?.let { report ->
                when (checkLine(report)) {
                    Increasing, Decreasing -> it
                    else -> null
                }
            }
        }

        acc - okLines.toSet()
    }

    val okFaultyReports = reports.size - badLines.size

    println(okFaultyReports)
    require(okFaultyReports == 665)
}

object Dec02 {

    enum class SafeResult {
        Initial,
        Invalid,
        Increasing,
        Decreasing
    }

    fun getReportWithLevelRemoved(report: List<Int>, levelIndex: Int): List<Int>? {
        when (levelIndex) {
            -1 -> return report
            !in report.indices -> return null
            else -> {
                val reportWithRemovedLevel = report.toMutableList()
                reportWithRemovedLevel.removeAt(levelIndex)
                return reportWithRemovedLevel
            }
        }
    }

    fun checkLine(line: List<Int>): SafeResult {
        return line.windowed(size = 2).fold(Initial) { acc, pair ->
            getSafeResult(pair, acc)
        }
    }

    private fun getSafeResult(pair: List<Int>, currentResult: SafeResult): SafeResult {
        val diffValue = pair[0] - pair[1]

        return if (abs(diffValue) !in 1..3)
            Invalid
        else {
            val increase = getSafeResultFromDiff(diffValue)
            when {
                currentResult == Initial -> increase
                currentResult != increase -> Invalid
                else -> currentResult
            }
        }
    }

    private fun getSafeResultFromDiff(
        diffValue: Int
    ): SafeResult {
        return when {
            diffValue > 0 -> Increasing
            diffValue < 0 -> Decreasing
            else -> Invalid
        }
    }

}