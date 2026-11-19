package com.allry.bookguide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

final class GuideContent {
    static final int MAX_TITLE_LENGTH = 32;

    private final Component title;
    private final String titlePlain;
    private final Component author;
    private final String authorPlain;
    private final Component description;
    private final Component intro;
    private final Component tocTitle;
    private final Component tocHint;
    private final List<Section> sections;

    private GuideContent(
            Component title,
            String titlePlain,
            Component author,
            String authorPlain,
            Component description,
            Component intro,
            Component tocTitle,
            Component tocHint,
            List<Section> sections
    ) {
        this.title = title;
        this.titlePlain = titlePlain;
        this.author = author;
        this.authorPlain = authorPlain;
        this.description = description;
        this.intro = intro;
        this.tocTitle = tocTitle;
        this.tocHint = tocHint;
        this.sections = sections;
    }

    static GuideContent load(File file) throws LoadException {
        if (!file.isFile()) {
            throw new LoadException("File guide.yml tidak ditemukan di " + file.getAbsolutePath());
        }

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new LoadException("guide.yml tidak bisa dibaca: " + e.getMessage(), e);
        }

        var book = yaml.getConfigurationSection("book");
        if (book == null) {
            throw new LoadException("Bagian 'book' tidak ditemukan di guide.yml.");
        }

        Component title = parse(book.getString("title"), "book.title");
        String titlePlain = TextParser.plain(title).trim();
        if (titlePlain.isEmpty()) {
            throw new LoadException("book.title tidak boleh kosong.");
        }
        if (titlePlain.length() > MAX_TITLE_LENGTH) {
            throw new LoadException("book.title maksimal " + MAX_TITLE_LENGTH + " karakter.");
        }

        Component author = parse(book.getString("author"), "book.author");
        String authorPlain = TextParser.plain(author).trim();
        if (authorPlain.isEmpty()) {
            throw new LoadException("book.author tidak boleh kosong.");
        }

        Component description = parse(book.getString("description"), "book.description");
        if (TextParser.plain(description).isBlank()) {
            throw new LoadException("book.description tidak boleh kosong.");
        }

        Component intro = parse(book.getString("intro", "Semua panduan dalam satu buku."), "book.intro");
        Component tocTitle = parse(book.getString("toc-title", "Daftar Isi"), "book.toc-title");
        Component tocHint = parse(book.getString("toc-hint", "Klik judul bab."), "book.toc-hint");

        List<Section> sections = parseSections(yaml.getMapList("sections"));
        return new GuideContent(title, titlePlain, author, authorPlain, description, intro, tocTitle, tocHint, sections);
    }

    private static List<Section> parseSections(List<Map<?, ?>> sectionMaps) throws LoadException {
        if (sectionMaps.isEmpty()) {
            throw new LoadException("Tidak ada bab di 'sections'. Tambahkan minimal satu bab.");
        }

        List<Section> sections = new ArrayList<>();
        for (int sectionIndex = 0; sectionIndex < sectionMaps.size(); sectionIndex++) {
            Map<?, ?> sectionMap = sectionMaps.get(sectionIndex);
            Component sectionTitle = parse(sectionMap.get("title"), "sections[" + sectionIndex + "].title");
            String sectionTitlePlain = TextParser.plain(sectionTitle).trim();
            if (sectionTitlePlain.isEmpty()) {
                throw new LoadException("sections[" + sectionIndex + "].title tidak boleh kosong.");
            }

            Object pagesObject = sectionMap.get("pages");
            if (!(pagesObject instanceof List<?> pageList) || pageList.isEmpty()) {
                throw new LoadException("Bab '" + sectionTitlePlain + "' tidak punya halaman.");
            }

            List<List<Component>> pages = new ArrayList<>();
            for (int pageIndex = 0; pageIndex < pageList.size(); pageIndex++) {
                String location = "Bab '" + sectionTitlePlain + "', halaman " + (pageIndex + 1);
                pages.add(parsePage(pageList.get(pageIndex), location));
            }
            sections.add(new Section(sectionTitle, sectionTitlePlain, pages));
        }
        return sections;
    }

    private static List<Component> parsePage(Object pageObject, String location) throws LoadException {
        if (!(pageObject instanceof Map<?, ?> pageMap)) {
            throw new LoadException(location + ": setiap halaman harus berupa map dengan key 'lines'.");
        }

        Object linesObject = pageMap.get("lines");
        if (!(linesObject instanceof List<?> lineList) || lineList.isEmpty()) {
            throw new LoadException(location + ": daftar 'lines' tidak boleh kosong.");
        }

        List<Component> lines = new ArrayList<>();
        boolean hasContent = false;
        for (int lineIndex = 0; lineIndex < lineList.size(); lineIndex++) {
            Object lineObject = lineList.get(lineIndex);
            if (!(lineObject instanceof String raw)) {
                throw new LoadException(location + ", baris " + (lineIndex + 1) + ": teks harus berupa string.");
            }
            if (raw.isBlank()) {
                lines.add(Component.empty());
                continue;
            }
            hasContent = true;
            try {
                lines.add(TextParser.parse(raw));
            } catch (TextParser.ParseException e) {
                throw new LoadException(location + ", baris " + (lineIndex + 1) + ": " + e.getMessage(), e);
            }
        }

        if (!hasContent) {
            throw new LoadException(location + ": halaman tidak boleh hanya berisi baris kosong.");
        }
        return List.copyOf(lines);
    }

    private static Component parse(Object value, String location) throws LoadException {
        if (!(value instanceof String raw) || raw.isBlank()) {
            throw new LoadException(location + " tidak boleh kosong.");
        }
        try {
            return TextParser.parse(raw);
        } catch (TextParser.ParseException e) {
            throw new LoadException(location + ": " + e.getMessage(), e);
        }
    }

    Component title() {
        return title;
    }

    String titlePlain() {
        return titlePlain;
    }

    Component author() {
        return author;
    }

    String authorPlain() {
        return authorPlain;
    }

    Component description() {
        return description;
    }

    Component intro() {
        return intro;
    }

    Component tocTitle() {
        return tocTitle;
    }

    Component tocHint() {
        return tocHint;
    }

    List<Section> sections() {
        return sections;
    }

    record Section(Component title, String titlePlain, List<List<Component>> pages) {
    }

    static final class LoadException extends Exception {
        LoadException(String message) {
            super(message);
        }

        LoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
