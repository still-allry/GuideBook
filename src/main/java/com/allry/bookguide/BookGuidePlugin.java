package com.allry.bookguide;

import java.io.File;
import java.util.logging.Level;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BookGuidePlugin extends JavaPlugin {
    private Book book;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Folder data plugin tidak bisa dibuat.");
        }

        try {
            saveResource("guide.yml", false);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, "Resource guide.yml tidak ditemukan di dalam jar.", e);
        }

        loadInitialGuide();

        GuideCommand command = new GuideCommand(this);
        PluginCommand guideCommand = getCommand("guide");
        if (guideCommand == null) {
            getLogger().severe("Command guide tidak terdaftar di plugin.yml.");
            return;
        }
        guideCommand.setExecutor(command);
        guideCommand.setTabCompleter(command);
    }

    Book book() {
        return book;
    }

    void reloadGuide(CommandSender sender) {
        try {
            GuideBookBuilder.BuildResult result = loadFromDisk();
            book = result.book();
            String message = "Guide berhasil dimuat: " + result.pageCount() + " halaman.";
            sender.sendMessage(Component.text(message, NamedTextColor.GREEN));
            getLogger().info(message + " Diminta oleh " + sender.getName() + ".");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Gagal memuat guide.yml.", e);
            sender.sendMessage(Component.text("Guide gagal dimuat: " + errorMessage(e), NamedTextColor.RED));
            if (book != null) {
                sender.sendMessage(Component.text("Konten lama tetap dipakai.", NamedTextColor.GRAY));
            }
        }
    }

    private void loadInitialGuide() {
        try {
            GuideBookBuilder.BuildResult result = loadFromDisk();
            book = result.book();
            getLogger().info("Book Guide siap: " + result.pageCount() + " halaman.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE,
                    "Gagal memuat guide.yml. Perbaiki file di plugins/BookGuide/guide.yml lalu jalankan /guide reload.", e);
        }
    }

    private GuideBookBuilder.BuildResult loadFromDisk() throws Exception {
        GuideContent content = GuideContent.load(new File(getDataFolder(), "guide.yml"));
        return new GuideBookBuilder(content).build();
    }

    private static String errorMessage(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
