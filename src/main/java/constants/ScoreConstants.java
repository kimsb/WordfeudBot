package constants;

import java.util.HashMap;

public class ScoreConstants {

    private static final HashMap<Character, Integer> letterScores;
    static {
        letterScores = new HashMap<>();
        letterScores.put('A', 1);
        letterScores.put('B', 4);
        letterScores.put('C', 10);
        letterScores.put('D', 1);
        letterScores.put('E', 1);
        letterScores.put('F', 2);
        letterScores.put('G', 4);
        letterScores.put('H', 3);
        letterScores.put('I', 2);
        letterScores.put('J', 4);
        letterScores.put('K', 3);
        letterScores.put('L', 2);
        letterScores.put('M', 2);
        letterScores.put('N', 1);
        letterScores.put('O', 3);
        letterScores.put('P', 4);
        letterScores.put('R', 1);
        letterScores.put('S', 1);
        letterScores.put('T', 1);
        letterScores.put('U', 4);
        letterScores.put('V', 5);
        letterScores.put('W', 10);
        letterScores.put('Y', 8);
        letterScores.put('Æ', 8);
        letterScores.put('Ø', 4);
        letterScores.put('Å', 4);
        letterScores.put('-', 0);
    }

    private static final HashMap<Character, Double> relativeLetterScores;
    static {
        relativeLetterScores = new HashMap<>();
        relativeLetterScores.put('A', 3.6539);
        relativeLetterScores.put('B', - 1.4739);
        relativeLetterScores.put('C', - 10.4449);
        relativeLetterScores.put('D', - 1.2325);
        relativeLetterScores.put('E', - 4.6583);
        relativeLetterScores.put('F', - 2.5103);
        relativeLetterScores.put('G', - 1.6526);
        relativeLetterScores.put('H', - 3.0512);
        relativeLetterScores.put('I', 2.3689);
        relativeLetterScores.put('J', - 6.2484);
        relativeLetterScores.put('K', - 0.3689);
        relativeLetterScores.put('L', 0.4722);
        relativeLetterScores.put('M', - 2.3348);
        relativeLetterScores.put('N', 1.0184);
        relativeLetterScores.put('O', 0.1618);
        relativeLetterScores.put('P', - 2.5292);
        relativeLetterScores.put('R', 1.0918);
        relativeLetterScores.put('S', 0.6087);
        relativeLetterScores.put('T', 0.6279);
        relativeLetterScores.put('U', - 1.1800);
        relativeLetterScores.put('V', - 3.3475);
        relativeLetterScores.put('W', - 13.9083);
        relativeLetterScores.put('Y', - 2.8938);
        relativeLetterScores.put('Æ', - 4.3911);
        relativeLetterScores.put('Ø', - 2.6567);
        relativeLetterScores.put('Å', - 3.0975);
        relativeLetterScores.put('-', 5.0001);
    }

    public static int letterScore(char letter) {
        Integer letterScore = letterScores.get(letter);
        return letterScore == null ? 0 : letterScore;
    }

    public static double relativeLetterScore(char letter) {
        Double relativeLetterScore = relativeLetterScores.get(letter);
        return relativeLetterScore == null ? 0.0 : relativeLetterScore;
    }
}
