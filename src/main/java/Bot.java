
import com.BoxOfC.MDAG.MDAG;
import nu.mrpi.wordfeudapi.RestWordFeudClient;
import nu.mrpi.wordfeudapi.domain.Game;
import nu.mrpi.wordfeudapi.domain.User;

import java.util.stream.Stream;

public class Bot {

    MDAG dictionary;
    RestWordFeudClient botClient, kimClient;
    User kim, moominBot;


    Bot(MDAG dictionary) {
        ConfigProperties configProperties = new ConfigProperties();
        this.dictionary = dictionary;
        botClient = new RestWordFeudClient();
        kimClient = new RestWordFeudClient();
        kim = kimClient.logon(configProperties.getProperty("kimuser"), configProperties.getProperty("kimpassword"));
        moominBot = botClient.logon(configProperties.getProperty("botuser"), configProperties.getProperty("botpassword"));
        botLoop();
    }

    void botLoop() {
        Stream.of(botClient.getGames())
                .mapToLong(Game::getId)
                .forEach(id -> findBestMove(botClient.getGame(id)));
    }

    void findBestMove(Game game) {
        System.out.println("Motspiller: " + game.getOpponentName());
    }

}
