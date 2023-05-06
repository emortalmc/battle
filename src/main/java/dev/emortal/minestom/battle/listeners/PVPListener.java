package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.BattleGame;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.events.EntityPreDeathEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.title.Title.DEFAULT_TIMES;

public class PVPListener {

    private static final Title YOU_DIED_TITLE = Title.title(
            Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
            Component.empty(),
            DEFAULT_TIMES
    );

    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
    public static final Tag<Boolean> INVULNERABLE_TAG = Tag.Boolean("invulnerable");

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        eventNode.addListener(EntityDamageEvent.class, e -> {
            if (!(e.getEntity() instanceof Player player)) return;
            if (player.hasTag(INVULNERABLE_TAG)) {
                e.setCancelled(true);
            }
        });

        eventNode.addListener(EntityPreDeathEvent.class, e -> {
            if (!(e.getEntity() instanceof Player player)) return;

            e.setCancelDeath(true);

            // Get last player that hit
            DamageType lastDmg = player.getLastDamageSource();
            if (lastDmg instanceof CustomEntityDamage cd && cd.getEntity() instanceof Player killer) { // CustomEntityDamage should always be used over EntityDamage due to MinestomPvP
                playerDied(game, player, killer);
            } else {
                playerDied(game, player, null);
            }

            game.checkPlayerCounts();
        });
    }

    public static void playerDied(BattleGame game, Player player, @Nullable Player killer) {
        game.getAudience().playSound(Sound.sound(Key.key("battle.death"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());


        player.setGameMode(GameMode.SPECTATOR);
        player.setTeam(BattleGame.DEAD_TEAM);
        player.heal();
        player.setFood(20);
        player.setAutoViewable(false);
        player.showTitle(YOU_DIED_TITLE);



        if (killer != null) {
            Integer lastKills = player.getTag(KILLS_TAG);
            if (lastKills == null) lastKills = 0;
            player.setTag(KILLS_TAG, lastKills + 1);

            killer.showTitle(Title.title(
                    Component.empty(),
                    Component.text()
                            .append(Component.text(player.getUsername(), NamedTextColor.RED))
                            .build(),
                    DEFAULT_TIMES
            ));

            game.getAudience().sendMessage(
                    Component.text()
                            .append(Component.text("☠", NamedTextColor.RED))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(player.getUsername(), NamedTextColor.RED))
                            .append(Component.text(" was slain by ", NamedTextColor.GRAY))
                            .append(Component.text(killer.getUsername(), NamedTextColor.WHITE))
            );
        } else {
            game.getAudience().sendMessage(
                    Component.text()
                            .append(Component.text("☠", NamedTextColor.RED))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(player.getUsername(), NamedTextColor.RED))
                            .append(Component.text(" died", NamedTextColor.GRAY))
            );
        }

    }

}
