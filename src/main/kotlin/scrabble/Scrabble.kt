package scrabble

import Bot
import allSwapsSorted
import domain.*
import domain.TurnType.*
import simulation.toPrintableLines
import wordfeudapi.domain.ApiBoard
import kotlin.random.Random

//TODO scrabble-scores på letters
class Scrabble(val bot: Bot) {

  private val bag = Bag(Constants.letterDistribution.toList().shuffled())

  fun play() {
    val firstPlayerRack = bag.pickTiles(7)
    val secondPlayerRack = bag.pickTiles(7)
    val playerStarts = Random.nextBoolean()
    var playerRack = if (playerStarts) Rack(firstPlayerRack) else Rack(secondPlayerRack)
    var botRack = if (playerStarts) Rack(secondPlayerRack) else Rack(firstPlayerRack)
    var playerScore = 0
    var botScore = 0
    var board = emptyScrabbleBoard()
    val gameIsRunning = true
    val playerTurns: MutableList<PlayerTurn> = mutableListOf()

    while (gameIsRunning) {

      if (playerTurns.isNotEmpty() || playerStarts.not()) {
        val game = Game(
          board = board,
          rack = botRack,
          score = botScore,
          opponentScore = playerScore,
          scorelessTurns = 0 //TODO ta med dette her?
        )
        val botTurn = bot.makeTurn(game)
        when (botTurn.turnType) {
          MOVE -> {
            botScore += botTurn.move!!.score
            board = board.withMove(botTurn.move)
            botRack = botRack.swap(
              toSwap = botTurn.move.addedTiles.map { it.first.letter },
              newLetters = bag.pickTiles(botTurn.move.addedTiles.size)
            )
            println("Boten la ${botTurn.move.word} for ${botTurn.move.score} poeng")
            if (botRack.tiles.isEmpty()) {
              println("Kampen er ferdig!")
              break
            }
          }
          SWAP -> {
            botRack = botRack.swap(toSwap = botTurn.tilesToSwap, newLetters = bag.swapTiles(botTurn.tilesToSwap))
            println("Boten bytter ${botTurn.tilesToSwap.size} brikker")
          }
          PASS -> println("Boten passer")
        }
      }

      println("+---------------------------------------------+")
      board.toPrintableLines().forEach { println(it) }
      if (playerStarts) {
        println("Kim: $playerScore - Bot: $botScore")
      } else {
        println("Bot: $botScore - Kim: $playerScore")
      }
      println("Rack: ${String(playerRack.tiles.sorted().toCharArray())}")
      val allMovesSorted = board.findAllMovesSorted(playerRack)
      val tilesOnBoard = board.squares.flatten().filter { it.isOccupied() }.mapNotNull { it.getLetter() }
      val allSwapsSorted = allSwapsSorted(String(playerRack.tiles.toCharArray()), tilesOnBoard)
      var input = ""

      while (!inputIsValid(input)) {
        print("Trekk: ")
        input = readLine()!!
      }

      when {
        input == "PASS" -> {
          playerTurns.add(PlayerTurn(board, allMovesSorted, allSwapsSorted, Turn(PASS)))
        }
        input.startsWith("BYTT") -> {
          val toSwap = input.substring(5).toList()
          val pickedUp = bag.swapTiles(toSwap)
          println("du plukket opp: ${pickedUp.joinToString("")}")
          playerRack = playerRack.swap(toSwap = toSwap, newLetters = pickedUp)
          playerTurns.add(PlayerTurn(board, allMovesSorted, allSwapsSorted, Turn(SWAP, tilesToSwap = toSwap)))
        }
        else -> {
          val inputRow = if (input.contains(">")) input.substring(1, input.indexOf(">")).toInt()
          else input.substring(1, input.indexOf(" ")).toInt()
          val move = allMovesSorted.find {
            it.word == input.substringAfter(" ")
                && it.horizontal == input.contains(">")
                && it.row == inputRow
                && it.addedTiles.first().second.column == "ABCDEFGHIJKLMNO".indexOf(input.first())
          }
          if (move == null) {
            println("Buuuuh! Du har gjort et ugyldig trekk...")
            playerTurns.add(PlayerTurn(board, allMovesSorted, allSwapsSorted, Turn(PASS)))
          } else {
            println("Du la ${move.word} for ${move.score} poeng")
            playerScore += move.score
            playerTurns.add(PlayerTurn(board, allMovesSorted, allSwapsSorted, Turn(MOVE, move = move)))
            board = board.withMove(move)
            playerRack = playerRack.swap(
              toSwap = move.addedTiles.map { it.first.letter },
              newLetters = bag.pickTiles(move.addedTiles.size)
            )
            if (playerRack.tiles.isEmpty()) {
              println("Kampen er ferdig!")
              break
            }
          }
        }
      }
    }

    println("Tid for refleksjon!")
    playerTurns.forEachIndexed { index, playerTurn ->
      print("Trekk #$index:")
      val turn = playerTurn.turn
      when (playerTurn.turn.turnType) {
        MOVE -> println("Du la ${turn.move!!.word} for ${turn.move.score} poeng")
        SWAP -> println("Du byttet ${String(turn.tilesToSwap.toCharArray())}")
        PASS -> println("Du passet")
      }
      if (playerTurn.allMovesSorted.isEmpty()) {
        println("Du kunne ikke lagt et gyldig legg")
      } else {
        println("De høyest scorende leggene du kunne gjort:")
        playerTurn.allMovesSorted.subList(0, playerTurn.allMovesSorted.size.coerceAtMost(10)).forEach {
          println("${it.score}: ${it.word} ${it.toTileMove().chatMovePosition()} ")
        }
        println()
      }

      if (playerTurn.allSwapsSorted.isEmpty()) {
        println("Du kunnet ikke byttet")
      } else {
        println("De beste byttene du kunne gjort:")
        playerTurn.allSwapsSorted.subList(0, playerTurn.allSwapsSorted.size.coerceAtMost(10)).forEach {
          println("${it.first}: ${String.format("%.2f%%", it.second * 100)} ")
        }
        println()
      }

      println("La oss spille en kamp til!")
      play()
    }

  }
}

//TODO valider input
private fun inputIsValid(input: String): Boolean {
  if (input.isBlank()) return false
  return true
}

data class PlayerTurn(
  val board: Board,
  val allMovesSorted: List<Move>,
  val allSwapsSorted: List<Pair<String, Double>>,
  val turn: Turn
)

private fun emptyScrabbleBoard(): Board {
  val standardApiBoard = ApiBoard(
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
  return Board(standardApiBoard, emptyArray())
}