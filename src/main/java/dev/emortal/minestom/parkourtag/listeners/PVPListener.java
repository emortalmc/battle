package dev.emortal.minestom.parkourtag.listeners;

import dev.emortal.minestom.parkourtag.BattleGame;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.tag.Tag;

public class PVPListener {

    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        eventNode.addListener(PlayerDeathEvent.class, e -> {

        });
    }

}
