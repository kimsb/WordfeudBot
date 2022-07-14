import domain.Game
import domain.Turn
import domain.TurnType.*

class ControlBot : Bot {

    override val name = "ControlBot"

    override fun makeTurn(game: Game): Turn {
        val move = game.board.findAllMovesSorted(game.rack).firstOrNull()

        return when {
            move != null -> Turn(turnType = MOVE, move = move)
            game.board.swapIsAllowed() -> Turn(turnType = SWAP, tilesToSwap = game.rack.tiles)
            else -> Turn(turnType = PASS)
        }
    }
}