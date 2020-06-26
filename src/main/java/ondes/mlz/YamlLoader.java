package ondes.mlz;

import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;
import static ondes.mlz.Util.getResourceAsString;

public class YamlLoader {
    static boolean DB = false;

    public static Map loadFile(Path path) {
        if (DB) out.println("path: " + path);
        if (path.toFile().isDirectory()) return null;
        try {
            String text = Files.lines(path, StandardCharsets.UTF_8)
                .collect(joining("\n"));
            Yaml yaml = new Yaml();
            return yaml.load(text);
        } catch (Exception ex) {
            err.println("Exception reading file " + path + ": " + ex);
            return null;
        }
    }

    public static Map loadResource(String resName) {
        String text = getResourceAsString(resName);
        Yaml yaml = new Yaml();
        return yaml.load(text);

    }
}
