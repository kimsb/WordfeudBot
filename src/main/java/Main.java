import mdag.MDAG;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        new Bot(main.createDictionary());
    }

    private MDAG createDictionary() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("nsf2016.txt").getFile());
            return new MDAG(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
