package org.featurehouse.mcmod.symlinkcheck.impl;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList
implements PathMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMENT_PREFIX = "#";
    private final List<ConfigEntry> entries;
    private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap<>();
    public static final PathAllowList NONE = new PathAllowList(Collections.emptyList());

    public PathAllowList(List<ConfigEntry> var0) {
        this.entries = var0;
    }

    public PathMatcher getForFileSystem(FileSystem var0) {
        return this.compiledPaths.computeIfAbsent(var0.provider().getScheme(), var12 -> {
            List<PathMatcher> var4;
            try {
                var4 = this.entries.stream().map(var1 -> var1.compile(var0)).toList();
            } catch (Exception var3) {
                LOGGER.error("Failed to compile file pattern list", var3);
                return var00 -> false;
            }
            return switch (var4.size()) {
                case 0 -> var00 -> false;
                case 1 -> (PathMatcher)var4.get(0);
                default -> path -> {
                    for (PathMatcher matcher : var4) {
                        if (matcher.matches(path)) return true;
                    }
                    return false;
                };
            };
        });
    }

    @Override
    public boolean matches(Path var0) {
        return this.getForFileSystem(var0.getFileSystem()).matches(var0);
    }

    public static PathAllowList readPlain(BufferedReader var02) {
        return new PathAllowList(var02.lines().flatMap(var0 -> ConfigEntry.parse(var0).stream()).toList());
    }

    public record ConfigEntry(EntryType type, String pattern) {
        public PathMatcher compile(FileSystem var0) {
            return this.type().compile(var0, this.pattern);
        }

        static Optional<ConfigEntry> parse(String var0) {
            if (var0.isBlank() || var0.startsWith(PathAllowList.COMMENT_PREFIX)) {
                return Optional.empty();
            }
            if (!var0.startsWith("[")) {
                return Optional.of(new ConfigEntry(EntryType.PREFIX, var0));
            }
            int var1 = var0.indexOf(93, 1);
            if (var1 == -1) {
                throw new IllegalArgumentException("Unterminated type in line '" + var0 + "'");
            }
            String var2 = var0.substring(1, var1);
            String var3 = var0.substring(var1 + 1);
            return switch (var2) {
                case "glob", "regex" -> Optional.of(new ConfigEntry(EntryType.FILESYSTEM, var2 + ":" + var3));
                case "prefix" -> Optional.of(new ConfigEntry(EntryType.PREFIX, var3));
                default -> throw new IllegalArgumentException("Unsupported definition type in line '" + var0 + "'");
            };
        }

        static ConfigEntry glob(String var0) {
            return new ConfigEntry(EntryType.FILESYSTEM, "glob:" + var0);
        }

        static ConfigEntry regex(String var0) {
            return new ConfigEntry(EntryType.FILESYSTEM, "regex:" + var0);
        }

        static ConfigEntry prefix(String var0) {
            return new ConfigEntry(EntryType.PREFIX, var0);
        }
    }

    @FunctionalInterface
    public interface EntryType {
        EntryType FILESYSTEM = FileSystem::getPathMatcher;
        EntryType PREFIX = (fs, str) -> path -> path.toString().startsWith(str);

        PathMatcher compile(FileSystem var1, String var2);
    }
}

