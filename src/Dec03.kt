import java.io.File

fun main(args: Array<String>) {

    val operationsRegex = """mul\((\d{1,3}),(\d{1,3})\)""".toRegex()

    val input = File("src/dec03.txt").readText()

    val operationMatches = operationsRegex.findAll(input)
    val operations = operationMatches.map { Dec03.Operation.fromMatchResult(it) }
    val operationsResult = operations.sumOf { it.result }

    println(operationsResult)

    require(operationsResult == 170807108)

    val conditions = """(don\'t)[\s\S]*?(do\(\)|\Z)""".toRegex()
    val conditionMatches = conditions.findAll(input).map { it.range }.toList()

    val operationsResult2 = operations.filterNot { op ->
        conditionMatches.any { it.contains(op.range.first) && it.contains(op.range.last) }
    }.sumOf { it.result }

    require(operationsResult2 == 74838033)

    println(operationsResult2)
}

object Dec03 {
    data class Operation(
        val op: String,
        val first: Int,
        val second: Int,
        val range: IntRange,
    ){
        val result : Int
            get() = first * second

        companion object {
            fun fromMatchResult(match: MatchResult) =
                Operation(match.groups[0]!!.value, match.groups[1]!!.value.toInt(),  match.groups[2]!!.value.toInt(), match.range)
        }
    }
}