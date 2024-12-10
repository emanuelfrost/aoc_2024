import Dec09.getChecksum
import Dec09.toBlocks
import java.io.File
import java.util.Collections

fun main(args: Array<String>) {
    val input = File("src/dec09.txt").readText()
    val regex = """\d""".toRegex()
    val diskMatches = regex.findAll(input)

    val disk: MutableList<Dec09.Block> = mutableListOf()

    for (block in diskMatches.withIndex()) {
        val isFreeSpace = block.index % 2 != 0

        disk.addAll(buildList {
            val value = block.value.value.toInt()
            (0..<value).forEach { _ ->
                if (isFreeSpace) {
                    add(Dec09.Block.Free)
                } else {
                    val index = block.index / 2
                    add(Dec09.Block.File(index))
                }
            }
        })
    }

    val compactedDisk = Dec09.compactDisk(0, disk)

    val checkSum = compactedDisk.getChecksum()

    require(checkSum == 6242766523059)
    println(checkSum)

    val diskWithFiles: MutableList<Dec09.DiskBlock?> = mutableListOf()
    for (block in diskMatches.withIndex()) {
        val isFreeSpace = block.index % 2 != 0
        val value = block.value.value.toInt()
        diskWithFiles.add(
            if (isFreeSpace) {
                if(value > 0)
                    Dec09.DiskBlock.Free(value)
                else
                    null
            } else {
                val index = block.index / 2
                Dec09.DiskBlock.File(index, value)
            }
        )
    }

    val totalNumberOfFiles = diskWithFiles.filterIsInstance<Dec09.DiskBlock.File>().count()
    val compactedDiskWholeFiles = Dec09.compactDiskWholeFiles(0, totalNumberOfFiles, diskWithFiles.filterNotNull().toMutableList())
    val checksumPart2 = compactedDiskWholeFiles.toBlocks().getChecksum()

    println(checksumPart2)
    require(checksumPart2 == 6272188244509)
}

object Dec09 {
    fun List<Block>.getChecksum() : Long{
        return this.withIndex().sumOf { position ->
            when (val v = position.value) {
                is Block.File -> position.index * v.id.toLong()
                else -> 0L
            }
        }
    }

    fun List<DiskBlock>.toBlocks() : List<Block>{
        return this.mapNotNull { diskBlock ->
            when(val block = diskBlock){
                is DiskBlock.File -> (0..<block.size).map { Block.File(block.id) }
                is DiskBlock.Free -> (0..<block.size).map { Block.Free }
                else -> null
            }
        }.flatten()
    }

    tailrec fun compactDiskWholeFiles(index: Int, totalFiles: Int, list: List<DiskBlock>): List<DiskBlock> {
        val nextIndex = index + 1
        val fileIndex = (totalFiles-index)-1
        val mutated = list.toMutableList()

        val file = try {
            mutated.withIndex().first { (it.value as? DiskBlock.File)?.id == fileIndex } as IndexedValue<DiskBlock.File>
        }catch (ex:Exception){
            throw Exception("No file found for $fileIndex")
        }

        println(file.value.id)

        val firstFreeSpace = try{
            mutated.withIndex().firstOrNull {
                val free = it.value as? DiskBlock.Free
                if(free == null)
                    false
                else
                    free.size >= file.value.size && it.index < file.index
            } as? IndexedValue<DiskBlock.Free>
        } catch (ex: Exception){
            throw Exception("No free space found for size ${file.value.size}")
        }

        if(firstFreeSpace != null){
            if(file.value.size == firstFreeSpace.value.size){
                Collections.swap(mutated, firstFreeSpace.index, file.index)
            } else {
                val fileSizedFreeSpace: DiskBlock.Free = DiskBlock.Free(file.value.size)
                val restSizedFreeSpace: DiskBlock.Free = DiskBlock.Free(firstFreeSpace.value.size-file.value.size)
                mutated[firstFreeSpace.index] = fileSizedFreeSpace
                Collections.swap(mutated, firstFreeSpace.index, file.index)
                mutated.add(firstFreeSpace.index+1, restSizedFreeSpace)
            }
        }

        return if(nextIndex > (totalFiles - 1)){
            mutated
        } else {
            compactDiskWholeFiles(index+1, totalFiles, mutated)
        }
    }

    tailrec fun compactDisk(index: Int, disk: List<Block>): List<Block> {
        val nextIndex = index + 1
        val totalSize = disk.size
        val currentBlock = disk[index]

        if (currentBlock is Block.Free) {
            disk.withIndex().lastOrNull { it.value is Block.File && it.index > index }?.let {
                Collections.swap(disk, index, it.index)
            }
        }

        return if (nextIndex > totalSize - 1)
            disk
        else
            compactDisk(nextIndex, disk)
    }

    sealed interface Block {
        data class File(val id: Int) : Block
        data object Free : Block
    }

    sealed interface DiskBlock {
        data class File(val id: Int, val size: Int) : DiskBlock
        data class Free(val size: Int) : DiskBlock
    }
}