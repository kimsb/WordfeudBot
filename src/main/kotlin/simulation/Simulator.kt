package simulation

import Bot
import Constants
import domain.*
import domain.TurnType.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import wordfeudapi.domain.ApiBoard
import kotlin.system.measureTimeMillis

class Simulator(
    val bot: Bot,
    val controlBot: Bot,
    private val letterDistribution: String? = null
) {

    fun simulate(rounds: Int) {
        val simulatedRounds: List<SimulatedRound>
        val time = measureTimeMillis {
            val deferredSimulationResults = (1..rounds).map { i ->
                GlobalScope.async {
                    println("Starting simulation of round #$i")
                    simulateRound()
                }
            }
            runBlocking {
                simulatedRounds = deferredSimulationResults.awaitAll()
            }
        }
        printSimulatedRounds(bot, controlBot, simulatedRounds, time)
    }

    private fun simulateRound(): SimulatedRound {
        val letters = letterDistribution?.toList() ?: Constants.letterDistribution.toList().shuffled()

        return SimulatedRound(
            initialLetterDistribution = letters.joinToString(""),
            simulatedGameA = simulateGame(bag = Bag(letters), myBotStarts = true),
            simulatedGameB = simulateGame(bag = Bag(letters), myBotStarts = false)
        )
    }

    private fun simulateGame(bag: Bag, myBotStarts: Boolean): SimulatedGame {
        var player1 = Player(bot = if (myBotStarts) bot else controlBot, rack = Rack(bag.pickTiles(7)))
        var player2 = Player(bot = if (myBotStarts) controlBot else bot, rack = Rack(bag.pickTiles(7)))
        var board = emptyWFBoard()
        var scorelessTurns = 0
        var player1sTurn = true
        var gameIsRunning = true

        while (gameIsRunning) {

            val playerInMove = if (player1sTurn) player1 else player2
            val opponent = if (player1sTurn) player2 else player1
            val game = Game(
                board = board,
                rack = playerInMove.rack,
                score = playerInMove.score,
                opponentScore = opponent.score,
                scorelessTurns = scorelessTurns
            )
            val turn = playerInMove.bot.makeTurn(game)
            playerInMove.turns.add(turn)

            when (turn.turnType) {
                MOVE -> {
                    scorelessTurns = 0
                    board = game.board.withMove(turn.move!!)
                    playerInMove.score = playerInMove.score + turn.move.score
                    playerInMove.rack = playerInMove.rack.swap(
                        toSwap = turn.move.addedTiles.map { it.first.letter },
                        newLetters = bag.pickTiles(turn.move.addedTiles.size)
                    )
                }
                SWAP -> {
                    scorelessTurns++
                    playerInMove.rack =
                        playerInMove.rack.swap(
                            toSwap = turn.tilesToSwap,
                            newLetters = bag.swapTiles(turn.tilesToSwap)
                        )
                }
                PASS -> {
                    scorelessTurns++
                }
            }

            if (scorelessTurns == 3) {
                playerInMove.score = playerInMove.score - playerInMove.rack.score()
                opponent.score = opponent.score - opponent.rack.score()
                gameIsRunning = false
            } else if (playerInMove.rack.tiles.isEmpty()) {
                playerInMove.score = playerInMove.score + opponent.rack.score()
                opponent.score = opponent.score - opponent.rack.score()
                gameIsRunning = false
            }

            player1 = if (player1sTurn) playerInMove else opponent
            player2 = if (player1sTurn) opponent else playerInMove
            player1sTurn = !player1sTurn
        }

        val myPlayer = if (myBotStarts) player1 else player2
        val opponent = if (myBotStarts) player2 else player1
        return SimulatedGame(
            board = board,
            myBotScore = myPlayer.score,
            controlBotScore = opponent.score,
            myBotTurns = myPlayer.turns,
            controlBotTurns = opponent.turns
        )
    }

    data class Player(
        val bot: Bot,
        var rack: Rack,
        var turns: MutableList<Turn> = mutableListOf(),
        var score: Int = 0,
    )

    data class SimulatedRound(
        val initialLetterDistribution: String,
        val simulatedGameA: SimulatedGame,
        val simulatedGameB: SimulatedGame
    )

    data class SimulatedGame(
        val board: Board,
        val myBotTurns: List<Turn>,
        val controlBotTurns: List<Turn>,
        val myBotScore: Int,
        val controlBotScore: Int
    )

    private fun emptyWFBoard(): Board {
        val standardApiBoard = ApiBoard(
            arrayOf(
                intArrayOf(2, 0, 0, 0, 4, 0, 0, 1, 0, 0, 4, 0, 0, 0, 2),
                intArrayOf(0, 1, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1, 0),
                intArrayOf(0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0),
                intArrayOf(0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 2, 0, 0, 0),
                intArrayOf(4, 0, 0, 0, 3, 0, 1, 0, 1, 0, 3, 0, 0, 0, 4),
                intArrayOf(0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0),
                intArrayOf(0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0),
                intArrayOf(1, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 1),
                intArrayOf(0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0),
                intArrayOf(0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0),
                intArrayOf(4, 0, 0, 0, 3, 0, 1, 0, 1, 0, 3, 0, 0, 0, 4),
                intArrayOf(0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 2, 0, 0, 0),
                intArrayOf(0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0),
                intArrayOf(0, 1, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1, 0),
                intArrayOf(2, 0, 0, 0, 4, 0, 0, 1, 0, 0, 4, 0, 0, 0, 2)
            )
        )
        return Board(standardApiBoard, emptyArray())
    }
}