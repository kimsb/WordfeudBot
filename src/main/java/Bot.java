import domain.BoardDO;
import domain.MoveDO;
import wordfeudapi.RestWordFeudClient;
import wordfeudapi.domain.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Bot {

    private RestWordFeudClient botClient, kimClient, tomClient;
    private HashMap<Long, String> highestScoringMoves = new HashMap<>();
    private HashMap<Long, String> bingoMessages = new HashMap<>();
    private HashMap<Long, Integer> bagCount = new HashMap<>();
    private String BOTUSERNAME;
    private String KIMUSERNAME;
    private String TOMUSERNAME;


    Bot() {
        botClient = new RestWordFeudClient();
        kimClient = new RestWordFeudClient();
        tomClient = new RestWordFeudClient();
        KIMUSERNAME = kimClient.logon(System.getenv("KIMUSER"), System.getenv("KIMPASSWORD")).getUsername();
        TOMUSERNAME = tomClient.logon(System.getenv("TOMUSER"), System.getenv("TOMPASSWORD")).getUsername();
        BOTUSERNAME = botClient.logon(System.getenv("BOTUSER"), System.getenv("BOTPASSWORD")).getUsername();
        botLoop();
    }

    private void botLoop() {
        while (true) {
            acceptInvites();
            createTips(getTheirTurnGameIds());
            giveTips();
            final List<Long> myTurnGameIds = getMyTurnGameIds();

            if (myTurnGameIds.isEmpty()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (Long id : myTurnGameIds) {
                Game game = botClient.getGame(id);
                if (game.getRuleset().getApiIntRepresentation() != 1) {
                    botClient.pass(id);
                } else {
                    giveBingoTips(id, game);
                    makeBestMove(id, game);
                }
            }
        }
    }

    private void giveTips() {
        Notifications notifications = botClient.getNotifications();
        for (NotificationEntry entry : notifications.getEntries()) {
            if (entry.isChatMessage()) {
                long gameId = entry.getGameId();
                botClient.chat(gameId, highestScoringMoves.get(gameId));
                log(entry.getUsername(), entry.getGameId(), "sender chatmelding med tips om høyest scorende legg");
            }
        }
    }

    private void makeBestMove(Long id, Game game) {
        final List<TileMove> bestMoves = findBestMoves(game);

        Board board = botClient.getBoard(game);
        final long start = System.currentTimeMillis();
        final TileMove bestRelativeMove = new RelativeMoveScore(board).findBestRelativeMove(game, bestMoves);
        final long parallellTime = System.currentTimeMillis() - start;

        if (bestMoves.isEmpty()) {
            if (game.getBagCount() >= 7) {
                botClient.swap(id, new Swap().getBestSwap(game.getMyRack().chars()));
            } else {
                botClient.pass(id);
            }
        } else {
            TileMove bestOriginalMove = bestMoves.get(bestMoves.size() - 1);
            botClient.makeMove(game, bestRelativeMove);
            String logmessage = "(" + parallellTime + "ms) legger \"" + bestRelativeMove.getWord() + "\" "
                    + "for " + bestRelativeMove.getPoints() + "p " + movePosition(bestRelativeMove);
            if (!(movePosition(bestOriginalMove).equals(movePosition(bestRelativeMove))
                    && bestOriginalMove.getWord().equals(bestRelativeMove.getWord()))) {
                logmessage += ", ikke \"" + bestOriginalMove.getWord() + "\" "
                        + "for " + bestOriginalMove.getPoints() + "p " + movePosition(bestOriginalMove);
            }
            log(getOpponent(game), game.getId(), logmessage);
        }
    }

    private void giveBingoTips(final Long gameId, final Game game) {
        final int currentBagCount = Byte.toUnsignedInt(game.getBagCount());
        if (bagCount.containsKey(gameId) && bagCount.get(gameId) != currentBagCount + 7) {
            if (bingoMessages.containsKey(gameId) && bingoMessages.get(gameId) != null) {
                botClient.chat(gameId, bingoMessages.get(gameId));
                log(getOpponent(game), gameId, "sender chatmelding med bingotips");
            }
        }
        bingoMessages.remove(gameId);
    }

    private void createTips(final List<Long> theirTurnGameIds) {
        for (Long id : theirTurnGameIds) {
            final Game game = botClient.getGame(id);
            if (bingoMessages.containsKey(id)) {
                break;
            }
            if (KIMUSERNAME.equals(game.getOpponentName())) {
                createChatMessage(id, kimClient);
            } else if (TOMUSERNAME.equals(game.getOpponentName())) {
                createChatMessage(id, tomClient);
            }
        }
    }

    private List<Long> getTheirTurnGameIds() {
        return Stream.of(botClient.getGames())
                .filter(game -> game.isRunning() && !game.isMyTurn())
                .map(Game::getId)
                .collect(Collectors.toList());
    }

    private List<Long> getMyTurnGameIds() {
        return Stream.of(botClient.getGames())
                .filter(game -> game.isRunning() && game.isMyTurn())
                .map(Game::getId)
                .collect(Collectors.toList());
    }

    private void acceptInvites() {
        //TODO: bare accept norsk-bokmål
        Stream.of(botClient.getStatus().getInvitesReceived())
                .forEach(invite -> {
                    /*if (invite.getRuleset().getApiIntRepresentation() != 1) {
                        botClient.rejectInvite(invite.getId());
                    }*/
                    final long gameId = botClient.acceptInvite(invite.getId());
                    log(invite.getInviter(), gameId, "accepted invite");
                });
    }

    private void createChatMessage(final Long gameId, final RestWordFeudClient client) {
        final Game game = client.getGame(gameId);
        final List<TileMove> bestMoves = findBestMoves(game);
        final List<TileMove> bingos = bestMoves.stream()
                .filter(tileMove -> tileMove.getTiles().length == 7)
                .collect(Collectors.toList());
        if (bestMoves.isEmpty()) {
            highestScoringMoves.put(gameId, "Du kan ikke gjøre noen gyldige legg");
            bingoMessages.remove(gameId);
            return;
        }
        highestScoringMoves.put(gameId, "Høyest scorende legg: " + moveStringBuilder(bestMoves.get(bestMoves.size() - 1)));
        if (bingos.isEmpty()) {
            bingoMessages.remove(gameId);
            return;
        }
        StringBuilder message = new StringBuilder();
        int tipsCount = 0;
        while (!bingos.isEmpty() && tipsCount < 3) {
            TileMove bingo = bingos.remove(bingos.size() - 1);
            message.append(moveStringBuilder(bingo));
            tipsCount++;
        }
        if (!bingos.isEmpty()) {
            message.append("\n...");
        }
        log(getOpponent(game), gameId, "kan legge bingo: " + message.toString().replaceFirst("\n", "")
                .replace("\n", ", "));
        bingoMessages.put(gameId, "Du kunne lagt bingo:" + message.toString());
        bagCount.put(gameId, Byte.toUnsignedInt(game.getBagCount()));

    }

    private StringBuilder moveStringBuilder(final TileMove tileMove) {
        return new StringBuilder("\n").append(tileMove.getPoints()).append(": ").append(tileMove.getWord())
                .append(" ").append(movePosition(tileMove));
    }

    private String movePosition(final TileMove tileMove) {
        final char x = "ABCDEFGHIJKLMNO".charAt(tileMove.getTiles()[0].getX());
        final String arrow = tileMove.isHorizontalWord() ? "\u21E2" : "\u21E3";
        return "(" + x + (tileMove.getTiles()[0].getY() + 1) + arrow + ")";
    }

    private List<TileMove> findBestMoves(Game game) {
        Rack rack = game.getMyRack();

        Board board = botClient.getBoard(game);
        Tile[] tiles = game.getTiles();

        ArrayList<MoveDO> allMoves = new MoveFinder(board).findAllMoves(new BoardDO(tiles), new String(rack.chars()));

        return allMoves.stream()
                .map(MoveDO::toTileMove)
                .sorted(Comparator.comparingInt(TileMove::getPoints))
                .collect(Collectors.toList());
    }

    private void log(final String opponent, final Long gameId, final String logMessage) {
        System.out.println(opponent + " (" + gameId + "): " + logMessage);
    }

    private String getOpponent(final Game game) {
        return BOTUSERNAME.equals(game.getFirstPlayerName()) ? game.getSecondPlayerName() : game.getFirstPlayerName();
    }

}
