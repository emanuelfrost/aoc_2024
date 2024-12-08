import Dec8.generateAntiNodesForAntenna
import java.io.File

fun main(args: Array<String>) {

    val testInput = """............
........0...
.....0......
.......0....
....0.......
......A.....
............
............
........A...
.........A..
............
............"""

    //testInput.lines()//
    val input = File("src/dec08.txt").readLines()
    val bounds = Dec8.Vec2(input.first().length-1, input.size-1)
    val antennas = Dec8.getAntennas(input, bounds)
    val antennaPositions = antennas.map { it.position }
    val antennaTypes = antennas.map { it.symbol }.toSet()
    val antiNodes = antennas.map {
        generateAntiNodesForAntenna(it, antennas)
    }.flatten().filter { !it.position.outOfBounds(bounds) }.distinctBy { it.position }

    println(antiNodes.size)
    require(antiNodes.size == 249)

}

object Dec8 {
    fun generateAntiNodesForAntenna(currentAntenna: Antenna, antennas: List<Antenna>): List<Antinode> {
        val antennaPairs = antennas.filter { it.symbol == currentAntenna.symbol } - currentAntenna
        val antiNodes :MutableList<Antinode> = mutableListOf()
        for(pairedAntenna in antennaPairs){
            val positionDiff = currentAntenna.position-pairedAntenna.position
            val antinode1 = Antinode(currentAntenna.symbol, currentAntenna, pairedAntenna, currentAntenna.position+positionDiff)
            val antinode2 = Antinode(currentAntenna.symbol, currentAntenna, pairedAntenna, pairedAntenna.position-positionDiff)
            antiNodes.add(antinode1)
            antiNodes.add(antinode2)
        }
        return antiNodes.toList()
    }

    fun getAntennas(input: List<String>, size: Vec2) =
        (0..size.y).mapNotNull { y ->
            (0..size.x).mapNotNull { x ->
                val c = input[y][x]
                if (c != '.')
                    Antenna(c, Vec2(x, y))
                else
                    null
            }
        }.flatten()

    data class Antinode(
        val symbol: Char,
        val antenna1: Antenna,
        val antenna2: Antenna,
        val position: Vec2,
    ){
        val key = "$symbol:${position.key}"
    }

    data class Antenna(
        val symbol: Char,
        val position: Vec2,
    )

    data class Vec2(
        val x: Int,
        val y: Int,
    ){
        val key = "$x-$y"
        operator fun plus(pos: Vec2) = Vec2(x + pos.x, y + pos.y)
        operator fun minus(pos: Vec2) = Vec2(x - pos.x, y - pos.y)
        fun outOfBounds(bounds: Vec2) = x < 0 || y < 0 || x > bounds.x || y > bounds.y
    }
}