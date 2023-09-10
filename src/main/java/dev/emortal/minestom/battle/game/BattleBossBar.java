package dev.emortal.minestom.battle.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class BattleBossBar {
    private static final TextColor REMAINING_COLOR = TextColor.fromHexString("#cdffc4");
    private static final TextColor REMAINING_TEXT_COLOR = TextColor.fromHexString("#8fff82");

    private final BossBar bossBar = BossBar.bossBar(Component.empty(), 0F, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

    public void show(@NotNull Audience audience) {
        audience.showBossBar(this.bossBar);
    }

    public void hide(@NotNull Audience audience) {
        audience.hideBossBar(this.bossBar);
    }

    public void updateRemaining(int remaining) {
        Component remainingText = Component.text()
                .append(Component.text(remaining, REMAINING_COLOR, TextDecoration.BOLD))
                .append(Component.text(" remaining", REMAINING_TEXT_COLOR))
                .build();
        this.bossBar.name(remainingText);
    }

    public void updateProgress(float progress) {
        this.bossBar.progress(progress);
    }

    public void delete() {
        MinecraftServer.getBossBarManager().destroyBossBar(this.bossBar);
    }
}
