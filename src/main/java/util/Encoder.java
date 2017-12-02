package util;

import wordfeudapi.domain.Tile;

public class Encoder {

    public static char fromNorwegian(char norwegianLetter) {
        switch (norwegianLetter) {
            case 'Æ' : return 'Q';
            case 'Ø' : return 'Z';
            case 'Å' : return 'X';
            case 'æ' : return 'q';
            case 'ø' : return 'z';
            case 'å' : return 'x';
            default: return norwegianLetter;
        }
    }

    public static char toNorwegian(char substituteLetter) {
        switch (substituteLetter) {
            case 'Q' : return 'Æ';
            case 'Z' : return 'Ø';
            case 'X' : return 'Å';
            case 'q' : return 'æ';
            case 'z' : return 'ø';
            case 'x' : return 'å';
            default: return substituteLetter;
        }
    }

    public static Tile[] encodeTiles(Tile[] tiles) {
        for (int i = 0; i < tiles.length; i++) {
            tiles[i].setCharacter(toNorwegian(tiles[i].getCharacter()) + "");
        }
        return tiles;
    }

    public static String encodeWord(String word) {
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = toNorwegian(chars[i]);
        }
        return new String(chars);
    }
}
