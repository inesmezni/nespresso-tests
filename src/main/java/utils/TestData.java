package utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lit les jeux de données du fichier testdata/testdata.csv (format : cle;valeur).
 */
public class TestData {

    private static final String FILE = "testdata/testdata.csv";
    private static final Map<String, String> DATA = new HashMap<>();

    static {
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE), StandardCharsets.UTF_8);
            for (String line : lines.subList(1, lines.size())) {   // on saute l'en-tête
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";", 2);
                DATA.put(parts[0].trim(), parts[1].trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire " + FILE, e);
        }
    }

    public static String get(String key) {
        String value = DATA.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Clé absente de " + FILE + " : " + key);
        }
        return value;
    }
}