package domain

import wordfeudapi.domain.ApiBoard
import wordfeudapi.domain.ApiTile
import java.lang.StringBuilder
import kotlin.math.max

class Board(squares: List<List<Square>>) {
    val squares: List<List<Square>> = squares.mapIndexed { i, row ->
        row.mapIndexed { j, square ->
            square.copy(
                isAnchor = !squares[i][j].isOccupied() &&
                        ((i == 7 && j == 7) ||
                                squares.getOrNull(i - 1)?.get(j)?.isOccupied() == true ||
                                squares[i].getOrNull(j - 1)?.isOccupied() == true ||
                                squares[i].getOrNull(j + 1)?.isOccupied() == true ||
                                squares.getOrNull(i + 1)?.get(j)?.isOccupied() == true)
            )
        }
    }

    constructor(apiBoard: ApiBoard, apiTiles: Array<ApiTile>) : this(
        apiBoard.board.mapIndexed { row, ints ->
            ints.mapIndexed { column, _ ->
                val tile = apiTiles.find { it.x == column && it.y == row }
                    ?.let { Tile(if (it.isWildcard) it.character.lowercaseChar() else it.character) }
                Square(
                    tile = tile,
                    letterMultiplier = apiBoard.getLetterMultiplier(column, row),
                    wordMultiplier = apiBoard.getWordMultiplier(column, row)
                )
            }
        }
    )

    fun findAllMovesSorted(rack: Rack): List<Move> {
        val rowMoves = getRowsWithCrossChecks().flatMapIndexed { index, it ->
            it.findAcrossMoves(rack).map {
                toMove(it, index, true)
            }
        }
        val columnMoves = getTransposedRowsWithCrossChecks().flatMapIndexed { index, it ->
            it.findAcrossMoves(rack).map {
                toMove(it, index, false)
            }
        }
        return (rowMoves + columnMoves).sortedByDescending { it.score }
    }

    private fun getRowsWithCrossChecks(): List<Row> {
        return Board(getTransposedRows().map(Row::crossChecks)).getTransposedRows()
    }

    private fun getTransposedRowsWithCrossChecks(): List<Row> {
        return Board(getRows().map(Row::crossChecks)).getTransposedRows()
    }

    private fun transpose(): Board {
        return Board(squares.indices.map { row ->
            squares.indices.map { column ->
                squares[column][row]
            }
        })
    }

    private fun getRows(): List<Row> {
        return squares.map {
            Row(it)
        }
    }

    private fun getTransposedRows(): List<Row> {
        return transpose().squares.map {
            Row(it)
        }
    }

    private fun toMove(rowMove: RowMove, rowIndex: Int, horizontal: Boolean): Move {
        val addedTiles = (rowMove.word.indices).filterIndexed { index, _ ->
            if (horizontal) {
                !squares[rowIndex][rowMove.startIndex + index].isOccupied()
            } else {
                !squares[rowMove.startIndex + index][rowIndex].isOccupied()
            }
        }.map {
            if (horizontal) {
                Pair(Tile(rowMove.word[it]), Coordinate(rowIndex, rowMove.startIndex + it))
            } else {
                Pair(Tile(rowMove.word[it]), Coordinate(rowMove.startIndex + it, rowIndex))
            }
        }
        return Move(
            rowMove.word,
            rowMove.score,
            rowIndex,
            horizontal,
            addedTiles
        )
    }

    fun withMove(move: Move): Board {

        val mutableSquares = squares.map { it.toMutableList() }.toMutableList()
        move.addedTiles.forEach {
            mutableSquares[it.second.row][it.second.column] = Square(Tile(it.first.letter))
        }
        return Board(mutableSquares)
    }

    fun bagCount(): Int {
        val occupiedSquares = squares.flatten().filter { it.isOccupied() }.count()
        return max(0, 90 - occupiedSquares)
    }

    fun swapIsAllowed(): Boolean {
        return bagCount() >= 7
    }

}
