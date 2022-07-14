package simulation

import Bot
import domain.Board
import domain.Turn
import domain.TurnType

fun printSimulatedRounds(myBot: Bot, controlBot: Bot, simulatedRounds: List<Simulator.SimulatedRound>, time: Long) {
    val rounds = simulatedRounds.size
    for (i in 1..rounds) {
        val simulationResult = simulatedRounds[i - 1]
        simulationResult.simulatedGameA.printSimulatedRounds(
            myBot,
            controlBot,
            i,
            true,
            simulationResult.initialLetterDistribution
        )
        simulationResult.simulatedGameB.printSimulatedRounds(
            myBot,
            controlBot,
            i,
            false,
            simulationResult.initialLetterDistribution
        )
    }
    println("${System.lineSeparator()}Simulation finished in $time ms${System.lineSeparator()}")
    val myBotWins = simulatedRounds.sumOf {
        when {
            it.simulatedGameA.myBotScore > it.simulatedGameA.controlBotScore -> 1.0
            it.simulatedGameA.myBotScore == it.simulatedGameA.controlBotScore -> 0.5
            else -> 0.0
        } + when {
            it.simulatedGameB.myBotScore > it.simulatedGameB.controlBotScore -> 1.0
            it.simulatedGameB.myBotScore == it.simulatedGameB.controlBotScore -> 0.5
            else -> 0.0
        }
    }

    println(
        "${myBot.name}: $myBotWins wins (${"%.2f".format(myBotWins / (rounds * 2) * 100)}%) total score: ${
            simulatedRounds.sumOf { it.simulatedGameA.myBotScore + it.simulatedGameB.myBotScore }
        }"
    )
    println(
        "${controlBot.name}: ${(rounds * 2) - myBotWins} wins (${"%.2f".format(100 - (myBotWins / (rounds * 2) * 100))}%) total score: ${
            simulatedRounds.sumOf { it.simulatedGameA.controlBotScore + it.simulatedGameB.controlBotScore }
        }"
    )
}

private fun Board.toPrintableLines(): List<String> {
    val boardLines = squares.map { row ->
        "| " + row.map { square ->
            when {
                square.isOccupied() -> square.tile!!.letter
                square.wordMultiplier == 3 -> '@'
                square.wordMultiplier == 2 -> '*'
                square.letterMultiplier == 3 -> '+'
                square.letterMultiplier == 2 -> '-'
                else -> ' '
            }
        }.joinToString("  ") + " |"
    }
    return (boardLines + listOf("+---------------------------------------------+"))
}

private fun Simulator.SimulatedGame.printOutcome(): String {
    return when {
        this.myBotScore > this.controlBotScore -> "VICTORY!"
        this.myBotScore == this.controlBotScore -> "DRAW"
        else -> "LOSS..."
    }
}

private fun Simulator.SimulatedGame.printSimulatedRounds(
    myBot: Bot,
    controlBot: Bot,
    gameNumber: Int,
    myBotStarts: Boolean,
    initialLetterDistribution: String
) {
    println()
    println("Game #${gameNumber}${if (myBotStarts) 'a' else 'b'} - ${printOutcome()}")
    println("Bag: $initialLetterDistribution")

    val p1Name = if (myBotStarts) myBot.name else controlBot.name
    val p2Name = if (myBotStarts) controlBot.name else myBot.name
    val p1Score = if (myBotStarts) myBotScore else controlBotScore
    val p2Score = if (myBotStarts) controlBotScore else myBotScore
    val p1Moves = if (myBotStarts) myBotTurns else controlBotTurns
    val p2Moves = if (myBotStarts) controlBotTurns else myBotTurns

    val formatTemplate = "| %16s | %3s | %-3s | %-16s |     %30s"
    println("+------------------+-----+-----+------------------+     +---------------------------------------------+")

    val boardLines = this.board.toPrintableLines()
    val lineCount = maxOf(boardLines.size + 1, p1Moves.size + 3)
    (0..lineCount).forEach { i ->
        println(
            when {
                i == 0 ->
                    formatTemplate.format(
                        p1Name,
                        p1Score,
                        p2Score,
                        p2Name,
                        boardLines[i]
                    )
                i == 1 ->
                    "+------------------+-----+-----+------------------+     " + boardLines[i]
                i == p1Moves.size+2 ->
                    formatTemplate.format(
                        "",
                        (p1Score - p1Moves.mapNotNull { it.move?.score }.sum()).toString(),
                        (p2Score - p2Moves.mapNotNull { it.move?.score }.sum()).toString(),
                        "",
                        boardLines.getOrNull(i) ?: ""
                    )
                i == p1Moves.size + 3 ->
                    "+------------------+-----+-----+------------------+     " + boardLines.getOrElse(
                        i,
                        defaultValue = { "" })
                i > p1Moves.size + 3 ->
                    "                                                        " + boardLines.getOrElse(
                        i,
                        defaultValue = { "" })
                else -> formatTemplate.format(
                    p1Moves.getOrNull(i-2)?.printSimulatedRounds() ?: "",
                    p1Moves.getOrNull(i-2)?.score() ?: "",
                    p2Moves.getOrNull(i-2)?.score() ?: "",
                    p2Moves.getOrNull(i-2)?.printSimulatedRounds() ?: "",
                    boardLines.getOrNull(i) ?: ""
                )
            }
        )
    }
}

private fun Turn.printSimulatedRounds(): String {
    return when (this.turnType) {
        TurnType.MOVE -> this.move!!.word
        TurnType.SWAP -> "<swap [${this.tilesToSwap.joinToString("")}]>"
        TurnType.PASS -> "<pass>"
    }
}

private fun Turn.score(): String {
    return when (this.turnType) {
        TurnType.MOVE -> this.move!!.score
        TurnType.SWAP -> 0
        TurnType.PASS -> 0
    }.toString()
}