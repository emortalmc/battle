package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.BattleGame;
import io.github.bloepiloepi.pvp.events.PlayerExhaustEvent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;

public class HungerListener {

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        eventNode.addListener(PlayerExhaustEvent.class, e -> {
            if (e.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                e.setCancelled(true);
                e.getPlayer().setFood(20);
            }
        });
    }

}
