import org.junit.Test;
import wordfeudapi.RestWordFeudClient;
import wordfeudapi.domain.Game;
import wordfeudapi.domain.User;

public class apiTest {

    RestWordFeudClient restWordFeudClient = new RestWordFeudClient();


    @Test
    public void testings() {
        User user = restWordFeudClient.logon("kbovim@hotmail.com", "h7kawr0y");
        System.out.println("test");

        Game[] games = restWordFeudClient.getGames();

    }

}
