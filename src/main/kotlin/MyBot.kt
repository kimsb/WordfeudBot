import domain.Game
import domain.Move
import domain.Turn
import domain.TurnType.*

class MyBot(override val name: String) : Bot {

  override fun makeTurn(game: Game): Turn {
    val move = getAllMovesSorted(game).firstOrNull()

    return when {
      move != null -> Turn(turnType = MOVE, move = move)
      game.board.swapIsAllowed() -> Turn(turnType = SWAP, tilesToSwap = game.rack.tiles)
      else -> Turn(turnType = PASS)
    }
  }

  fun getAllMovesSorted(game: Game): List<Move> {
    return game.board.findAllMovesSorted(game.rack)
  }
}