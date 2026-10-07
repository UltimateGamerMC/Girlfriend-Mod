package com.beckytidus.girlfriendmod.client.screen;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.network.GirlfriendActionPayload;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import com.beckytidus.girlfriendmod.util.RelationshipTier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

/** Sneak + right-click menu: cuddles, settings, name and look. */
@Environment(EnvType.CLIENT)
public class GirlfriendScreen extends Screen {
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 214;
    private static final int PINK = 0xFFFF7EB6;
    private static final int TEXT = 0xFFEDE3F0;
    private static final int MUTED = 0xFFA99BB0;

    private final GirlFriendEntity girlfriend;
    private EditBox nameBox;
    private EditBox skinBox;
    private Button followButton;
    private Button sitButton;
    private Button friendlyFireButton;
    private Button forgiveButton;
    private Button lookButton;
    private int left;
    private int top;

    public GirlfriendScreen(GirlFriendEntity girlfriend) {
        super(Component.literal(girlfriend.getDisplayNameForChat()));
        this.girlfriend = girlfriend;
    }

    private void send(String action, String argument) {
        ClientPlayNetworking.send(new GirlfriendActionPayload(girlfriend.getId(), action, argument));
    }

    private void send(String action) {
        send(action, "");
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        int x = left + 104;
        int y = top + 76;
        int bw = 52;

        addRenderableWidget(Button.builder(Component.literal("Hug"), b -> send("hug")).bounds(x, y, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Headpat"), b -> send("headpat")).bounds(x + 56, y, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Kiss"), b -> send("kiss")).bounds(x + 112, y, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Dance"), b -> send("dance")).bounds(x + 168, y, bw, 20).build());

        y += 24;
        followButton = addRenderableWidget(Button.builder(Component.empty(), b -> send("follow")).bounds(x, y, 108, 20).build());
        sitButton = addRenderableWidget(Button.builder(Component.empty(), b -> send("sit")).bounds(x + 112, y, 108, 20).build());

        y += 24;
        friendlyFireButton = addRenderableWidget(Button.builder(Component.empty(), b -> send("friendlyfire")).bounds(x, y, 108, 20).build());
        forgiveButton = addRenderableWidget(Button.builder(Component.literal("Say sorry ♥"), b -> send("forgive")).bounds(x + 112, y, 108, 20).build());

        y += 28;
        nameBox = new EditBox(font, x, y, 160, 18, Component.literal("Name"));
        nameBox.setMaxLength(24);
        nameBox.setValue(girlfriend.getPlayerCustomName());
        addRenderableWidget(nameBox);
        addRenderableWidget(Button.builder(Component.literal("Rename"), b -> send("rename", nameBox.getValue())).bounds(x + 164, y - 1, 56, 20).build());

        y += 24;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleLook(-1)).bounds(x, y, 20, 20).build());
        lookButton = addRenderableWidget(Button.builder(Component.empty(), b -> send("skin_reset")).bounds(x + 22, y, 116, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleLook(1)).bounds(x + 140, y, 20, 20).build());

        y += 24;
        skinBox = new EditBox(font, x, y, 160, 18, Component.literal("Player skin"));
        skinBox.setMaxLength(16);
        skinBox.setHint(Component.literal("Any Minecraft username"));
        addRenderableWidget(skinBox);
        addRenderableWidget(Button.builder(Component.literal("Use skin"), b -> {
            if (!skinBox.getValue().isBlank()) send("skin_player", skinBox.getValue());
        }).bounds(x + 164, y - 1, 56, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(left + 8, top + PANEL_H - 28, 88, 20).build());
        refreshLabels();
    }

    private int currentLook() {
        try {
            return Integer.parseInt(girlfriend.getTextureVariant());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void cycleLook(int direction) {
        int next = Math.floorMod(currentLook() - 1 + direction, GirlfriendSkins.TEXTURE_COUNT) + 1;
        send("skin_variant", String.valueOf(next));
    }

    private void refreshLabels() {
        followButton.setMessage(Component.literal(girlfriend.isFollowingOwner() ? "Following you" : "Waiting here"));
        sitButton.setMessage(Component.literal(girlfriend.isSitting() ? "Stand up" : "Sit down"));
        friendlyFireButton.setMessage(Component.literal("Your hits: " + (girlfriend.isFriendlyFire() ? "hurt her" : "harmless")));
        forgiveButton.active = girlfriend.isSulking();
        boolean playerSkin = !girlfriend.getSkinOwnerName().isEmpty();
        lookButton.setMessage(Component.literal(playerSkin ? "Skin: " + girlfriend.getSkinOwnerName() + " (reset)" : "Look " + currentLook() + " / " + GirlfriendSkins.TEXTURE_COUNT));
        lookButton.active = playerSkin;
    }

    @Override
    public void tick() {
        super.tick();
        if (!girlfriend.isAlive() || minecraft.player == null || girlfriend.distanceToSqr(minecraft.player) > 12 * 12) {
            onClose();
            return;
        }
        refreshLabels();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, PINK);
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0201820);
        graphics.fill(left + 6, top + 6, left + 98, top + PANEL_H - 34, 0xFF2C2230);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, left + 8, top + 8, left + 96, top + PANEL_H - 36, 42, 0.0625F, mouseX, mouseY, girlfriend);

        RelationshipTier tier = girlfriend.getRelationshipTier();
        int x = left + 104;
        graphics.text(font, Component.literal(girlfriend.getDisplayNameForChat()).withColor(0xF5A9F2), x, top + 9, PINK, true);
        graphics.text(font, Component.literal(tier.title).withStyle(tier.color), x + font.width(girlfriend.getDisplayNameForChat()) + 6, top + 9, TEXT, true);
        graphics.text(font, GirlfriendText.hearts(girlfriend.getRelationshipLevel(), 100, 10).append(Component.literal("  " + girlfriend.getRelationshipLevel() + "/100").withColor(MUTED)), x, top + 21, TEXT, false);
        bar(graphics, x, top + 34, "Mood", girlfriend.getMoodLevel(), 0xFFFFB3D9);
        bar(graphics, x, top + 44, "Hunger", girlfriend.getHunger(), 0xFFFFD27F);
        bar(graphics, x, top + 54, "Health", (int) (100 * girlfriend.getHealth() / girlfriend.getMaxHealth()), 0xFF8EE89A);
        String status = girlfriend.isSulking() ? "She's sulking... maybe a gift or a sorry?" : tier.perk;
        graphics.text(font, font.plainSubstrByWidth(status, 220), x, top + 65, MUTED, false);
    }

    private void bar(GuiGraphicsExtractor graphics, int x, int y, String label, int value, int color) {
        graphics.text(font, label, x, y, MUTED, false);
        int bx = x + 40;
        graphics.fill(bx, y + 2, bx + 100, y + 7, 0xFF3A2E3E);
        graphics.fill(bx, y + 2, bx + Math.max(0, Math.min(100, value)), y + 7, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
