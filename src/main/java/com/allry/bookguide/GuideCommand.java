package com.allry.bookguide;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GuideCommand implements CommandExecutor, TabCompleter {
    private final BookGuidePlugin plugin;

    GuideCommand(BookGuidePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bookguide.admin")) {
                sender.sendMessage(Component.text("Kamu tidak punya izin bookguide.admin.", NamedTextColor.RED));
                return true;
            }
            plugin.reloadGuide(sender);
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage(Component.text("Gunakan /guide atau /guide reload.", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Buku hanya bisa dibuka oleh pemain.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("bookguide.use")) {
            player.sendMessage(Component.text("Kamu tidak punya izin bookguide.use.", NamedTextColor.RED));
            return true;
        }

        Book book = plugin.book();
        if (book == null) {
            player.sendMessage(Component.text("Guide belum tersedia. Hubungi admin server.", NamedTextColor.RED));
            return true;
        }

        player.openBook(book);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1 && sender.hasPermission("bookguide.admin")) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if ("reload".startsWith(prefix)) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}
