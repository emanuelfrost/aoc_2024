package src

import src.Dec04.checkForXMas
import src.Dec04.diagonalLines
import src.Dec04.verticalLines
import java.io.File

fun main(args: Array<String>) {

    val input = File("src/dec04.txt").readLines()
    val xmasRegex = """((?<=X)MA(?=S))|((?<=S)AM(?=X))""".toRegex()

    val horizontal = input.sumOf { xmasRegex.findAll(it).count() }
    val vertical = verticalLines(input).sumOf { xmasRegex.findAll(it).count() }
    val diagonalNE = diagonalLines(input, true).sumOf { xmasRegex.findAll(it).count() }
    val diagonalNW = diagonalLines(input, false).sumOf { xmasRegex.findAll(it).count() }

    val xmasCount = horizontal + diagonalNW + vertical + diagonalNE

    println(xmasCount)
    assert(xmasCount == 2562)

    //Part2
    val xMasCount = checkForXMas(input)

    println(xMasCount)
    assert(xMasCount == 1902)
}

object Dec04 {

    fun checkForXMas(lines: List<String>): Int {
        val width = lines.first().length
        val height = lines.size
        var counter = 0
        val mas = listOf("MAS", "SAM")
        val masFirstLetters = listOf("M", "S")

        for (y in 0..<height) for (x in 0..<width) {
            val pos = Pos(x, y)
            val letter = lines.letter(pos)
            if (masFirstLetters.contains(letter)) {
                val square = getXmasSquare(pos, lines)
                if (mas.contains(square.first) && mas.contains(square.second)) {
                    counter++
                }
            }
        }

        return counter
    }

    private fun getXmasSquare(pos: Pos, lines: List<String>): Pair<String, String> {
        val middle = lines.letter(pos + Pos(1, 1))
        val firstWord = lines.letter(pos) + middle + lines.letter(pos + Pos(2, 2))
        val secondWord = lines.letter(pos + Pos(2, 0)) + middle + lines.letter(pos + Pos(0, 2))
        return Pair(firstWord, secondWord)
    }

    private fun List<String>.letter(pos: Pos) =
        getOrNull(pos.y)?.getOrNull(pos.x)?.toString() ?: ""

    fun verticalLines(lines: List<String>): List<String> {
        val width = lines.first().length
        val height = lines.size

        val newLines = (0..<width).map { emptyList<String>() }.toMutableList()

        for (x in 0..<width) for (y in 0..<height) {
            val c = lines[y][x]
            newLines[x] = newLines[x] + c.toString()
        }

        return newLines.map { it.joinToString("") }
    }

    fun diagonalLines(lines: List<String>, northToEast: Boolean): List<String> {
        val w = lines.first().length
        val h = lines.size
        val totalRows = (w + h) - 1

        val newLines = (0..totalRows).map { emptyList<String>() }.toMutableList()
        for (rowCount in 0..<totalRows) {
            var position = when {
                rowCount < w -> if (northToEast) Pos(w - 1 - rowCount, 0) else Pos(rowCount, 0)
                else -> if (northToEast) Pos(0, rowCount - w + 1) else Pos(w - 1, rowCount - w + 1)
            }
            while (!position.outOfBounds(w, h)) {
                newLines[rowCount] = newLines[rowCount] + lines.letter(Pos(position.x,position.y))
                position += if (northToEast) Pos(1, 1) else Pos(-1, 1) //North to west
            }
        }

        return newLines.map { it.joinToString("") }
    }

    data class Pos(val x: Int, val y: Int) {
        operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
        fun outOfBounds(width: Int, height: Int) = x < 0 || y < 0 || x > width - 1 || y > height - 1
    }
}