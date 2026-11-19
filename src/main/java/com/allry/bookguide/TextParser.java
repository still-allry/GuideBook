package com.allry.bookguide;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

final class TextParser {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern CODE = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "<black>"),
            Map.entry('1', "<dark_blue>"),
            Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"),
            Map.entry('4', "<dark_red>"),
            Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"),
            Map.entry('7', "<gray>"),
            Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"),
            Map.entry('a', "<green>"),
            Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"),
            Map.entry('d', "<light_purple>"),
            Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    private TextParser() {
    }

    static Component parse(String raw) throws ParseException {
        try {
            return MINI_MESSAGE.deserialize(toMiniMessage(raw));
        } catch (RuntimeException e) {
            throw new ParseException("format teks tidak valid: " + rootMessage(e), e);
        }
    }

    static String plain(Component component) {
        return PLAIN.serialize(component);
    }

    private static String toMiniMessage(String raw) {
        String input = raw.replace('\u00a7', '&');
        input = replaceHex(input);
        Matcher matcher = CODE.matcher(input);
        StringBuilder out = new StringBuilder(input.length() + 16);
        while (matcher.find()) {
            String tag = LEGACY_TAGS.get(Character.toLowerCase(matcher.group(1).charAt(0)));
            matcher.appendReplacement(out, Matcher.quoteReplacement(tag == null ? matcher.group() : tag));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static String replaceHex(String input) {
        Matcher matcher = HEX.matcher(input);
        StringBuilder out = new StringBuilder(input.length() + 16);
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement("<color:#" + matcher.group(1) + ">"));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    static final class ParseException extends Exception {
        ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
