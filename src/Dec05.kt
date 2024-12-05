import Dec05.getMiddleNumber
import Dec05.getOrderedPage
import Dec05.getRulesForPageList
import Dec05.validatePageIndexed
import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main(args: Array<String>) {

    val input = File("src/dec05.txt").readText().split(Regex("\n\\s*\n"))
    val rulesRegex = """(\d+)\|(\d+)""".toRegex()
    val rules = rulesRegex.findAll(input.first().trim()).map {
        val first = it.groups[1]!!.value.toInt()
        val last = it.groups[2]!!.value.toInt()
        val key = Dec05.createRuleKey(first, last)
        key to Dec05.Rule(key, first, last)
    }.toMap()

    val pages = input.last().trim().lines().map { it.split(",").map { it.toInt() } }

    val validPagesSum = pages
        .filter { it.getRulesForPageList(rules).validatePageIndexed(it) == null }
        .sumOf { it.getMiddleNumber() }

    println(validPagesSum)
    require(validPagesSum == 4959)

    val invalidPagesSum = pages.filter { it.getRulesForPageList(rules).validatePageIndexed(it) != null }
        .map { it.getRulesForPageList(rules).getOrderedPage(it) }
        .sumOf { it.getMiddleNumber() }

    println(invalidPagesSum)
    require(invalidPagesSum == 4655)
}

object Dec05 {
    data class Rule(
        val key: String,
        val first: Int,
        val last: Int
    )

    fun createRuleKey(first: Int, last: Int) =
        "${min(first, last)}-${max(first, last)}"

    private fun List<Int>.getDistinctPairs() = this.flatMap {
        this.mapNotNull { b -> if (it != b) it to b else null }
    }

    fun List<Int>.getMiddleNumber() =
        this[this.size/2]

    fun List<Int>.getRulesForPageList(rules: Map<String, Rule>) =
        this.getDistinctPairs().mapNotNull {
            rules[createRuleKey(it.first, it.second)]
        }

    fun List<Rule>.validatePageIndexed(page: List<Int>): Pair<Int, Int>? {
        for (rule in this) {
            var foundFirstIndex: Int? = null
            var foundLastIndex: Int? = null
            for (number in page.withIndex()) {
                if (number.value == rule.first){
                    foundFirstIndex = number.index
                }
                if (number.value == rule.last) {
                    foundLastIndex = number.index
                }

                if(foundFirstIndex != null && foundLastIndex != null){
                    val valid = foundFirstIndex < foundLastIndex
                    if(!valid)
                        return Pair(foundFirstIndex, foundLastIndex)
                }
            }
        }
        return null
    }

    private fun <E> MutableList<E>.swap(index1: Int, index2: Int){
        this[index1] = this[index2].also { this[index2] = this[index1] }
    }

    tailrec fun List<Rule>.getOrderedPage(page: List<Int>): List<Int> {
        val valid = validatePageIndexed(page) ?: return page

        val newList = page.toMutableList().also {
            it.swap(valid.first, valid.second)
        }
        return getOrderedPage(newList.toList())
    }


}