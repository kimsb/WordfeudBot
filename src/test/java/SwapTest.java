import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SwapTest {

    @Test
    public void testSwap() {
        Swap swap = new Swap();
        char[] chars = {'L', 'E', 'R', 'N', 'W', 'T', 'S'};
        char[] bestSwap = swap.getBestSwap(chars);

        assertEquals("LWS", new String(bestSwap));
    }

    @Test
    public void testSwap2() {
        Swap swap = new Swap();
        char[] chars = {'U', 'I', 'I', 'G', 'D', 'A', 'R'};
        char[] bestSwap = swap.getBestSwap(chars);

        assertEquals("UIGD", new String(bestSwap));
    }
}