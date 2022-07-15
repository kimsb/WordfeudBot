import mdag.MDAG
import mdag.MDAGNode

object BingoAnagrams {

  private val instance: MDAG

  private const val VALID_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ"

  init {
    val words = Main::class.java.getResourceAsStream("anagrams.txt")!!
      .bufferedReader()
      .readLines()
      .filter(this::wordContainsValidLetters)
    instance = MDAG(words)
  }

  fun contains(word: String): Boolean {
    return instance.contains(word)
  }

  fun getSourceNode(): MDAGNode {
    return instance.sourceNode as MDAGNode
  }

  //TODO ta med blanke for Scrabble?
  private fun wordContainsValidLetters(word: String): Boolean {
    word.forEach {
      if (!VALID_LETTERS.contains(it)) {
        return false
      }
    }
    return true
  }
}
