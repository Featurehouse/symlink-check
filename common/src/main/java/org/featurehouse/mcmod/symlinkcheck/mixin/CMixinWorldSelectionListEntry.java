package org.featurehouse.mcmod.symlinkcheck.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.SymlinkCheckMod;
import org.featurehouse.mcmod.symlinkcheck.impl.ContentValidationException;
import org.featurehouse.mcmod.symlinkcheck.impl.ForbiddenSymlinkInfo;
import org.featurehouse.mcmod.symlinkcheck.impl.SymlinkLevelSummary;
import org.featurehouse.mcmod.symlinkcheck.impl.client.SymlinkWarningScreen;
import org.featurehouse.mcmod.symlinkcheck.marks.Mark1;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static net.minecraft.client.gui.GuiComponent.blit;

@Mixin(WorldSelectionList.WorldListEntry.class)
abstract class CMixinWorldSelectionListEntry extends WorldSelectionList.Entry implements Mark1 {
    @Accessor("iconFile") abstract Path iconFile$SymlinkCheck();
    @Accessor("iconFile") abstract void setIconFile$symlinkCheck(Path path);
    @Accessor("screen") abstract SelectWorldScreen screen$symlinkCheck();

    @Accessor("minecraft") abstract Minecraft minecraft$symlinkCheck();

    @Accessor("summary")
    abstract LevelSummary summary$symlinkCheck();

    @Inject(method = "render", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;isLocked()Z"))
    private void beforeIsLocked(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f, CallbackInfo ci) {
        int g = m - k < 32 ? 32 : 0;
        if (this.summary$symlinkCheck() instanceof SymlinkLevelSummary) {
            //RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, SymlinkCheckMod.ICON_OVERLAY_LOCATION);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            blit(poseStack, k, j, 96.0f, g, 32, 32, 256, 256);
            blit(poseStack, k, j, 32.0f, g, 32, 32, 256, 256);
            ci.cancel();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/text/DateFormat;format(Ljava/util/Date;)Ljava/lang/String;"))
    private String replaceDateFormat(DateFormat instance, Date date) {
        if (date.getTime() != -1L) return instance.format(date);
        return I18n.exists("symlink_check.date_format.null") ? I18n.get("symlink_check.date_format.null") : "unknown";
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;isRegularFile(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"))
    private boolean replaceFileIsRegular(Path provider, LinkOption[] ioe) {
        // to skip further assignment
        return true;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList$WorldListEntry;loadServerIcon()Lnet/minecraft/client/renderer/texture/DynamicTexture;"))
    private void validateBeforeLoad(WorldSelectionList worldSelectionList, WorldSelectionList worldSelectionList2, LevelSummary levelSummary, CallbackInfo ci) {
        validateIconFile$symlinkCheck();
    }

    @Inject(method = "joinWorld", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;backupStatus()Lnet/minecraft/world/level/storage/LevelSummary$BackupStatus;"))
    private void onJoinWorld(CallbackInfo ci) {
        if (this.summary$symlinkCheck() instanceof SymlinkLevelSummary) {
            this.minecraft$symlinkCheck().setScreen(new SymlinkWarningScreen(this.screen$symlinkCheck()));
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = {"editWorld", "recreateWorld"}, cancellable = true)
    private void editOrRecreate(CallbackInfo ci) {
        if (this.summary$symlinkCheck() instanceof SymlinkLevelSummary) {
            this.minecraft$symlinkCheck().setScreen(new SymlinkWarningScreen(this.screen$symlinkCheck()));
            ci.cancel();
        }
    }

    private void validateIconFile$symlinkCheck() {
        if (this.iconFile$SymlinkCheck() == null) {
            return;
        }
        try {
            BasicFileAttributes attr = Files.readAttributes(this.iconFile$SymlinkCheck(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attr.isSymbolicLink()) {
                ArrayList<ForbiddenSymlinkInfo> var1 = new ArrayList<>();
                ((SafeStorageSource) this.minecraft$symlinkCheck().getLevelSource()).symlinkValidator().validateSymlink(this.iconFile$SymlinkCheck(), var1);
                if (!var1.isEmpty()) {
                    LogUtils.getLogger().warn(ContentValidationException.getMessage(this.iconFile$SymlinkCheck(), var1));
                    this.setIconFile$symlinkCheck(null);
                } else {
                    attr = Files.readAttributes(this.iconFile$SymlinkCheck(), BasicFileAttributes.class);
                }
            }
            if (!attr.isRegularFile()) {
                this.setIconFile$symlinkCheck(null);
            }
        } catch (NoSuchFileException var2) {
            this.setIconFile$symlinkCheck(null);
        } catch (IOException var3) {
            LogUtils.getLogger().error("could not validate symlink", var3);
            this.setIconFile$symlinkCheck(null);
        }
    }

    @Override
    public void action0$symlinkCheck() {
        this.minecraft$symlinkCheck().setScreen(new SymlinkWarningScreen(this.screen$symlinkCheck()));
    }

    @Override
    public void action1$symlinkCheck() {
        this.minecraft$symlinkCheck().setScreen(new AlertScreen(() -> this.minecraft$symlinkCheck().setScreen(this.screen$symlinkCheck()), Component.translatable("selectWorld.recreate.error.title"), Component.translatable("selectWorld.recreate.error.text")));
    }
}
