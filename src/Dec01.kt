import java.io.File
import kotlin.math.abs


fun main(args: Array<String>) {

    val firstList: MutableList<Long> = mutableListOf()
    val secondList: MutableList<Long> = mutableListOf()

    File("src/dec01.txt").forEachLine { line ->
        val data = line.split(" ")
        firstList.add(data.first().toLong())
        secondList.add(data.last().toLong())
    }

    val sum = firstList.sorted().zip(secondList.sorted()){ x,y ->
        abs(x - y)
    }.sumOf { it }

    println(sum)

    require(sum == 3246517L)

    //Your puzzle answer was 3246517.
    //The first half of this puzzle is complete! It provides one gold star: *

    val sum2 = firstList.map { number ->
        number * secondList.count { it == number }
    }.sum()

    println(sum2)

    require(sum2 == 29379307L)
}