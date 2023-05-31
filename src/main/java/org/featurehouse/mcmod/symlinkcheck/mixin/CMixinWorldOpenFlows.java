package org.featurehouse.mcmod.symlinkcheck.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.impl.client.SymlinkWarningScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(WorldOpenFlows.class)
abstract class CMixinWorldOpenFlows {
    @Accessor("minecraft")
    abstract Minecraft minecraft$symlinkCheck();

    @Redirect(method = "createWorldAccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource;createAccess(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;"))
    private LevelStorageSource.LevelStorageAccess onWorldAccessCreation(LevelStorageSource instance, String s) throws IOException {
        return ((SafeStorageSource) instance).validateAndCreateAccessCatch(s, e -> {
            LogUtils.getLogger().warn("{}", e.getMessage());
            this.minecraft$symlinkCheck().setScreen(new SymlinkWarningScreen(null));
        });
    }
}
