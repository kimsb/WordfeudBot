import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SwapperTest {

  @Test
  fun `should get right percentage`() {
    val rack = "AHINNVÅ"
    val bag = "ABDFIKLOORRSSSTTYØØÅ"
    var fullBag = "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRRSSSSSSSTTTTTTTUUUVVVWYÆØØÅÅ"
    bag.forEach { fullBag = fullBag.replaceFirst(it, '-') }
    val usedTiles = fullBag.replace("-", "").toList()

    val allSwapsSorted = allSwapsSorted(rack, usedTiles)
    //HÅRVANN (2 av 20)
    val find = allSwapsSorted.find { it.first == "AHNNVÅ" }

    assertThat(find!!.second).isEqualTo(0.1)
  }

  //TODO
  @Test
  fun `should handle blanks`() {

  }

}