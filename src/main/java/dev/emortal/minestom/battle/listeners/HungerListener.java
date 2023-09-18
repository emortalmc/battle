package dev.emortal.minestom.battle.listeners;

import io.github.bloepiloepi.pvp.events.PlayerExhaustEvent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public final class HungerListener {

    public static void register(@NotNull Instance instance) {
        instance.eventNode().addListener(PlayerExhaustEvent.class, HungerListener::onExhaust);
    }

    private static void onExhaust(@NotNull PlayerExhaustEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.ADVENTURE) return;

        event.setCancelled(true);
        player.setFood(20);
    }

    private HungerListener() {
    }
}
