package org.featurehouse.mcmod.symlinkcheck.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.featurehouse.mcmod.symlinkcheck.ParametersAreNonnullByDefault;
import org.featurehouse.mcmod.symlinkcheck.SymlinkCheckMod;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SymlinkWarningScreen extends Screen {
    private static final Component TITLE = Component.translatableWithFallback("symlink_warning.title", "World folder contains symbolic links").withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE_TEXT = Component.translatableWithFallback("symlink_warning.message", "Loading worlds with symlink may be unsafe to your device. " +
                    "See " + SymlinkCheckMod.SYMLINK_MS_LINK + " for more information",
            SymlinkCheckMod.SYMLINK_MS_LINK);
    private static final Component GUI_OPEN_IN_BROWSER = Component.translatable("chat.link.open");
    private static final Component GUI_COPY_LINK_TO_CLIPBOARD = Component.translatable("symlink_check.gui.copy_link_to_clipboard");
    @Nullable
    private final Screen callbackScreen;
    private final GridLayout layout = new GridLayout().rowSpacing(10);

    public SymlinkWarningScreen(@Nullable Screen parent) {
        super(TITLE);
        this.callbackScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = this.layout.createRowHelper(1);
        rowHelper.addChild(new StringWidget(this.title, this.font));
        rowHelper.addChild(new MultiLineTextWidget(MESSAGE_TEXT, this.font).setMaxWidth(this.width - 50).setCentered(true));
        GridLayout gridLayout = new GridLayout().columnSpacing(5);
        GridLayout.RowHelper rowHelper1 = gridLayout.createRowHelper(3);
        rowHelper1.addChild(Button.builder(GUI_OPEN_IN_BROWSER, button ->
                Util.getPlatform().openUri(SymlinkCheckMod.SYMLINK_MS_LINK)).size(120, 20).build());
        rowHelper1.addChild(Button.builder(GUI_COPY_LINK_TO_CLIPBOARD, button -> {
            if (this.minecraft != null) {
                this.minecraft.keyboardHandler.setClipboard(SymlinkCheckMod.SYMLINK_MS_LINK);
            }
        }).size(120, 20).build());
        rowHelper1.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).size(120, 20).build());
        rowHelper.addChild(gridLayout);
        this.repositionElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void render(PoseStack stack, int i, int j, float f) {
        this.renderBackground(stack);
        super.render(stack, i, j, f);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE_TEXT);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.callbackScreen);
        }
    }
}

