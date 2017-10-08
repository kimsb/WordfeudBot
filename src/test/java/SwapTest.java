import org.junit.Assert;
import org.junit.Test;

public class SwapTest {

    @Test
    public void testSwap() {
        Swap swap = new Swap();
        char[] chars = {'L', 'E', 'R', 'N', 'W', 'T', 'S'};
        char[] bestSwap = swap.getBestSwap(chars);

        Assert.assertTrue("LWS".equals(new String(bestSwap)));
    }

    @Test
    public void testSwap2() {
        Swap swap = new Swap();
        char[] chars = {'U', 'I', 'I', 'G', 'D', 'A', 'R'};
        char[] bestSwap = swap.getBestSwap(chars);

        Assert.assertTrue("UIGD".equals(new String(bestSwap)));
    }
}