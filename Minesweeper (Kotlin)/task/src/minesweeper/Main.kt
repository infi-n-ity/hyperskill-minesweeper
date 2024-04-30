package minesweeper

import kotlin.random.Random

const val MAP_ROW_SIZE = 9
const val MAP_COLUMN_SIZE = 9

const val ROW_START_INDEX = 0
const val ROW_END_INDEX = MAP_ROW_SIZE - 1
const val COLUMN_START_INDEX = 0
const val COLUMN_END_INDEX = MAP_COLUMN_SIZE - 1

fun main() {
    println("How many mines do you want on the field?")
    val numberOfMines = readln().toInt()
    val map = MinesWeeperMap()
    map.mine(numberOfMines)
    map.print()

    do {
        println("Set/unset mines marks or claim a cell as free:")
        val (x, y, command) = readln().split(" ")
        val result = map.mark(x.toInt(), y.toInt(), command)
        if (result == '*' || result == '/') {
            println()
            map.print()
        } else if (result == 'X') {
            println()
            map.print()
            println("You stepped on a mine and failed!")
            return
            break
        } else {
            println("There is a number here!")
        }
    } while (!map.isWin())

    println("Congratulations! You found all the mines!")
}

class MinesWeeperMap {
    private val map = List(MAP_ROW_SIZE) { MutableList(MAP_COLUMN_SIZE) { Cell() } }

    fun mine(numberOfMines: Int) {
        val random = Random
        var numberOfMinesInMap = 0;

        while (numberOfMinesInMap != numberOfMines) {
            val randomRow = random.nextInt(ROW_START_INDEX, ROW_END_INDEX + 1)
            val randomColumn = random.nextInt(COLUMN_START_INDEX, COLUMN_END_INDEX + 1)
            if (!map[randomRow][randomColumn].isMine) {
                map[randomRow][randomColumn].isMine = true
                numberOfMinesInMap++
            }
        }
    }

    private fun calculateHints(rowIndex: Int, columnIndex: Int) {
        if (!map[rowIndex][columnIndex].isMine) {
             when {
                rowIndex == ROW_START_INDEX && columnIndex == COLUMN_START_INDEX -> getNumberOfMinesAround(
                    ROW_START_INDEX..ROW_START_INDEX + 1,
                    COLUMN_START_INDEX..COLUMN_START_INDEX + 1,
                    rowIndex,
                    columnIndex
                )

                rowIndex == ROW_START_INDEX && columnIndex == COLUMN_END_INDEX -> getNumberOfMinesAround(
                    ROW_START_INDEX..ROW_START_INDEX + 1,
                    COLUMN_END_INDEX - 1..COLUMN_END_INDEX,
                    rowIndex,
                    columnIndex
                )

                rowIndex == ROW_END_INDEX && columnIndex == COLUMN_START_INDEX -> getNumberOfMinesAround(
                    ROW_END_INDEX - 1..ROW_END_INDEX,
                    COLUMN_START_INDEX..COLUMN_START_INDEX + 1,
                    rowIndex,
                    columnIndex
                )

                rowIndex == ROW_END_INDEX && columnIndex == COLUMN_END_INDEX -> getNumberOfMinesAround(
                    ROW_END_INDEX - 1..ROW_END_INDEX,
                    COLUMN_END_INDEX - 1..COLUMN_END_INDEX,
                    rowIndex,
                    columnIndex
                )

                rowIndex == ROW_START_INDEX -> getNumberOfMinesAround(
                    ROW_START_INDEX..ROW_START_INDEX + 1,
                    columnIndex - 1..columnIndex + 1,
                    rowIndex,
                    columnIndex
                )

                rowIndex == ROW_END_INDEX -> getNumberOfMinesAround(
                    ROW_END_INDEX - 1..ROW_END_INDEX,
                    columnIndex - 1..columnIndex + 1,
                    rowIndex,
                    columnIndex
                )

                columnIndex == COLUMN_START_INDEX -> getNumberOfMinesAround(
                    rowIndex - 1..rowIndex + 1,
                    COLUMN_START_INDEX..COLUMN_START_INDEX + 1,
                    rowIndex,
                    columnIndex
                )

                columnIndex == COLUMN_END_INDEX -> getNumberOfMinesAround(
                    rowIndex - 1..rowIndex + 1,
                    COLUMN_END_INDEX - 1..COLUMN_END_INDEX,
                    rowIndex,
                    columnIndex
                )

                else -> getNumberOfMinesAround(
                    rowIndex - 1..rowIndex + 1, columnIndex - 1..columnIndex + 1,
                    rowIndex,
                    columnIndex
                )
            }
        }
    }

    private fun getNumberOfMinesAround(
        rowRange: IntRange,
        columnRange: IntRange,
        currentRowIndex: Int,
        currentColumnIndex: Int
    ) {
        var numberOfMinesAround = 0
        for (i in rowRange) {
            for (j in columnRange) {
                if (map[i][j].isMine) {
                    numberOfMinesAround++
                }
            }
        }
        map[currentRowIndex][currentColumnIndex].field = if (numberOfMinesAround > 0) numberOfMinesAround.digitToChar() else '/'

        if (numberOfMinesAround == 0) {
            for (i in rowRange) {
                for (j in columnRange) {
                    if ((i != currentRowIndex || j != currentColumnIndex) && (map[i][j].field == '.' || map[i][j].field == '*') && !map[i][j].isMine) {
                        calculateHints(i, j)
                    }
                }
            }
        }
    }

    fun print() {
        println(" │123456789│")
        println("—│—————————│")
        map.forEachIndexed { index, row ->
            val r = row.map {
                if (it.isMark) {
                    '*'
                } else it.field
            }
            println("${index + 1}│" + r.joinToString("") + "│")
        }
        println("—│—————————│")
    }

    fun mark(x: Int, y: Int, command: String): Char {
        if (command == "mine") {
            return if (map[y - 1][x - 1].field == '.' || map[y - 1][x - 1].field == '*') {
                if (map[y - 1][x - 1].isMark) {
                    map[y - 1][x - 1].field = '.'
                    map[y - 1][x - 1].isMark = false
                } else {
                    map[y - 1][x - 1].field = '*'
                    map[y - 1][x - 1].isMark = true
                }
                '*'
            } else {
                ' '
            }
        } else {
            if (map[y - 1][x - 1].field == '.' && !map[y - 1][x - 1].isMine) {
                calculateHints(y - 1, x - 1)
                return '/'
            } else {
                map[y - 1][x - 1].field = 'X'
                return 'X'
            }
        }
    }

    fun isWin(): Boolean {
        var freeCellsOpen = true
        var allMinesMarked = true
        map.forEach { row ->
            val allMinesMarkedInRow = row.filter { it.isMine }.all { it.isMark }
            val notMineCellMarkedInRow = row.filter { !it.isMine }.all { !it.isMark }
            val freeCellsOpenInRow = row.filter { !it.isMine }.all { it.field != '.' }
            if (!freeCellsOpenInRow) {
                freeCellsOpen =  false
            } else if (!allMinesMarkedInRow || notMineCellMarkedInRow) {
                allMinesMarked = false
            }
        }
        return freeCellsOpen || allMinesMarked
    }
}

class Cell(var field: Char = '.', var isMark: Boolean = false, var isMine: Boolean = false)
