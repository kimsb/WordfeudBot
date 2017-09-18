import mdag.MDAG;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        new Bot(main.createDictionary());
    }

    private MDAG createDictionary() {

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("nsf2016.txt");
        List<String> dictionary = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.toList());
        return new MDAG(dictionary);
    }
}
