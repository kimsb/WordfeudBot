import domain.Game
import domain.Turn

interface Bot {
    val name: String

    fun makeTurn(game: Game): Turn
}