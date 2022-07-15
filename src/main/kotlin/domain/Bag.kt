package domain

data class Bag(
  var tiles: List<Char>
) {
  fun pickTiles(count: Int): List<Char> {
    val removed = tiles.subList(0, minOf(tiles.size, count))
    tiles = tiles.drop(removed.size)
    return removed
  }

  fun swapTiles(toSwap: List<Char>): List<Char> {
    check(tiles.size >= 7) { "Trying to swap when bag only contains ${tiles.size} letters" }
    val removed = tiles.subList(0, toSwap.size)
    tiles = tiles.drop(toSwap.size)
    tiles = tiles + toSwap
    tiles = tiles.shuffled()
    return removed
  }
}