import Dec11.blink
import java.io.File

fun main(args: Array<String>) {
    val input = File("src/dec11.txt").readText()
    val regex = """\d+""".toRegex()
    val stones = regex.findAll(input).map {
        it.value.toLong()
    }

    val res = (1..25).fold(initial = stones){ acc, iteration ->
        acc.blink()
    }

    println(res.count())

    val res2 = blink(stones.toList())
    println(res2)

}

object Dec11 {

    data class Stones(
        val groups: MutableMap<Long, Long> = mutableMapOf()
    ){
        fun update(stone: Long, count: Long){
            groups[stone] = groups.getOrDefault(stone, 0L) + count
        }
    }

    fun blink(list: List<Long>) : Long {
        var stones = list.toList().asGroup()
        repeat(75){
            stones = stones.blink()
        }

        return stones.groups.values.sumOf { it }
    }

    fun Stones.blink() : Stones{
        return Stones().also { stones ->
            this.groups.forEach { (number, count) ->
                when {
                    number == 0L -> {
                        stones.update(1, count)
                    }

                    number.evenNumberOfDigits() -> {
                        val (first, second) = number.split()
                        stones.update(first, count)
                        stones.update(second, count)
                    }

                    else -> {
                        stones.update(number * 2024, count)
                    }
                }
            }
        }
    }

    private fun List<Long>.asGroup() = Stones().also { stones ->
        this.groupingBy { it }.eachCount().map {
            stones.update(it.key, it.value.toLong())
        }
    }

    fun Sequence<Long>.blink() : Sequence<Long>{
        val list = this
        return sequence {
            list.forEach { number ->
                when {
                    number == 0L -> {
                        yield(1)
                    }
                    number.evenNumberOfDigits() -> {
                        val (first, second) = number.split()
                        yield(first)
                        yield(second)
                    }
                    else -> {
                        yield(number*2024)
                    }
                }
            }
        }
    }

    private fun Long.evenNumberOfDigits() : Boolean {
        return this.toString().length % 2 == 0
    }

    private fun Long.split() : Pair<Long, Long> {
        val string = this.toString()
        val (first, second) = string.chunked(string.length / 2)
        return Pair(first.toLong(), second.toLong())
    }
}


