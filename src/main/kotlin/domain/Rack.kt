package domain

import Constants.letterScore
import java.lang.IllegalArgumentException

data class Rack(
    val tiles: List<Char>
) {
    fun contains(letter: Char): Boolean {
        return tiles.any { it == letter }
    }

    fun without(letter: Char): Rack {
        val index = tiles.indexOf(if (letter.isLowerCase()) '*' else letter)
        return Rack(tiles.subList(0, index) + tiles.subList(index + 1, tiles.size))
    }

    fun swap(toSwap: List<Char>, newLetters: List<Char>): Rack {
        var tempTiles = tiles
        for (letter in toSwap) {
            val index = tempTiles.indexOf(if (letter.isLowerCase()) '*' else letter)
            if (index == -1) {
                throw IllegalArgumentException("Trying to swap tile: $letter which is not on rack")
            }
            tempTiles = tempTiles.subList(0, index) + tempTiles.subList(index + 1, tempTiles.size)
        }
        return Rack(tempTiles + newLetters)
    }

    fun score(): Int {
        return tiles.sumOf { letterScore(it) }
    }
}
