import domain.BoardDO;
import domain.MoveDO;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wordfeudapi.domain.Board;
import wordfeudapi.domain.Tile;
import wordfeudapi.domain.TileMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RelativeMoveScoreTest {

    @BeforeClass
    public static void setUp() {
        Dictionary.initialize();
    }

    @Ignore
    @Test
    public void testDictionary() {
        boolean test = Dictionary.getDictionary().contains("TEST");
        assertThat(test, is(true));

        System.out.println("");
    }

    @Ignore
    @Test
    public void testParallellVersion() {
        Tile[] tilesAfter10Moves = makeNMoves(10);
        final RelativeMoveScore relativeMoveScore = new RelativeMoveScore(getStandardBoard());
        final List<String> remainingLetters = relativeMoveScore.findRemainingLetters("", tilesAfter10Moves);
        final List<String> randomOpponentRacks = relativeMoveScore.getRandomOpponentRacks(remainingLetters);

        ArrayList<MoveDO> allMoves = new MoveFinder(getStandardBoard()).findAllMoves(new BoardDO(tilesAfter10Moves), randomOpponentRacks.remove(0));
        List<TileMove> bestCPUMoves = allMoves.stream()
                .map(MoveDO::toTileMove)
                .sorted(Comparator.comparingInt(TileMove::getPoints))
                .collect(Collectors.toList());

        TileMove bestRelativeMove = relativeMoveScore.findBestRelativeMove(randomOpponentRacks, bestCPUMoves, tilesAfter10Moves);

        System.out.println("bestMove: " + bestRelativeMove.getWord() + " (" + bestRelativeMove.getPoints() + ")");
    }

    private Tile[] makeNMoves(final int moveCount) {
        final Board standardBoard = getStandardBoard();
        final MoveFinder moveFinder = new MoveFinder(standardBoard);
        final RelativeMoveScore relativeMoveScore = new RelativeMoveScore(standardBoard);
        Tile[] tilesOnBoard = new Tile[0];
        for (int i = 0; i < moveCount; i++) {
            final List<String> remainingLetters = relativeMoveScore.findRemainingLetters("", tilesOnBoard);
            final List<String> randomOpponentRacks = relativeMoveScore.getRandomOpponentRacks(remainingLetters);
            ArrayList<MoveDO> allMoves = moveFinder.findAllMoves(new BoardDO(tilesOnBoard), randomOpponentRacks.get(0));
            List<TileMove> bestMoves = allMoves.stream()
                    .map(MoveDO::toTileMove)
                    .sorted(Comparator.comparingInt(TileMove::getPoints))
                    .collect(Collectors.toList());
            TileMove bestOriginalMove = bestMoves.get(bestMoves.size() - 1);
            tilesOnBoard = Stream.of(tilesOnBoard, bestOriginalMove.getTiles()).flatMap(Stream::of).toArray(Tile[]::new);
        }
        return tilesOnBoard;
    }


    private static Board getStandardBoard() {
        int[] a = {1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1};
        int[] b = {0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0};
        int[] c = {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0};
        int[] d = {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
        int[] e = {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1};
        int[] f = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
        int[] g = {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
        int[] h = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0};
        int[] i = {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
        int[] j = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
        int[] k = {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1};
        int[] l = {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
        int[] m = {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0};
        int[] n = {0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0};
        int[] o = {1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1};
        return new Board(new int[][]{a, b, c, d, e, f, g, h, i, j, k, l, m, n, o});
    }

}