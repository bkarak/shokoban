package org.bkarak.sokoban;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The index of bundled levels and skins, read from {@code maps.cfg} and {@code skins.cfg}.
 * Unlike the applet's {@code Hashtable}, entries keep the order they are declared in.
 */
public final class Catalog {

    private final Map<String, String> levels;
    private final Map<String, String> skins;

    private Catalog(Map<String, String> levels, Map<String, String> skins) {
        this.levels = levels;
        this.skins = skins;
    }

    public static Catalog bundled() throws IOException {
        Catalog catalog = new Catalog(parseIndex(Resources.readText("maps.cfg")),
                parseIndex(Resources.readText("skins.cfg")));
        if (catalog.levels.isEmpty() || catalog.skins.isEmpty()) {
            throw new IOException("catalog is empty: maps.cfg or skins.cfg has no entries");
        }
        return catalog;
    }

    static Map<String, String> parseIndex(String text) {
        Map<String, String> index = new LinkedHashMap<>();
        for (String chunk : text.split("%%|\\R")) {
            String entry = chunk.strip();
            if (entry.isEmpty() || entry.startsWith("#")) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            index.put(entry.substring(0, separator).strip(), entry.substring(separator + 1).strip());
        }
        return index;
    }

    public List<String> levelNames() {
        return new ArrayList<>(levels.keySet());
    }

    public List<String> skinNames() {
        return new ArrayList<>(skins.keySet());
    }

    public Level loadLevel(String name) throws IOException {
        String path = levels.get(name);
        if (path == null) {
            throw new IOException("no such level: " + name);
        }
        return LevelParser.parse(Resources.readText(path), name);
    }

    public Skin loadSkin(String name) throws IOException {
        String directory = skins.get(name);
        if (directory == null) {
            throw new IOException("no such skin: " + name);
        }
        return Skin.load(name, directory);
    }

    /** The level after {@code name}, wrapping around at the end of the list. */
    public String levelAfter(String name) {
        List<String> names = levelNames();
        int index = names.indexOf(name);
        return names.get((index + 1) % names.size());
    }
}
