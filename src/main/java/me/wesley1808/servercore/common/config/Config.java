package me.wesley1808.servercore.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.GenericBuilder;
import me.wesley1808.servercore.common.ServerCore;
import me.wesley1808.servercore.common.config.tables.*;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public final class Config {
    public static final Table[] TABLES = {
            new Table(FeatureConfig.class, "features", "Lets you enable / disable certain features and modify them."),
            new Table(DynamicConfig.class, "dynamic", "Modifies mobcaps, no-chunk-tick, simulation and view-distance depending on the MSPT."),
            new Table(EntityLimitConfig.class, "entity_limits", "Stops animals / villagers from breeding if there are too many of the same type nearby."),
            new Table(OptimizationConfig.class, "optimizations", "Allows you to toggle specific optimizations that don't have full vanilla parity.\n These settings will only take effect after server restarts."),
            new Table(CommandConfig.class, "commands", "Allows you to disable specific commands and modify the way some of them are formatted."),
            new Table(ActivationRangeConfig.class, "activation_range", "Stops entities from ticking if they are too far away."),
            new Table(ItemValidationConfig.class, "item_validation", "Validates the size of items like books and disallows them from being sent to the client if they are too large."),
    };

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("servercore.toml");
    private static final GenericBuilder<CommentedConfig, CommentedFileConfig> BUILDER = CommentedFileConfig.builder(CONFIG_PATH).preserveInsertionOrder().sync();

    static {
        // Required to generate the config with the correct order.
        System.setProperty("nightconfig.preserveInsertionOrder", "true");
    }

    public static void load() {
        CommentedFileConfig config = BUILDER.build();
        config.load();

        for (Table table : TABLES) {
            Config.validate(table, config);
            Config.loadEntries(config.get(table.key), table.clazz);
        }

        Config.save();
    }

    public static void save() {
        CommentedFileConfig config = BUILDER.build();

        for (Table table : TABLES) {
            Config.validate(table, config);
            Config.saveEntries(config.get(table.key), table.clazz);
            config.setComment(table.key, " " + table.comment);
        }

        config.save();
        config.close();
    }

    // Creates table when missing.
    private static void validate(Table table, CommentedFileConfig config) {
        if (!config.contains(table.key)) {
            config.add(table.key, CommentedConfig.inMemory());
        }
    }

    private static void loadEntries(CommentedConfig config, Class<?> clazz) {
        try {
            Config.forEachEntry(clazz, (field, entry) -> {
                final String key = field.getName().toLowerCase();
                final Object value = config.getOrElse(key, entry.getDefault());
                if (!entry.set(value)) {
                    ServerCore.getLogger().error("[ServerCore] Invalid config entry found! {} = {}", key, value);
                }
            });
        } catch (Exception ex) {
            ServerCore.getLogger().error("[ServerCore] Exception was thrown whilst loading configs!", ex);
        }
    }

    private static void saveEntries(CommentedConfig config, Class<?> clazz) {
        try {
            config.clear();
            Config.forEachEntry(clazz, (field, entry) -> {
                final String key = field.getName().toLowerCase();
                final String comment = entry.getComment();
                config.set(key, entry.get());
                if (comment != null) {
                    config.setComment(key, " " + comment.replace("\n", "\n "));
                }
            });
        } catch (Exception ex) {
            ServerCore.getLogger().error("[ServerCore] Exception was thrown whilst saving configs!", ex);
        }
    }

    @SuppressWarnings("all")
    public static <T> void forEachEntry(Class<?> clazz, BiConsumer<Field, ConfigEntry<T>> consumer) throws IllegalAccessException {
        for (Field field : clazz.getFields()) {
            if (field.get(clazz) instanceof ConfigEntry entry) {
                consumer.accept(field, entry);
            }
        }
    }

    public record Table(Class<?> clazz, String key, String comment) {
    }
}