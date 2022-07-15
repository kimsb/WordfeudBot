package wordfeudapi.domain;

/**
 * @author Pierre Ingmansson
 */
public class TileMove implements Comparable<TileMove> {
    private final ApiTile[] apiTiles;
    private final String word;
    private final int points;
    private final boolean horizontalWord;

    public TileMove(final ApiTile[] apiTiles, final String word, final int points, final boolean horizontalWord) {
        this.apiTiles = apiTiles;
        this.word = word;
        this.points = points;
        this.horizontalWord = horizontalWord;
    }

    public ApiTile[] getApiTiles() {
        return apiTiles;
    }

    public String getWord() {
        return word;
    }

    public int getPoints() {
        return points;
    }

    public boolean isHorizontalWord() {
        return horizontalWord;
    }

    public String chatMovePosition() {
        int x = "ABCDEFGHIJKLMNO".charAt(apiTiles[0].getX());
        String arrow = isHorizontalWord() ? "\u21E2" : "\u21E3";
        return "(" + x + (apiTiles[0].getY() + 1) + arrow + ")";
    }

    @Override
    public int compareTo(TileMove other) {
        return other.getPoints() - getPoints();
    }
}
