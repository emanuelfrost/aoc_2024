import Dec10.getKey
import Dec10.getMap
import Dec10.process
import java.io.File

fun main(args: Array<String>) {
    val input = File("src/dec10.txt").readLines()

    val bounds = Dec10.Vec2(input.first().length - 1, input.size - 1)
    val map = input.getMap().associateBy { it.pos.getKey() }
    val entries = map.entries.filter { it.value.value == 0 }.map { it.value }

    val res = entries.map {
        process(it, it, map, bounds)
    }

    val trailsScore = res.map { it.distinct() }.sumOf { it.size }

    println(trailsScore)
    require(trailsScore == 644)

    val sum = res.sumOf { it.size }

    println(sum)
    require(sum == 1366)
}

object Dec10 {
    fun process(startPath: MapTile, path: MapTile, map: Map<String, MapTile>, bounds: Vec2) : List<Pair<MapTile, MapTile>> {
        val nextPaths = scanNextTrailPaths(path, map, bounds) { it.value == (path.value + 1) }
        val endPaths = nextPaths.filter { it.value == 9 }
        val continuation = nextPaths.filter { it.value != 9 }

        if(endPaths.isNotEmpty())
            return endPaths.map { Pair(startPath, it) }

        if(continuation.isEmpty())
            return emptyList()

        return continuation.map { process(startPath, it, map, bounds ) }.flatten()
    }

    private fun scanNextTrailPaths(mapTile: MapTile, map: Map<String, MapTile>, bounds: Vec2, predicate: (MapTile) -> Boolean): List<MapTile> {
        return buildList {
            mapTile.getRelativeMapTile(map, Direction.NORTH, bounds, predicate)?.let { add(it) }
            mapTile.getRelativeMapTile(map, Direction.WEST,  bounds, predicate)?.let { add(it) }
            mapTile.getRelativeMapTile(map, Direction.SOUTH, bounds, predicate)?.let { add(it) }
            mapTile.getRelativeMapTile(map, Direction.EAST,  bounds, predicate)?.let { add(it) }
        }
    }

    private fun MapTile.getRelativeMapTile(
        map: Map<String, MapTile>,
        direction: Direction,
        bounds: Vec2,
        predicate: (MapTile) -> Boolean
    ): MapTile? {
        val key = this.pos.getRelativePosition(direction).getKey()
        val mapTile = map.getOrDefault(key, null)
        return if (mapTile != null && !mapTile.pos.outOfBounds(bounds) && predicate(mapTile)) mapTile else null
    }

    private fun Vec2.getRelativePosition(direction: Direction) =
        this + direction.pos

    enum class Direction(val pos: Vec2) {
        NORTH(pos = Vec2(x = 0, y = -1)),
        EAST(pos = Vec2(x = 1, y = 0)),
        SOUTH(pos = Vec2(x = 0, y = 1)),
        WEST(pos = Vec2(x = -1, y = 0)),
    }

    fun List<String>.getMap(): List<MapTile> =
        this.mapIndexed { y: Int, s: String ->
            s.mapIndexed { x: Int, c: Char ->
                MapTile(
                    value = c.toString().toInt(),
                    pos = Vec2(x, y)
                )
            }
        }.flatten()

    fun Vec2.getKey() =
        "$x-$y"

    data class MapTile(
        val value: Int,
        val pos: Vec2
    )

    data class Vec2(
        val x: Int,
        val y: Int,
    ) {
        operator fun plus(pos: Vec2) = Vec2(x + pos.x, y + pos.y)
        operator fun minus(pos: Vec2) = Vec2(x - pos.x, y - pos.y)
        operator fun times(i: Int) = Vec2(x * i, y * i)
        fun outOfBounds(bounds: Vec2) = x < 0 || y < 0 || x > bounds.x || y > bounds.y
    }
}