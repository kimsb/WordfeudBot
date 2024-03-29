object Constants {

  const val VALID_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ"
  const val letterDistributionWF =
    "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRRSSSSSSSTTTTTTTUUUVVVWYÆØØÅÅ**"

  const val letterDistributionScrabble =
    "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRSSSSSSTTTTTTUUUVVVWYÆØØÅÅ**"

  private val letterScoresWF =
    mapOf(
      Pair('A', 1),
      Pair('B', 4),
      Pair('C', 10),
      Pair('D', 1),
      Pair('E', 1),
      Pair('F', 2),
      Pair('G', 4),
      Pair('H', 3),
      Pair('I', 2),
      Pair('J', 4),
      Pair('K', 3),
      Pair('L', 2),
      Pair('M', 2),
      Pair('N', 1),
      Pair('O', 3),
      Pair('P', 4),
      Pair('R', 1),
      Pair('S', 1),
      Pair('T', 1),
      Pair('U', 4),
      Pair('V', 5),
      Pair('W', 10),
      Pair('Y', 8),
      Pair('Æ', 8),
      Pair('Ø', 4),
      Pair('Å', 4)
    )

  private val letterScoresScrabble =
    mapOf(
      Pair('A', 1),
      Pair('B', 4),
      Pair('C', 10),
      Pair('D', 1),
      Pair('E', 1),
      Pair('F', 2),
      Pair('G', 2),
      Pair('H', 3),
      Pair('I', 1),
      Pair('J', 4),
      Pair('K', 2),
      Pair('L', 1),
      Pair('M', 2),
      Pair('N', 1),
      Pair('O', 2),
      Pair('P', 4),
      Pair('R', 1),
      Pair('S', 1),
      Pair('T', 1),
      Pair('U', 4),
      Pair('V', 4),
      Pair('W', 8),
      Pair('Y', 6),
      Pair('Æ', 6),
      Pair('Ø', 5),
      Pair('Å', 4)
    )

  var platform = "WF"

  fun letterScore(letter: Char): Int {
    return if (platform == "WF") letterScoresWF.getOrElse(letter) { 0 }
    else letterScoresScrabble.getOrElse(letter) { 0 }
  }

  fun bingoScore(): Int {
    return if (platform == "WF") 40
    else 50
  }
}
