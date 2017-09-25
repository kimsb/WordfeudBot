import domain.BoardDO;
import domain.MoveDO;
import mdag.MDAG;
import wordfeudapi.domain.Board;
import wordfeudapi.domain.Game;
import wordfeudapi.domain.Tile;
import wordfeudapi.domain.TileMove;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelativeMoveScore {

    private MDAG dictionary;
    private Board board;

    RelativeMoveScore(final MDAG dictionary, final Board board) {
        this.dictionary = dictionary;
        this.board = board;
    }

    public TileMove findBestRelativeMove(final Game game, final List<TileMove> bestMoves) {
        final List<String> randomOpponentRacks = getRandomOpponentRacks(game);

        TreeMap<Double, TileMove> relativeMoveScores = new TreeMap<>(Collections.reverseOrder());

        //15p straff for bruk av blank
        TreeMap<Double, TileMove> bestMovesTreeMap = new TreeMap<>(Collections.reverseOrder());
        double differential = 0.0000001;
        for (TileMove tileMove : bestMoves) {
            if (tileMove.getWord().equals(tileMove.getWord().toUpperCase())) {
                bestMovesTreeMap.put(tileMove.getPoints() + differential, tileMove);
            } else {
                bestMovesTreeMap.put(tileMove.getPoints() + differential - 15, tileMove);
            }
            differential += 0.0000001;
        }
        //vurderer bare de maks 50 beste
        ArrayList<TileMove> selectedBestMoves = new ArrayList<>();
        int count = 0;
        while (!bestMovesTreeMap.isEmpty() && count < 30) {
            selectedBestMoves.add(bestMovesTreeMap.remove(bestMovesTreeMap.firstKey()));
            count++;
        }

        for (TileMove tileMove : selectedBestMoves) {
            Tile[] tiles = Stream.of(game.getTiles(), tileMove.getTiles()).flatMap(Stream::of).toArray(Tile[]::new);
            BoardDO boardDO = new BoardDO(tiles);
            MoveFinder moveFinder = new MoveFinder(board);
            double sum = 0;
            for (String opponentRack : randomOpponentRacks) {
                List<TileMove> tileMoves = moveFinder.findAllMoves(dictionary, boardDO, opponentRack).stream()
                        .map(MoveDO::toTileMove)
                        .sorted(Comparator.comparingInt(TileMove::getPoints))
                        .collect(Collectors.toList());
                TileMove opponentMove = tileMoves.get(tileMoves.size() - 1);
                sum += opponentMove.getPoints();
            }
            double averageTopScore = sum / randomOpponentRacks.size();
            relativeMoveScores.put(tileMove.getPoints() - averageTopScore, tileMove);
        }
        return relativeMoveScores.firstEntry().getValue();
    }

    private List<String> getRandomOpponentRacks(final Game game) {
        final List<String> remainingLetters = findRemainingLetters(new String(game.getMyRack().chars()), game.getTiles());

        ArrayList<String> opponentRacks = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            Collections.shuffle(remainingLetters);
            StringBuilder letters = new StringBuilder();
            for (int j = 0; j < 7; j++) {
                letters.append(remainingLetters.get(j));
            }
            opponentRacks.add(letters.toString().replace("-", "*"));
        }
        return opponentRacks;
    }

    private List<String> findRemainingLetters(final String cpuRack, final Tile[] tiles) {
        final String cpuRackFormattedBlank = cpuRack.replace('*', '-');
        String allTiles = "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRRSSSSSSSTTTTTTTUUUVVVWYÆØØÅÅ--";
        for (Tile tile : tiles) {
            allTiles = allTiles.replaceFirst(Character.toString(tile.getCharacter()), "");
        }
        for (char character : cpuRackFormattedBlank.toCharArray()) {
            allTiles = allTiles.replaceFirst(Character.toString(character), "");
        }
        ArrayList<String> letters = new ArrayList<>();
        for (char letter : allTiles.toCharArray()) {
            letters.add(Character.toString(letter));
        }
        return letters;
    }
}
