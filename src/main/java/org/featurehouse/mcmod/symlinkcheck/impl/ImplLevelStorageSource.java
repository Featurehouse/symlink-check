package org.featurehouse.mcmod.symlinkcheck.impl;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.featurehouse.mcmod.symlinkcheck.marks.Mark1;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ImplLevelStorageSource {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static DirectoryValidator defaultValidator(Path gameDir) {
        return DirectoryValidator.parseValidator(gameDir.resolve("allowed_symlinks.txt"));
    }

    public static @Nullable LevelSummary preSummaryReader(LevelStorageSource.LevelDirectory dir, Path path, DirectoryValidator validator) throws Exception {
        if (Files.isSymbolicLink(path)) {
            ArrayList<ForbiddenSymlinkInfo> var4 = new ArrayList<>();
            validator.validateSymlink(path, var4);
            if (!var4.isEmpty()) {
                LOGGER.warn(ContentValidationException.getMessage(path, var4));
                return new SymlinkLevelSummary(dir.directoryName(), dir.iconFile());
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static Consumer<ContentValidationException> consumerEditWorldScreen(String var1) {
        return ex -> {
            LOGGER.warn("{}", ex.getMessage());
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), var1);
        };
    }

    @SuppressWarnings("unused")
    public static Consumer<ContentValidationException> consumer1(Object o) {
        return ex -> {
            LOGGER.warn("{}", ex.getMessage());
            ((Mark1) o).action0$symlinkCheck();
        };
    }

    @SuppressWarnings("unused")
    public static Consumer<ContentValidationException> consumer2(Object o) {
        return ex -> {
            LOGGER.warn("{}", ex.getMessage());
            ((Mark1) o).action1$symlinkCheck();
        };
    }
}
