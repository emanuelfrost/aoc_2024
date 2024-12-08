import Dec8.generateAntiNodesForAntenna
import java.io.File

fun main(args: Array<String>) {
    val input = File("src/dec08.txt").readLines()
    val bounds = Dec8.Vec2(input.first().length-1, input.size-1)
    val antennas = Dec8.getAntennas(input, bounds)
    val antennaPositions = antennas.map { it.position }

    val distinctPositionedAntiNodes = antennas.map { generateAntiNodesForAntenna(it, antennas, bounds, false) }
        .flatten()
        .distinctBy { it.position }

    println(distinctPositionedAntiNodes.size)
    require(distinctPositionedAntiNodes.size == 249)

    val infiniteAntinodes = antennas.map { generateAntiNodesForAntenna(it, antennas, bounds, true) }
        .flatten()

    val distinctPositionedAntinodesNotOverlappingAntennas = infiniteAntinodes
        .distinctBy { it.position }.filter { !antennaPositions.contains(it.position) }
    val antennasThatGeneratesAntinodes = antennas.filter { a -> antennas.count { it.symbol == a.symbol } >= 2 }

    val result2 = distinctPositionedAntinodesNotOverlappingAntennas.size + antennasThatGeneratesAntinodes.size

    println(result2)
    require(result2 == 905)
}

object Dec8 {
    fun generateAntiNodesForAntenna(currentAntenna: Antenna, antennas: List<Antenna>, bounds: Vec2, infinite: Boolean): List<Antinode> {
        val antennaPairs = antennas.filter { it.symbol == currentAntenna.symbol } - currentAntenna
        val antiNodes: MutableList<Antinode> = mutableListOf()
        for(pairedAntenna in antennaPairs){
            val positionDiff = currentAntenna.position-pairedAntenna.position
            antiNodes.addAll(getAntinode(bounds, currentAntenna, pairedAntenna, currentAntenna.position, positionDiff, true, infinite))
            antiNodes.addAll(getAntinode(bounds, currentAntenna, pairedAntenna, pairedAntenna.position, positionDiff, false, infinite))
        }
        return antiNodes.toList()
    }

    private tailrec fun getAntinode(
        bounds: Vec2,
        currentAntenna: Antenna,
        pairedAntenna: Antenna,
        startPosition: Vec2,
        modifier: Vec2,
        add: Boolean,
        infinite: Boolean,
        antiNodes: List<Antinode> = emptyList(),
        step: Int = 1
    ) : List<Antinode>{
        val newPosition = if (add) startPosition + (modifier * step) else startPosition - (modifier * step)
        val antinode = Antinode(currentAntenna.symbol, currentAntenna, pairedAntenna, newPosition)
        val outOfBounds = antinode.position.outOfBounds(bounds)

        return if(!infinite){
            if(!outOfBounds)
                antiNodes + antinode
            else
                antiNodes
        } else if(outOfBounds) antiNodes else
            getAntinode(bounds, currentAntenna, pairedAntenna, startPosition, modifier, add, true, antiNodes + antinode, step+1)
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
        operator fun times(i: Int) = Vec2(x * i, y * i)
        fun outOfBounds(bounds: Vec2) = x < 0 || y < 0 || x > bounds.x || y > bounds.y
    }
}