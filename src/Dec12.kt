import Dec12.getKey
import Dec12.getMap
import Dec12.process
import java.io.File

fun main(args: Array<String>) {
    val input = File("src/dec12.txt").readLines()
    val list = input.getMap()
    val bounds = Dec12.Vec2(input.first().length - 1, input.size - 1)
    val res = process(list, list.associateBy { it.pos.getKey() }, bounds, emptyList())

    val s = res.sumOf { it.tiles.size*it.borders.size }
    println(s)
    require(s == 1465112)
}

object Dec12 {

    fun process(scan: List<MapTile.Tile>, map: Map<String, MapTile.Tile>, bounds: Vec2, alreadyChecked: List<Group>): List<Group> {
        return scan.fold(initial = alreadyChecked){ checked, mapTile ->
            val surroundings = scanNextTrailPaths(mapTile, map, { it.value == mapTile.value })
            val matchedTiles = surroundings.filterIsInstance<MapTile.Tile>()
            val borders = surroundings.filterNot { it is MapTile.Tile }
            val group = checked.firstOrNull { it.tiles.contains(mapTile) }

            val list = if(group == null){
                checked + Group(tiles = setOf(mapTile)+matchedTiles, borders = borders.map { Pair(mapTile, it) }.toSet())
            } else {
                val list = checked - group
                val newGroup = group.copy(
                    tiles = (group.tiles + matchedTiles),
                    borders = group.borders + borders.map { Pair(mapTile, it) }
                )
                list + newGroup
            }

            val newScan = if(group == null) matchedTiles else matchedTiles - group.tiles
            process(newScan, map, bounds, list)
        }
    }

    data class Group(
        val tiles: Set<MapTile.Tile>,
        val borders: Set<Pair<MapTile.Tile, MapTile>>
    )

    private fun scanNextTrailPaths(mapTile: MapTile, map: Map<String, MapTile.Tile>, predicate: (MapTile.Tile) -> Boolean): List<MapTile> {
        return buildList {
            add(mapTile.getRelativeMapTile(map, Direction.NORTH, predicate))
            add(mapTile.getRelativeMapTile(map, Direction.WEST,  predicate))
            add(mapTile.getRelativeMapTile(map, Direction.SOUTH, predicate))
            add(mapTile.getRelativeMapTile(map, Direction.EAST,  predicate))
        }
    }

    private fun MapTile.getRelativeMapTile(
        map: Map<String, MapTile.Tile>,
        direction: Direction,
        predicate: (MapTile.Tile) -> Boolean
    ): MapTile {
        val relativePos = this.pos.getRelativePosition(direction)
        val mapTile = map.getOrDefault(relativePos.getKey(), null)
        return if(mapTile == null){
            MapTile.OutOfBounds(relativePos)
        } else if(!predicate(mapTile)){
            mapTile.asUnmatched()
        } else{
            mapTile
        }
    }


    private fun Vec2.getRelativePosition(direction: Direction) =
        this + direction.pos

    enum class Direction(val pos: Vec2) {
        NORTH(pos = Vec2(x = 0, y = -1)),
        EAST(pos = Vec2(x = 1, y = 0)),
        SOUTH(pos = Vec2(x = 0, y = 1)),
        WEST(pos = Vec2(x = -1, y = 0)),
    }

    fun List<String>.getMap(): List<MapTile.Tile> =
        this.mapIndexed { y: Int, s: String ->
            s.mapIndexed { x: Int, c: Char ->
                MapTile.Tile(
                    value = c,
                    pos = Vec2(x, y)
                )
            }
        }.flatten()

    fun Vec2.getKey() =
        "$x-$y"

    sealed class MapTile(
        open val pos: Vec2
    ){
        data class Tile(
            val value: Char,
            override val pos: Vec2
        ) : MapTile(pos){

            fun asUnmatched() =
                Unmatched(value, pos)
        }

        data class OutOfBounds(
            override val pos: Vec2
        ) : MapTile(pos)

        data class Unmatched(
            val value: Char,
            override val pos: Vec2
        ) : MapTile(pos)
    }


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
