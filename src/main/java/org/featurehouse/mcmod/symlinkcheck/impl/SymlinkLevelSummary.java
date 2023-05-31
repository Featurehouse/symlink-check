package org.featurehouse.mcmod.symlinkcheck.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;

import java.nio.file.Path;

public class SymlinkLevelSummary extends LevelSummary {
    public SymlinkLevelSummary(String var0, Path var1) {
        super(null, null, var0, false, false, false, var1);
    }

    @Override
    public String getLevelName() {
        return this.getLevelId();
    }

    @Override
    public Component getInfo() {
        return Component.translatableWithFallback("symlink_warning.title", "World folder contains symbolic links").withStyle(var0 -> var0.withColor(-65536));
    }

    @Override
    public long getLastPlayed() {
        return -1L;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}