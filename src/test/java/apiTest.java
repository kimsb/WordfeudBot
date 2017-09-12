import nu.mrpi.wordfeudapi.RestWordFeudClient;
import nu.mrpi.wordfeudapi.domain.Game;
import nu.mrpi.wordfeudapi.domain.User;
import org.junit.Test;

public class apiTest {

    RestWordFeudClient restWordFeudClient = new RestWordFeudClient();


    @Test
    public void testings() {
        User user = restWordFeudClient.logon("kbovim@hotmail.com", "h7kawr0y");
        System.out.println("test");

        Game[] games = restWordFeudClient.getGames();

    }

}
