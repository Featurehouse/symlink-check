package org.featurehouse.mcmod.symlinkcheck.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException
extends Exception {
    private final Path directory;
    private final List<ForbiddenSymlinkInfo> entries;

    public ContentValidationException(Path var0, List<ForbiddenSymlinkInfo> var1) {
        this.directory = var0;
        this.entries = var1;
    }

    @Override
    public String getMessage() {
        return ContentValidationException.getMessage(this.directory, this.entries);
    }

    public static String getMessage(Path var02, List<ForbiddenSymlinkInfo> var1) {
        return "Failed to validate '" + var02 + "'. Found forbidden symlinks: " + var1.stream().map(var0 -> var0.link() + "->" + var0.target()).collect(Collectors.joining(", "));
    }
}

