import Dec07.prepareCheckEquation
import java.io.File
import kotlin.math.pow

fun main(args: Array<String>) {

    val input = File("src/dec07.txt").readLines()
    val predicate = """\d+""".toRegex()
    val data = input.map {
        val all = predicate.findAll(it)
        val testValue = all.first().value.toLong()
        val numbers = all.drop(1).map { it.value.toLong() }.toList()
        Dec07.Equation(testValue, numbers)
    }

    val operators = setOf(Dec07.Operator.ADDITION, Dec07.Operator.MULTIPLICATION)
    val result = data.sumOf {
        if(prepareCheckEquation(it, operators)){
            it.testValue
        } else {
            0
        }
    }

    println(result)
    require(result == 7885693428401)

    val operators2 = setOf(Dec07.Operator.ADDITION, Dec07.Operator.MULTIPLICATION, Dec07.Operator.CONCATENATION)
    val result2 = data.sumOf {
        if(prepareCheckEquation(it, operators2)){
            it.testValue
        } else {
            0
        }
    }

    println(result2)
    require(result2 == 348360680516005)
}

object Dec07 {
    fun prepareCheckEquation(equation: Equation, operators: Set<Operator>): Boolean {
        val numberPairs = equation.numbers.zipWithNext()
        val slots = numberPairs.size

        val maxTotalCombinations = operators.size.toDouble().pow(slots.toDouble()).toInt()

        return iterateOperations(0, equation, operators, numberPairs, maxTotalCombinations)
    }

    private tailrec fun iterateOperations(index: Int, equation: Equation, operators: Set<Operator>, pairs: List<Pair<Long, Long>>, maxTotalCombinations: Int) : Boolean {
        val operatorList = buildOperatorCombo(index, operators, pairs.size, maxTotalCombinations)
        val result = checkOperationCombo(operatorList, pairs)
        val nextIndex = index + 1

        return if(result == equation.testValue)
            true
        else if(nextIndex > (maxTotalCombinations-1))
            false
        else
            iterateOperations(nextIndex, equation, operators, pairs, maxTotalCombinations)
    }

    private fun checkOperationCombo(operators: List<Operator>, pairs: List<Pair<Long, Long>>): Long {
        return pairs.withIndex().fold(initial = 0) { acc, value ->
            val first = if(value.index == 0) value.value.first else acc
            when(operators[value.index]){
                Operator.ADDITION -> first + value.value.second
                Operator.MULTIPLICATION -> first * value.value.second
                Operator.CONCATENATION -> "${first}${value.value.second}".toLong()
            }
        }
    }

    private fun buildOperatorCombo(index: Int, operators: Set<Operator>, slots: Int, maxTotalCombinations: Int) : List<Operator>{
        val operatorList = operators.toList()
        if (index < 0 || index >= maxTotalCombinations) {
            throw IllegalArgumentException("Index out of bounds for the given number of slots and operators.")
        }

        val result = mutableListOf<Operator>()
        var currentIndex = index
        repeat(slots) {
            val operator = operatorList[currentIndex % operators.size]
            result.add(operator)
            currentIndex /= operators.size
        }
        return result.reversed()

    }

    data class Equation(
        val testValue: Long,
        val numbers: List<Long>
    )

    enum class Operator {
        ADDITION,
        MULTIPLICATION,
        CONCATENATION,
    }
}