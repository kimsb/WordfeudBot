import BingoAnagrams.getSourceNode
import mdag.MDAGNode

//TODO blank
const val startBag =
  "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRRSSSSSSSTTTTTTTUUUVVVWYÆØØÅÅ"
var startBagMap: Map<Char, Int> = emptyMap()

private fun initStartBagMap(usedTiles: List<Char>) {
  var bag = startBag
  usedTiles.forEach { bag = bag.replaceFirst(it, '-') }
  bag = bag.replace("-", "")
  startBagMap = bag.groupingBy { it }.eachCount()
}

fun allSwapsSorted(rack: String, usedTiles: List<Char>): List<Pair<String, Double>> {
  initStartBagMap(usedTiles)

  val sortedByDescending: List<Pair<String, Double>> = getPossibleSwaps(rack).map {
    it to swap1(it, 0, "", startBagMap.toMutableMap(), getSourceNode())
  }.sortedByDescending { it.second }

  return sortedByDescending
}

fun swap1(leave: String, leaveIndex: Int, trukket: String, bag: MutableMap<Char, Int>, node: MDAGNode): Double {

  if (node.isAcceptNode) {
    val tempMap = startBagMap.toMutableMap()
    return if (trukket.isEmpty()) {
      1.00
    } else
      trukket.mapIndexed { index, char ->
        val count = tempMap[char]!!
        tempMap[char] = count - 1
        count / (startBag.length.toDouble() - index)
      }.reduce { acc, i -> acc * i } * getPermutationCount(trukket)
  }

  var sum = 0.0

  if (leaveIndex < leave.length) {
    val current = leave[leaveIndex]
    val hej = node.outgoingTransitions.entries.filter {
      if (trukket.length + leave.length == 7) {
        it.key == current
      } else {
        it.key <= current
      }
    }
    hej.forEach { (char, node) ->
      if (current == char) {
        if (leaveIndex < leave.length) {
          sum += swap1(leave, leaveIndex + 1, trukket, bag, node)
        }
      } else if (bag.containsKey(char) && bag[char]!! > 0) {
        bag[char] = bag[char]!! - 1
        sum += swap1(leave, leaveIndex, trukket + char, bag, node)
        bag[char] = bag[char]!! + 1
      }
    }

  } else {
    val hej = node.outgoingTransitions.entries
    hej.forEach { (char, node) ->
      if (bag.containsKey(char) && bag[char]!! > 0) {
        bag[char] = bag[char]!! - 1
        sum += swap1(leave, leaveIndex, trukket + char, bag, node)
        bag[char] = bag[char]!! + 1
      }
    }
  }
  return sum

}

fun getPossibleSwaps(string: String): Set<String> {
  val sorted = string.toList().sorted()
  return sorted.mapIndexed { index, _ -> index }.powerset()
    .map { set -> set.map { index -> sorted[index] }.joinToString("") }
    .toSet()
}

fun <T> Collection<T>.powerset(): Set<Set<T>> =
  powerset(this, setOf(emptySet()))

private tailrec fun <T> powerset(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> =
  if (left.isEmpty()) acc
  else powerset(left.drop(1), acc + acc.map { it + left.first() })

//without duplicates
private fun getPermutationCount(string: String): Int {
  var permutationCount = permutationMap[string.length]!!

  var currentCharIndex = 0
  var currentCharCount = 1

  while (currentCharIndex + 1 < string.length) {
    if (string[currentCharIndex] == string[currentCharIndex + 1]) {
      currentCharCount++
    } else if (currentCharCount > 1) {
      permutationCount /= permutationMap[currentCharCount]!!
      currentCharCount = 1
    }
    currentCharIndex++
  }
  return permutationCount
}

//n!
val permutationMap =
  mapOf(
    Pair(1, 1),
    Pair(2, 2),
    Pair(3, 6),
    Pair(4, 24),
    Pair(5, 120),
    Pair(6, 720),
    Pair(7, 5040)
  )