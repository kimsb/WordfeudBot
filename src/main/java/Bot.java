import domain.BoardDO;
import domain.MoveDO;
import mdag.MDAG;
import wordfeudapi.RestWordFeudClient;
import wordfeudapi.domain.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bot {

    MDAG dictionary;
    RestWordFeudClient botClient, kimClient, tomClient;
    User kim, moominBot, tom;
    HashMap<Long, String> bingoMessages = new HashMap<>();
    HashMap<Long, Integer> bagCount = new HashMap<>();


    Bot(MDAG dictionary) {
        this.dictionary = dictionary;
        botClient = new RestWordFeudClient();
        kimClient = new RestWordFeudClient();
        tomClient = new RestWordFeudClient();
        kim = kimClient.logon(System.getenv("KIMUSER"), System.getenv("KIMPASSWORD"));
        tom = tomClient.logon(System.getenv("TOMUSER"), System.getenv("TOMPASSWORD"));
        moominBot = botClient.logon(System.getenv("BOTUSER"), System.getenv("BOTPASSWORD"));
        botLoop();
    }

    void botLoop() {
        while (true) {
            //accept invites
            Stream.of(botClient.getStatus().getInvitesReceived())
                    .map(Invite::getId)
                    .forEach(id -> botClient.acceptInvite(id));

            List<Long> gameIds = Stream.of(botClient.getGames())
                    .filter(game -> game.isRunning() && game.isMyTurn())
                    .map(Game::getId)
                    .collect(Collectors.toList());
            if (gameIds.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Long id : gameIds) {
                Game game = botClient.getGame(id);

                //sjekk om bingotips skal gis
                int currentBagCount = Byte.toUnsignedInt(game.getBagCount());
                if (bagCount.containsKey(id) && bagCount.get(id) != currentBagCount + 7) {
                    if (bingoMessages.containsKey(id)) {
                        botClient.chat(id, bingoMessages.get(id));
                        System.out.println("Bingotips til " + game.getOpponentName() + bingoMessages.get(id));
                    }
                }
                bingoMessages.remove(id);

                List<TileMove> bestMoves = findBestMoves(game);
                if (bestMoves.isEmpty()) {
                    if (game.getBagCount() >= 7) {
                        botClient.swap(id, game.getMyRack().chars());
                    } else {
                        botClient.pass(id);
                    }
                } else {
                    TileMove bestMove = bestMoves.get(bestMoves.size() - 1);
                    botClient.makeMove(game, bestMove);
                    System.out.println("Mot " + game.getOpponentName() + ": legger \"" + bestMove.getWord() + "\" "
                            + "for " + bestMove.getPoints() + "poeng");

                    bagCount.put(id, Byte.toUnsignedInt(game.getBagCount()) - bestMove.getTiles().length);
                }

                //bingotips for moomin85/tobov!
                if ("moomin85".equals(game.getOpponentName())) {
                    List<TileMove> bingos = findBestMoves(kimClient.getGame(id)).stream()
                            .filter(tileMove -> tileMove.getTiles().length == 7)
                            .collect(Collectors.toList());
                    createChatMessage(game.getId(), bingos);
                }
                if ("tobov!".equals(game.getOpponentName())) {
                    List<TileMove> bingos = findBestMoves(kimClient.getGame(id)).stream()
                            .filter(tileMove -> tileMove.getTiles().length == 7)
                            .collect(Collectors.toList());
                    createChatMessage(game.getId(), bingos);
                }

            }
        }

    }

    private void createChatMessage(Long gameId, List<TileMove> bingos) {
        if (bingos.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder("Du kunne lagt bingo:");
        int tipsCount = 0;
        while (!bingos.isEmpty() && tipsCount < 3) {
            TileMove bingo = bingos.remove(bingos.size() - 1);
            message.append("\n").append(bingo.getWord()).append(" (").append(bingo.getPoints()).append(")");
            tipsCount++;
        }
        bingoMessages.put(gameId, message.toString());
    }

    List<TileMove> findBestMoves(Game game) {
        Rack rack = game.getMyRack();

        Board board = botClient.getBoard(game);
        Tile[] tiles = game.getTiles();

        ArrayList<MoveDO> allMoves = new MoveFinder(board).findAllMoves(dictionary, new BoardDO(tiles), new String(rack.chars()));

        return allMoves.stream()
                .map(MoveDO::toTileMove)
                .sorted(Comparator.comparingInt(TileMove::getPoints))
                .collect(Collectors.toList());
    }

}
