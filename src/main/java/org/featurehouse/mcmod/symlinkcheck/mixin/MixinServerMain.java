package org.featurehouse.mcmod.symlinkcheck.mixin;

import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.impl.ContentValidationException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(Main.class)
public class MixinServerMain {
    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource;createAccess(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;"
    ))
    private static LevelStorageSource.LevelStorageAccess redirectStorageAccess(LevelStorageSource instance, String s) throws ContentValidationException, IOException {
        return ((SafeStorageSource) instance).validateAndCreateAccess(s);
    }
}
