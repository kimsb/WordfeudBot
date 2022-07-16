import scrabble.Scrabble
import simulation.Simulator

object Main {

  @JvmStatic
  fun main(args: Array<String>) {

    val myBot = MyBot(System.getenv("WF_BOTNAME"))

    //Scrabble game
    if (args.isNotEmpty() && args[0] == "SCRABBLE") {
      Constants.platform = "SCRABBLE"
      Scrabble(bot = myBot).play()
    }

    //The real deal
    WFApi(bot = myBot)

    //Simulation
    Simulator(
      bot = myBot,
      controlBot = ControlBot()
    ).simulate(
      rounds = 100
    )
  }
}
