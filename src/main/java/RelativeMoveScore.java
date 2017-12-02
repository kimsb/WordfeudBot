import domain.BoardDO;
import domain.MoveDO;
import wordfeudapi.domain.Board;
import wordfeudapi.domain.Game;
import wordfeudapi.domain.Tile;
import wordfeudapi.domain.TileMove;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelativeMoveScore {

    private Board board;

    RelativeMoveScore(final Board board) {
        this.board = board;
    }

    public TileMove findBestRelativeMove(final Game game, final List<TileMove> bestMoves) {
        final List<String> remainingLetters = findRemainingLetters(new String(game.getMyRack().chars()), game.getTiles());
        final List<String> randomOpponentRacks = getRandomOpponentRacks(remainingLetters);
        return findBestRelativeMove(randomOpponentRacks, bestMoves, game.getTiles());
    }

    protected TileMove findBestRelativeMove(final List<String> randomOpponentRacks, final List<TileMove> bestCPUMoves,
                                            final Tile[] tilesOnBoard) {

        //20p straff for bruk av blank
        TreeMap<Double, TileMove> bestMovesTreeMap = new TreeMap<>(Collections.reverseOrder());
        double differential = 0.0000001;
        for (TileMove tileMove : bestCPUMoves) {
            if (tileMove.getWord().equals(tileMove.getWord().toUpperCase())) {
                bestMovesTreeMap.put(tileMove.getPoints() + differential, tileMove);
            } else {
                bestMovesTreeMap.put(tileMove.getPoints() + differential - 20, tileMove);
            }
            differential += 0.0000001;
        }
        //vurderer bare de maks n beste
        ArrayList<TileMove> selectedBestMoves = new ArrayList<>();
        int count = 0;
        while (!bestMovesTreeMap.isEmpty() && count < 50) {
            selectedBestMoves.add(bestMovesTreeMap.remove(bestMovesTreeMap.firstKey()));
            count++;
        }

        TreeMap<Double, TileMove> parallell = parallell(randomOpponentRacks, selectedBestMoves, tilesOnBoard);

        return parallell != null ? parallell.isEmpty() ? null : parallell.lastEntry().getValue() : null;
    }

    private TreeMap<Double, TileMove> parallell(final List<String> randomOpponentRacks, final List<TileMove> selectedBestMoves, final Tile[] tilesOnBoard) {
        TreeMap<Double, TileMove> relativeMoveScores = selectedBestMoves.stream().parallel().collect(Collectors.toMap(
                tileMove -> {
                    Tile[] tiles = Stream.of(tilesOnBoard, tileMove.getTiles()).flatMap(Stream::of).toArray(Tile[]::new);
                    Double sum = randomOpponentRacks.stream().parallel().mapToDouble(
                            opponentRack -> {
                                BoardDO boardDO = new BoardDO(tiles);
                                List<TileMove> tileMoves = new MoveFinder(board).findAllMoves(boardDO, opponentRack).stream()
                                        .map(MoveDO::toTileMove)
                                        .sorted(Comparator.comparingInt(TileMove::getPoints))
                                        .collect(Collectors.toList());
                                if (!tileMoves.isEmpty()) {
                                    TileMove opponentMove = tileMoves.get(tileMoves.size() - 1);
                                    return opponentMove.getPoints();
                                }
                                return 0;
                            }
                    ).sum();
                    return tileMove.getPoints() - (sum / randomOpponentRacks.size());
                }, Function.identity(), (m1, m2) -> m1, TreeMap::new));
        return relativeMoveScores;
    }

    protected List<String> getRandomOpponentRacks(final List<String> remainingLetters) {
        ArrayList<String> opponentRacks = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            Collections.shuffle(remainingLetters);
            StringBuilder letters = new StringBuilder();
            for (int j = 0; j < 7 && j < remainingLetters.size(); j++) {
                letters.append(remainingLetters.get(j));
            }
            opponentRacks.add(letters.toString().replace("-", "*"));
        }
        return opponentRacks;
    }

    protected List<String> findRemainingLetters(final String cpuRack, final Tile[] tiles) {
        final String cpuRackFormattedBlank = cpuRack.replace('*', '-');
        String allTiles = "AAAAAAABBBCDDDDDEEEEEEEEEFFFFGGGGHHHIIIIIIJJKKKKLLLLLMMMNNNNNNOOOOPPRRRRRRRSSSSSSSTTTTTTTUUUVVVWYQZZXX--";
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
