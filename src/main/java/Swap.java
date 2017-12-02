import util.Encoder;

public class Swap {
    private static final String ernatslik = "-ERNATSLIK";
    private static final double MIN_VOWEL_RATIO = 0.25;
    private static final double MAX_VOWEL_RATIO = 0.7;


    public char[] getBestSwap(final char[] rack) {
        for (int i = 0; i < rack.length; i++) {
            rack[i] = Encoder.fromNorwegian(rack[i]);
        }
        System.out.print("Bot kaller Swap. Rack: " + new String(rack));
        StringBuilder ernatslikTiles = new StringBuilder();
        for (char letter : rack) {
            if (ernatslik.indexOf(letter) != -1) {
                ernatslikTiles.append(letter);
            }
        }
        char[] lettersToKeep = ernatslikTiles.toString().toCharArray();
        while (lettersToKeep.length > 0 && !hasOKVowelRatio(lettersToKeep)) {
            if (hasTooFewVowels(lettersToKeep)) {
                lettersToKeep = removeErnatslikChar(lettersToKeep, false);
            } else {
                lettersToKeep = removeErnatslikChar(lettersToKeep, true);
            }
        }
        StringBuilder lettersToSwapBuilder = new StringBuilder(String.valueOf(rack));
        for (char letter : lettersToKeep) {
            lettersToSwapBuilder.deleteCharAt(lettersToSwapBuilder.toString().indexOf(letter));
        }
        System.out.println(" bytter: " + lettersToSwapBuilder.toString());
        return lettersToSwapBuilder.toString().toCharArray();
    }

    private static char[] removeErnatslikChar(final char[] ernatslikChars, final boolean removeVowel) {
        StringBuilder ernatslikBuilder = new StringBuilder(String.valueOf(ernatslikChars));
        for (int i = ernatslik.length() - 1; i > 0; i--) {
            char ernatslikChar = ernatslik.charAt(i);
            if (!isVowel(ernatslikChar) && removeVowel) {
                continue;
            }
            int index = ernatslikBuilder.toString().indexOf(ernatslikChar);
            if (index != -1) {
                return ernatslikBuilder.deleteCharAt(index).toString().toCharArray();
            }
        }
        return ernatslikChars;
    }

    private static boolean hasOKVowelRatio(final char[] letters) {
        double vowelRatio = getVowelRatio(letters);
        return vowelRatio >= MIN_VOWEL_RATIO && vowelRatio <= MAX_VOWEL_RATIO;
    }

    private static boolean hasTooFewVowels(final char[] letters) {
        double vowelRatio = getVowelRatio(letters);
        return vowelRatio < MIN_VOWEL_RATIO;
    }

    private static double getVowelRatio(final char[] letters) {
        double vowels = 0;
        for (char letter : letters) {
            if (isVowel(letter)) {
                vowels++;
            }
        }
        return vowels / letters.length;
    }

    private static boolean isVowel(final char letter) {
        switch (letter) {
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'U':
            case 'Y':
            case 'Q':
            case 'Z':
            case 'X':
            case '-':
                return true;
            default:
                return false;
        }
    }
}
