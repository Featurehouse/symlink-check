package org.featurehouse.mcmod.symlinkcheck.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.SymlinkCheckMod;
import org.featurehouse.mcmod.symlinkcheck.impl.DirectoryValidator;
import org.featurehouse.mcmod.symlinkcheck.impl.ImplLevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public class MixinLevelStorageSource implements SafeStorageSource {
    private DirectoryValidator symlinkValidator;

    @Inject(method = "method_29015", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/LevelStorageSource;readLightweightData(Ljava/nio/file/Path;)Lnet/minecraft/nbt/Tag;"
    ), cancellable = true)
    private void preSummaryReader(LevelStorageSource.LevelDirectory levelDirectory, boolean bl, Path path, DataFixer dataFixer,
                                  CallbackInfoReturnable<LevelSummary> cir) throws Exception {
        LevelSummary summary = ImplLevelStorageSource.preSummaryReader(levelDirectory, path, symlinkValidator());
        if (summary != null) cir.setReturnValue(summary);
    }

    @Override
    public DirectoryValidator symlinkValidator() {
        return symlinkValidator;
    }

    @Override
    public void setSymlinkValidator(DirectoryValidator validator) {
        this.symlinkValidator = validator;
    }

    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void modifyCreateDefault(Path var0, CallbackInfoReturnable<LevelStorageSource> cir) {
        DirectoryValidator var1 = DirectoryValidator.parseValidator(var0.resolve(SymlinkCheckMod.ALLOWED_SYMLINKS_CONFIG_NAME));
        ((SafeStorageSource) cir.getReturnValue()).setSymlinkValidator(var1);
    }
}
