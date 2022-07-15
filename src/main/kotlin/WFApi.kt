import domain.Board
import domain.Move
import domain.Rack
import domain.TurnType.*
import wordfeudapi.RestWordFeudClient
import wordfeudapi.domain.Game
import wordfeudapi.domain.TileMove
import java.lang.Thread.sleep

class WFApi(
  private val bot: MyBot
) {

  private val wfClient = RestWordFeudClient()
  private val moominClient = RestWordFeudClient()

  init {
    wfClient.logon(bot.name, System.getenv("${bot.name.uppercase()}_PASSWORD"))
    moominClient.logon(System.getenv("MOOMIN_USERNAME"), System.getenv("MOOMIN_PASSWORD"))
    println("Logged in as ${bot.name}")
    botLoop()
  }

  private fun botLoop() {
    while (true) {
      acceptInvites()

      val gameIdsMyTurn = wfClient.games
        .filter(Game::isRunning)
        .filter(Game::isMyTurn)
        .map(Game::getId)

      gameIdsMyTurn.forEach {
        val game = wfClient.getGame(it)
        makeMove(game)
        sendTips(game) //TODO sender nå ikke tips i første trekk om moomin starter kampen
      }

      if (gameIdsMyTurn.isEmpty()) {
        sleep(5000)
      }
    }
  }

  private fun acceptInvites() {
    wfClient.status.invitesReceived.forEach {
      //Only accept norwegian bokmål
      if (it.ruleset.apiIntRepresentation == 1) {
        println("Starting game against ${it.inviter}")
        wfClient.acceptInvite(it.id)
      } else {
        wfClient.rejectInvite(it.id)
      }
    }
  }

  private fun makeMove(game: Game) {
    val turn = bot.makeTurn(
      domain.Game(
        Board(wfClient.getBoard(game), game.tiles),
        Rack(game.myRack.chars().toList()),
        score = game.me.score,
        opponentScore = game.opponent.score
      )
    )
    print("Against ${game.opponentName}: ")
    when (turn.turnType) {
      MOVE -> {
        val tileMove = turn.move!!.toTileMove()
        println("Playing ${tileMove.word} for ${tileMove.points} points")
        wfClient.makeMove(game, tileMove)
      }
      SWAP -> { //TODO finne ut hvordan swappe blank / bruke bestSwap-logikk
        val toSwap = turn.tilesToSwap.filter { it != '*' }
        println("Swapping [${toSwap.joinToString("")}]")
        wfClient.swap(game, toSwap.toCharArray())
      }
      PASS -> {
        println("Passing")
        wfClient.pass(game)
      }
    }
  }

  private fun sendTips(game: Game) {
    if (!game.opponentName.equals("moomin85")) {
      return
    }
    val moominGame = moominClient.getGame(game.id)
    val allMovesSorted = bot.getAllMovesSorted(
      domain.Game(
        Board(moominClient.getBoard(moominGame), moominGame.tiles),
        Rack(moominGame.myRack.chars().toList()),
        score = moominGame.me.score,
        opponentScore = moominGame.opponent.score
      )
    )
    //highest scoring tips
    if (allMovesSorted.isNotEmpty()) {
      println("Sending chatMessage to moomin85")

      val message = highestScoringTips(allMovesSorted) +
          bingoTips(allMovesSorted) +
          swapTips(moominGame)

      println("message: $message")

      wfClient.chat(game.id, message)
    }
  }

  private fun highestScoringTips(allMovesSorted: List<Move>): String {
    val highestScoringWord = allMovesSorted.first()
    return "Høyest scorende legg:\n" +
        "${highestScoringWord.score}: ${highestScoringWord.word} ${highestScoringWord.toTileMove().chatMovePosition()}"
  }

  private fun bingoTips(allMovesSorted: List<Move>): String {
    if (allMovesSorted.first().addedTiles.size != 7) {
      val bingos = allMovesSorted.filter { it.addedTiles.size == 7 }
      if (bingos.isNotEmpty()) {
        val bingo = bingos.first()
        return "\nDu kan legge bingo:\n" +
            "${bingo.score}: ${bingo.word} ${bingo.toTileMove().chatMovePosition()}"
      }
    }
    return ""
  }

  private fun swapTips(game: Game): String {
    val rack = String(game.myRack.chars())
    if (game.bagCount >= 7) {
      val usedTiles = game.tiles.map { it.character }
      val bestSwap = allSwapsSorted(rack, usedTiles).first()
      if (bestSwap.second.equals(1.0)) {
        return "\nDu har bingo på hånda!"
      }
      return "\nBrikkene ${bestSwap.first} gir ${String.format("%.2f%%", bestSwap.second * 100)} sjanse for bingo"
    }
    return ""
  }

}
