package org.featurehouse.mcmod.symlinkcheck.impl;

import com.mojang.logging.LogUtils;
import org.featurehouse.mcmod.symlinkcheck.SymlinkCheckMod;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
    private final PathAllowList symlinkTargetAllowList;
    private static final Logger LOGGER = LogUtils.getLogger();

    public DirectoryValidator(PathAllowList allowList) {
        this.symlinkTargetAllowList = allowList;
    }

    public static DirectoryValidator parseValidator(Path path) {
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return new DirectoryValidator(PathAllowList.readPlain(reader));
            } catch (Exception e) {
                LOGGER.error("Failed to parse {}, disallowing all symbolic links", SymlinkCheckMod.ALLOWED_SYMLINKS_CONFIG_NAME, e);
            }
        }
        return new DirectoryValidator(PathAllowList.NONE);
    }

    public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> symlinkInfos) throws IOException {
        Path var2 = Files.readSymbolicLink(path);
        if (!this.symlinkTargetAllowList.matches(var2)) {
            symlinkInfos.add(new ForbiddenSymlinkInfo(path, var2));
        }
    }

    public List<ForbiddenSymlinkInfo> validateSave(Path path, boolean allowSymlink) throws IOException {
        BasicFileAttributes attr;
        final ArrayList<ForbiddenSymlinkInfo> symlinkInfos = new ArrayList<>();
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException var4) {
            return symlinkInfos;
        }
        if (attr.isRegularFile() || attr.isOther()) {
            throw new IOException("Path " + path + " is not a directory");
        }
        if (attr.isSymbolicLink()) {
            if (allowSymlink) {
                path = Files.readSymbolicLink(path);
            } else {
                this.validateSymlink(path, symlinkInfos);
                return symlinkInfos;
            }
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            private void validateSymlink(Path var0, BasicFileAttributes var1) throws IOException {
                if (var1.isSymbolicLink()) {
                    DirectoryValidator.this.validateSymlink(var0, symlinkInfos);
                }
            }

            @Override
            public FileVisitResult preVisitDirectory(Path var0, BasicFileAttributes var1) throws IOException {
                this.validateSymlink(var0, var1);
                return super.preVisitDirectory(var0, var1);
            }

            @Override
            public FileVisitResult visitFile(Path var0, BasicFileAttributes var1) throws IOException {
                this.validateSymlink(var0, var1);
                return super.visitFile(var0, var1);
            }
        });
        return symlinkInfos;
    }
}

