package org.featurehouse.mcmod.symlinkcheck.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.impl.ImplLevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class CMixinMinecraft {
    @Accessor("gameDirectory")
    abstract File gameDirectory$symlinkCheck();

    @Invoker("getLevelSource")
    abstract LevelStorageSource getLevelSource$symlinkCheck();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void initLevelSource(GameConfig gameConfig, CallbackInfo ci) {
        ((SafeStorageSource) this.getLevelSource$symlinkCheck()).setSymlinkValidator(ImplLevelStorageSource.defaultValidator(this.gameDirectory$symlinkCheck().toPath()));
    }
}
