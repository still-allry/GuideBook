package com.allry.bookguide;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

final class GuideBookBuilder {
    private static final int TEXT_WIDTH = 114;
    private static final int CONTENT_LINES = 11;
    private static final int TOTAL_LINES = 14;
    private static final int MAX_PAGES = 100;
    private static final String SEPARATOR_TEXT = "------------------";

    private final GuideContent content;

    GuideBookBuilder(GuideContent content) {
        this.content = content;
    }

    BuildResult build() throws GuideContent.LoadException {
        List<ContentPage> contentPages = new ArrayList<>();
        List<SectionLayout> sectionLayouts = new ArrayList<>();

        for (GuideContent.Section section : content.sections()) {
            SectionLayout layout = new SectionLayout(section, contentPages.size());
            sectionLayouts.add(layout);

            for (List<Component> sourcePage : section.pages()) {
                List<Component> lines = new ArrayList<>();
                for (Component sourceLine : sourcePage) {
                    lines.addAll(wrap(sourceLine));
                }
                addBalancedPages(contentPages, layout, lines);
            }
        }

        int tocPageCount = 1;
        int contentStartPage;
        List<List<Component>> tocPages;
        for (int attempt = 0; ; attempt++) {
            contentStartPage = 2 + tocPageCount;
            tocPages = paginate(buildTocLines(sectionLayouts, contentStartPage));
            if (tocPages.size() == tocPageCount) {
                break;
            }
            tocPageCount = tocPages.size();
            if (attempt >= 3) {
                throw new GuideContent.LoadException("Daftar isi tidak bisa dihitung dengan stabil. Periksa judul bab.");
            }
        }

        int totalPages = 1 + tocPages.size() + contentPages.size();
        if (totalPages > MAX_PAGES) {
            throw new GuideContent.LoadException(
                    "Buku menghasilkan " + totalPages + " halaman, melebihi batas " + MAX_PAGES
                            + ". Kurangi halaman di guide.yml."
            );
        }

        List<Component> pages = new ArrayList<>(totalPages);
        pages.add(buildCover());

        for (int index = 0; index < tocPages.size(); index++) {
            pages.add(buildTocPage(tocPages.get(index), index, tocPages.size(), totalPages));
        }

        for (int index = 0; index < contentPages.size(); index++) {
            int pageNumber = 2 + tocPages.size() + index;
            pages.add(buildContentPage(contentPages.get(index), pageNumber, totalPages));
        }

        Book book = Book.builder()
                .title(content.title())
                .author(content.author())
                .pages(pages)
                .build();
        return new BuildResult(book, pages.size());
    }

    private Component buildCover() throws GuideContent.LoadException {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());

        Component title = Component.text("", NamedTextColor.GOLD, TextDecoration.BOLD).append(content.title());
        if (pixelWidth(title) > TEXT_WIDTH) {
            throw new GuideContent.LoadException("book.title terlalu panjang untuk sampul buku.");
        }
        lines.add(center(title));
        lines.add(center(separator()));

        lines.addAll(wrap(Component.text("", NamedTextColor.YELLOW).append(content.description())));
        lines.add(center(Component.text("oleh ", NamedTextColor.DARK_GRAY).append(content.author())));
        lines.add(Component.empty());
        lines.addAll(wrap(Component.text("", NamedTextColor.GRAY).append(content.intro())));
        lines.add(Component.empty());

        Component button = Component.text("Buka Daftar Isi", NamedTextColor.GOLD, TextDecoration.BOLD)
                .clickEvent(ClickEvent.changePage(2))
                .hoverEvent(HoverEvent.showText(Component.text("Buka Daftar Isi", NamedTextColor.GRAY)));
        if (pixelWidth(button) > TEXT_WIDTH) {
            throw new GuideContent.LoadException("Tombol sampul terlalu panjang.");
        }
        lines.add(center(button));

