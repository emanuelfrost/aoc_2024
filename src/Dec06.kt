import Dec06.getObstacles
import Dec06.isStartPos
import Dec06.setCharAtIndex
import Dec06.startPatrolling
import java.io.File

fun main(args: Array<String>) {

    val input = File("src/dec06.txt").readLines()
    val width = input.first().length
    val height = input.size

    val startingDirection: Dec06.Direction = Dec06.Direction.NORTH

    val obstacles = getObstacles(input)
    val currentPosition = input.withIndex().flatMap { line ->
        line.value.withIndex().mapNotNull { char ->
            if (char.value.isStartPos())
                Dec06.Pos(char.index, line.index)
            else null
        }
    }.first()

    val path = startPatrolling(
        width,
        height,
        currentPosition,
        startingDirection,
        obstacles,
        listOf(currentPosition),
        emptyList()
    )
    val unique = path?.distinct()?.size
    println(unique)
    require(unique == 5318)


    var loops = 0
    for (y in 0..<height)
        for (x in 0..<width) {
            val newObstacleList = input.toMutableList()
            val newLine = newObstacleList[y]
            newObstacleList[y] = setCharAtIndex(newLine, x, '#')

            val res = startPatrolling(
                width,
                height,
                currentPosition,
                startingDirection,
                getObstacles(newObstacleList),
                listOf(currentPosition),
                emptyList()
            )
            if (res == null)//Looop
                loops++
        }

    println(loops)
    require(loops == 1831)
}


object Dec06 {
    fun setCharAtIndex(original: String, index: Int, newChar: Char): String {
        if (index < 0 || index >= original.length) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for string of length ${original.length}")
        }
        return original.substring(0, index) + newChar + original.substring(index + 1)
    }

    private fun Char.isObstacle() = this.toString() == "#"
    fun Char.isStartPos() = this.toString() == "^"

    fun getObstacles(input: List<String>) =
        input.withIndex().flatMap { line ->
            line.value.withIndex()
                .mapNotNull { char -> if (char.value.isObstacle()) Pos(char.index, line.index) else null }
        }

    tailrec fun startPatrolling(
        width: Int,
        height: Int,
        currentPosition: Pos,
        direction: Direction,
        obstacles: List<Pos>,
        visitedPositions: List<Pos>,
        obstaclePositions: List<ObstaclePos>
    ): List<Pos>? {
        val newPosition = obstacles.filter {
            when (direction) {
                Direction.NORTH, Direction.SOUTH -> it.x == currentPosition.x
                Direction.EAST, Direction.WEST -> it.y == currentPosition.y
            }
        }.filter {
            when (direction) {
                Direction.NORTH -> it.y < currentPosition.y
                Direction.SOUTH -> it.y > currentPosition.y
                Direction.EAST -> it.x > currentPosition.x
                Direction.WEST -> it.x < currentPosition.x
            }
        }.let {
            when (direction) {
                Direction.NORTH, Direction.WEST -> it.lastOrNull()
                Direction.SOUTH, Direction.EAST -> it.firstOrNull()
            }
        }?.let {
            when (direction) {
                Direction.NORTH, Direction.SOUTH -> it + direction.pos
                Direction.WEST, Direction.EAST -> it - direction.pos
            }
        } ?: run {
            when (direction) {
                Direction.NORTH -> Pos(currentPosition.x, 0, true)
                Direction.SOUTH -> Pos(currentPosition.x, height - 1, true)
                Direction.EAST -> Pos(width - 1, currentPosition.y, true)
                Direction.WEST -> Pos(0, currentPosition.y, true)
            }
        }

        val newObstaclePosition: ObstaclePos? = if (!newPosition.out) {
            ObstaclePos(currentPosition, newPosition, direction)
        } else null

        if (newObstaclePosition != null && obstaclePositions.contains(newObstaclePosition)) {
            return null
        }

        val stepsToNewPosition = when (direction) {
            Direction.NORTH -> buildList {
                for (y in currentPosition.y - 1 downTo newPosition.y)
                    add(Pos(currentPosition.x, y))
            }

            Direction.EAST -> buildList {
                for (x in currentPosition.x + 1..newPosition.x)
                    add(Pos(x, currentPosition.y))
            }

            Direction.SOUTH -> buildList {
                for (y in currentPosition.y + 1..newPosition.y)
                    add(Pos(currentPosition.x, y))
            }

            Direction.WEST -> buildList {
                for (x in currentPosition.x - 1 downTo newPosition.x)
                    add(Pos(x, currentPosition.y))
            }
        }

        if (newPosition.out) {
            return visitedPositions + stepsToNewPosition
        }

        return startPatrolling(
            width,
            height,
            newPosition,
            rotateDirection90(currentDirection = direction),
            obstacles,
            visitedPositions + stepsToNewPosition,
            newObstaclePosition?.let { obstaclePositions + it } ?: obstaclePositions)
    }

    enum class Direction(val pos: Pos) {
        NORTH(Pos(0, 1)), EAST(Pos(1, 0)), SOUTH(Pos(0, -1)), WEST(Pos(-1, 0)),
    }

    private fun rotateDirection90(currentDirection: Direction) =
        when (currentDirection) {
            Direction.NORTH -> Direction.EAST
            Direction.EAST -> Direction.SOUTH
            Direction.SOUTH -> Direction.WEST
            Direction.WEST -> Direction.NORTH
        }

    data class ObstaclePos(
        val currentPosition: Pos,
        val newPosition: Pos,
        val currentDirection: Direction
    )

    data class Pos(val x: Int, val y: Int, val out: Boolean = false) {
        operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
        operator fun minus(pos: Pos) = Pos(x - pos.x, y - pos.y)
    }
}