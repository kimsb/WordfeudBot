import domain.Board
import domain.Game
import domain.Rack
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import scrabble.emptyScrabbleBoard
import wordfeudapi.domain.ApiBoard
import wordfeudapi.domain.ApiTile

class MyBotTest {

  @Test
  fun `should find the move 5G MAVER`() {
    val myBot = MyBot("moominbot")

    val apiTiles = arrayOf(
      ApiTile(5, 3, 'G', false),
      ApiTile(4, 4, 'A', false),
      ApiTile(5, 4, 'L', false),
      ApiTile(4, 5, 'R', false),
      ApiTile(5, 5, 'U', false),
      ApiTile(4, 6, 'M', false),
      ApiTile(5, 6, 'O', false),
      ApiTile(2, 7, 'H', false),
      ApiTile(3, 7, 'J', false),
      ApiTile(4, 7, 'O', false),
      ApiTile(5, 7, 'N', false),
      ApiTile(6, 7, 'E', false),
      ApiTile(7, 7, 'T', false),
      ApiTile(4, 8, 'D', false)
    )

    val board = Board(emptyScrabbleAPIBoard(), apiTiles)
    val game = Game(board = board, rack = Rack("ABDKMRV".toList()), score = 0, 0, 0)
    val allMovesSorted = myBot.getAllMovesSorted(game)
    val move = allMovesSorted.find {
      it.word == "MAVER" && !it.horizontal && it.row == 6 && it.addedTiles.first().second.row == 4
    }

    assertThat(move).isNotNull
  }

  @Test
  fun `should find HJONET`() {
    val myBot = MyBot("moominbot")

    val board = emptyScrabbleBoard()
    val game = Game(board = board, rack = Rack("HJONETW".toList()), score = 0, 0, 0)
    val allMovesSorted = myBot.getAllMovesSorted(game)
    val move = allMovesSorted.find {
      it.word == "HJONET" && it.horizontal && it.row == 7 && it.addedTiles.first().second.column == 2
    }

    assertThat(move).isNotNull
  }
}

//TODO dette er kopiert, bør samles ett sted
private fun emptyScrabbleAPIBoard(): ApiBoard {
  return ApiBoard(
    arrayOf(
      intArrayOf(4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4),
      intArrayOf(0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0),
      intArrayOf(0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0),
      intArrayOf(1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1),
      intArrayOf(0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0),
      intArrayOf(0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0),
      intArrayOf(0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0),
      intArrayOf(4, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 4),
      intArrayOf(0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0),
      intArrayOf(0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0),
      intArrayOf(0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0),
      intArrayOf(1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1),
      intArrayOf(0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0),
      intArrayOf(0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0),
      intArrayOf(4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4)
    )
  )
}