        if (lines.size() > TOTAL_LINES) {
            throw new GuideContent.LoadException("Sampul terlalu panjang. Persingkat book.description atau book.intro.");
        }
        return join(lines);
    }

    private List<Component> buildTocLines(List<SectionLayout> sectionLayouts, int contentStartPage) {
        List<Component> lines = new ArrayList<>();
        lines.addAll(wrap(Component.text("", NamedTextColor.GRAY).append(content.tocHint())));

        for (int index = 0; index < sectionLayouts.size(); index++) {
            SectionLayout layout = sectionLayouts.get(index);
            int targetPage = contentStartPage + layout.firstPageIndex;

            Component entry = Component.text("", NamedTextColor.GOLD)
                    .append(layout.section.title())
                    .append(Component.text("  ", NamedTextColor.GOLD))
                    .append(Component.text(targetPage, NamedTextColor.YELLOW))
                    .clickEvent(ClickEvent.changePage(targetPage))
                    .hoverEvent(HoverEvent.showText(Component.text("Buka bab ini", NamedTextColor.GRAY)));
            lines.addAll(wrap(entry));
        }
        return lines;
    }

    private Component buildTocPage(List<Component> contentLines, int index, int count, int totalPages) throws GuideContent.LoadException {
        int pageNumber = 2 + index;
        Component header = Component.text("", NamedTextColor.GOLD, TextDecoration.BOLD).append(content.tocTitle());
        if (count > 1) {
            header = header.append(Component.text(" " + (index + 1) + "/" + count, NamedTextColor.GRAY));
        }
        return page(header, contentLines, footerInfo(pageNumber, totalPages), navLine(pageNumber, totalPages, "Sampul", 1));
    }

    private Component buildContentPage(ContentPage contentPage, int pageNumber, int totalPages) throws GuideContent.LoadException {
        Component header = Component.text("", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(contentPage.section.section.title());
        return page(header, contentPage.lines, footerInfo(pageNumber, totalPages), navLine(pageNumber, totalPages, "Daftar Isi", 2));
    }

    private Component page(Component header, List<Component> contentLines, Component footer, Component nav) throws GuideContent.LoadException {
        if (pixelWidth(header) > TEXT_WIDTH) {
            String title = TextParser.plain(header);
            throw new GuideContent.LoadException("Judul bab terlalu panjang untuk header buku: " + title);
        }
        if (contentLines.size() > CONTENT_LINES) {
            throw new IllegalStateException("Terlalu banyak baris untuk satu halaman.");
        }

        List<Component> lines = new ArrayList<>(TOTAL_LINES);
        lines.add(center(header));
        for (int index = 0; index < CONTENT_LINES; index++) {
            lines.add(index < contentLines.size() ? contentLines.get(index) : Component.empty());
        }
        lines.add(center(footer));
        lines.add(center(nav));
        return join(lines);
    }

    private static Component footerInfo(int pageNumber, int totalPages) {
        return Component.text("Halaman " + pageNumber + "/" + totalPages, NamedTextColor.GRAY);
    }

    private static Component navLine(int pageNumber, int totalPages, String backLabel, int backTarget) {
        Component previous = pageNumber > 1
                ? navButton("[<]", pageNumber - 1, "Halaman sebelumnya")
                : Component.text("[<]", NamedTextColor.DARK_GRAY);
        Component back = navButton(backLabel, backTarget, "Buka " + backLabel);
        Component next = pageNumber < totalPages
                ? navButton("[>]", pageNumber + 1, "Halaman berikutnya")
                : Component.text("[>]", NamedTextColor.DARK_GRAY);

        return Component.empty()
                .append(previous)
                .append(Component.text(" "))
                .append(back)
                .append(Component.text(" "))
                .append(next);
    }

    private static Component navButton(String label, int targetPage, String hover) {
        return Component.text(label, NamedTextColor.GOLD)
                .clickEvent(ClickEvent.changePage(targetPage))
                .hoverEvent(HoverEvent.showText(Component.text(hover, NamedTextColor.GRAY)));
    }

    private static Component separator() {
        return Component.text(SEPARATOR_TEXT, NamedTextColor.DARK_GRAY);
    }

    private static void addBalancedPages(List<ContentPage> pages, SectionLayout layout, List<Component> lines) {
        if (lines.isEmpty()) {
            pages.add(new ContentPage(layout, List.of(Component.empty())));
            return;
        }

        int pageCount = (lines.size() + CONTENT_LINES - 1) / CONTENT_LINES;
        int baseSize = lines.size() / pageCount;
        int extra = lines.size() % pageCount;
        int offset = 0;
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int size = baseSize + (pageIndex < extra ? 1 : 0);
            int end = offset + size;
            pages.add(new ContentPage(layout, List.copyOf(lines.subList(offset, end))));
            offset = end;
        }
    }

    private static List<List<Component>> paginate(List<Component> lines) {
        List<List<Component>> pages = new ArrayList<>();
        for (int start = 0; start < lines.size(); start += CONTENT_LINES) {
            int end = Math.min(start + CONTENT_LINES, lines.size());
            pages.add(List.copyOf(lines.subList(start, end)));
        }
        if (pages.isEmpty()) {
            pages.add(List.of(Component.empty()));
        }
        return pages;
    }

    private static List<Component> wrap(Component source) {
        List<StyledChar> chars = new ArrayList<>();
        flatten(source, Style.empty(), chars);

        while (!chars.isEmpty() && chars.get(chars.size() - 1).value == ' ') {
            chars.remove(chars.size() - 1);
        }
        if (chars.isEmpty()) {
            return List.of(Component.empty());
        }

        List<Component> lines = new ArrayList<>();
        int start = 0;
        while (start < chars.size()) {
            int width = 0;
            int end = start;
            int lastSpace = -1;
            while (end < chars.size()) {
                StyledChar styledChar = chars.get(end);
                int charWidth = charWidth(styledChar);
                if (width + charWidth > TEXT_WIDTH) {
                    break;
                }
                if (styledChar.value == ' ') {
                    lastSpace = end;
                }
                width += charWidth;
                end++;
            }

            if (end == chars.size()) {
                lines.add(buildLine(chars, start, end));
                break;
            }

            if (end == start) {
                end = start + 1;
            }
            int breakAt = lastSpace > start ? lastSpace : end;
            lines.add(buildLine(chars, start, breakAt));
            start = breakAt;
            while (start < chars.size() && chars.get(start).value == ' ') {
                start++;
            }
        }
        return lines;
    }

    private static void flatten(Component component, Style inherited, List<StyledChar> out) {
        Style style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
        if (component instanceof TextComponent textComponent) {
            String text = textComponent.content();
            for (int index = 0; index < text.length(); index++) {
                out.add(new StyledChar(text.charAt(index), style));
            }
        }
        for (Component child : component.children()) {
            flatten(child, style, out);
        }
    }

    private static Component buildLine(List<StyledChar> chars, int start, int end) {
        while (end > start && chars.get(end - 1).value == ' ') {
            end--;
        }
        if (start >= end) {
            return Component.empty();
        }

        Component result = Component.empty();
        int index = start;
        while (index < end) {
            Style style = chars.get(index).style;
            StringBuilder text = new StringBuilder();
            while (index < end && chars.get(index).style.equals(style)) {
                text.append(chars.get(index).value);
                index++;
            }
            result = result.append(Component.text(text.toString(), style));
        }
        return result;
    }

    private static int charWidth(StyledChar styledChar) {
        int width;
        if (styledChar.value == ' ') {
            width = 4;
        } else if (styledChar.value < 128) {
            width = 6;
        } else {
            width = 9;
        }
        if (styledChar.style.hasDecoration(TextDecoration.BOLD)) {
            width++;
        }
        return width;
    }

    private static int pixelWidth(Component component) {
        List<StyledChar> chars = new ArrayList<>();
        flatten(component, Style.empty(), chars);
        int width = 0;
        for (StyledChar styledChar : chars) {
            width += charWidth(styledChar);
        }
        return width;
    }

    private static Component center(Component component) {
        int padding = Math.max(0, (TEXT_WIDTH - pixelWidth(component)) / 2);
        int spaces = padding / 4;
        if (spaces == 0) {
            return component;
        }
        return Component.text(" ".repeat(spaces)).append(component);
    }

    private static Component join(List<Component> lines) {
        Component result = Component.empty();
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                result = result.append(Component.newline());
            }
            result = result.append(lines.get(index));
        }
        return result;
    }

    record BuildResult(Book book, int pageCount) {
    }

    private record ContentPage(SectionLayout section, List<Component> lines) {
    }

    private record SectionLayout(GuideContent.Section section, int firstPageIndex) {
    }

    private record StyledChar(char value, Style style) {
    }
}
