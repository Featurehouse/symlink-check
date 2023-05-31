package org.featurehouse.mcmod.symlinkcheck;

import net.minecraft.world.level.storage.LevelStorageSource;
import org.featurehouse.mcmod.symlinkcheck.impl.ContentValidationException;
import org.featurehouse.mcmod.symlinkcheck.impl.DirectoryValidator;
import org.featurehouse.mcmod.symlinkcheck.impl.ForbiddenSymlinkInfo;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public interface SafeStorageSource {
    DirectoryValidator symlinkValidator();
    void setSymlinkValidator(DirectoryValidator validator);

    default LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String var0) throws IOException, ContentValidationException {
        Path var1 = getLevelPath(var0);
        List<ForbiddenSymlinkInfo> var2 = symlinkValidator().validateSave(var1, true);
        if (!var2.isEmpty()) {
            throw new ContentValidationException(var1, var2);
        }
        return cast().new LevelStorageAccess(var0);
    }

    default LevelStorageSource.LevelStorageAccess validateAndCreateAccessCatch(String s, Consumer<? super ContentValidationException> c) throws IOException {
        try {
            return validateAndCreateAccess(s);
        } catch (ContentValidationException e) {
            c.accept(e);
            return null;
        }
    }

    private LevelStorageSource cast() {
        return (LevelStorageSource) this;
    }

    private Path getLevelPath(String s) {
        return cast().getBaseDir().resolve(s);
    }
}
