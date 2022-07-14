package domain

import domain.TurnType.*

class Turn(
    val turnType: TurnType,
    val move: Move? = null,
    val tilesToSwap: List<Char> = emptyList()
) {
    init {
        check(
            when (turnType) {
                MOVE -> move != null
                SWAP -> tilesToSwap.isNotEmpty()
                PASS -> true
            }
        )
    }
}
