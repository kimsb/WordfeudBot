import domain.Board
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
        sendTips(game)
      }

      if (gameIdsMyTurn.isEmpty()) {
        sleep(1000)
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
      SWAP -> { //TODO finne ut hvordan swappe blank
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
    if (allMovesSorted.isNotEmpty()) {
      val highestScoringWord = allMovesSorted.first()
      var chatMessage = "Høyest scorende legg:\n" +
          "${highestScoringWord.score}: ${highestScoringWord.word} ${chatMovePosition(highestScoringWord.toTileMove())}"

      if (highestScoringWord.addedTiles.size != 7) {
        val bingos = allMovesSorted.filter { it.addedTiles.size == 7 }
        if (bingos.isNotEmpty()) {
          val bingo = bingos.first()
          chatMessage = chatMessage +
              "\nDu kan legge bingo:\n" +
              "${bingo.score}: ${bingo.word} ${chatMovePosition(bingo.toTileMove())}"
        }
      }
      println("Sending chatMessage to moomin85")
      wfClient.chat(game.id, chatMessage)
    }
  }

  private fun chatMovePosition(tileMove: TileMove): String {
    val x = "ABCDEFGHIJKLMNO"[tileMove.apiTiles[0].x]
    val arrow = if (tileMove.isHorizontalWord) "\u21E2" else "\u21E3"
    return "(" + x + (tileMove.apiTiles[0].y + 1).toString() + arrow + ")"
  }


}
