package domain

data class Game(
    val board: Board,
    val rack: Rack,
    val score: Int = 0,
    val opponentScore: Int = 0,
    val scorelessTurns: Int = 0
)