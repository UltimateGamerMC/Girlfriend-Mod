package com.beckytidus.girlfriendmod.client.hud;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import com.beckytidus.girlfriendmod.util.RelationshipTier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Small name / bond card under the crosshair while looking at your girlfriend. */
public final class GirlfriendHud {
    private GirlfriendHud() {
    }

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.hud.isHidden() || minecraft.player == null || minecraft.gui.screen() != null) return;
        if (!(minecraft.crosshairPickEntity instanceof GirlFriendEntity gf) || !gf.isAlive()) return;
        if (gf.distanceToSqr(minecraft.player) > 8 * 8) return;

        int w = minecraft.getWindow().getGuiScaledWidth();
        int y = 6;
        RelationshipTier tier = gf.getRelationshipTier();
        Component title = Component.literal(gf.getDisplayNameForChat()).withColor(0xF5A9F2)
            .append(Component.literal("  " + tier.title).withStyle(tier.color));
        Component hearts = GirlfriendText.hearts(gf.getRelationshipLevel(), 100, 10);
        String hint;
        if (!gf.hasOwner()) hint = "Right-click to say hi";
        else if (!gf.isOwnedBy(minecraft.player)) hint = "Already taken";
        else if (gf.isSulking()) hint = "Sulking... try a gift";
        else if (gf.getHunger() < 25) hint = "She looks hungry";
        else hint = "Sneak + right-click for menu";

        int width = Math.max(minecraft.font.width(title), Math.max(minecraft.font.width(hearts), minecraft.font.width(hint))) + 10;
        int x0 = (w - width) / 2;
        graphics.fill(x0, y - 3, x0 + width, y + 31, 0x90180F1C);
        graphics.text(minecraft.font, title, (w - minecraft.font.width(title)) / 2, y, 0xFFFFFFFF, true);
        graphics.text(minecraft.font, hearts, (w - minecraft.font.width(hearts)) / 2, y + 10, 0xFFFFFFFF, true);
        graphics.text(minecraft.font, hint, (w - minecraft.font.width(hint)) / 2, y + 20, 0xFFB8A9BE, false);
    }
}
