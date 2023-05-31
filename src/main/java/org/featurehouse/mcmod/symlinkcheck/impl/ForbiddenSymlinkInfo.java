package org.featurehouse.mcmod.symlinkcheck.impl;

import java.nio.file.Path;

public record ForbiddenSymlinkInfo(Path link, Path target) {
}