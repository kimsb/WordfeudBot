import scrabble.Scrabble
import simulation.Simulator

object Main {

  @JvmStatic
  fun main(args: Array<String>) {

    val myBot = MyBot(System.getenv("WF_BOTNAME"))

    //The real deal
    WFApi(bot = myBot)

    //Scrabble game
    Constants.platform = "SCRABBLE"
    Scrabble(bot = myBot).play()

    //Simulation
    Simulator(
      bot = myBot,
      controlBot = ControlBot()
    ).simulate(
      rounds = 100
    )
  }
}
