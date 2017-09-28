import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RelativeMoveScoreTest {

    @BeforeClass
    public static void setUp() {
        Dictionary.initialize();
    }

    @Test
    public void testDictionary() {
        boolean test = Dictionary.getDictionary().contains("TEST");
        assertThat(test, is(true));
    }

}