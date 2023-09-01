package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.BattleGame;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.events.EntityPreDeathEvent;
import io.github.bloepiloepi.pvp.events.FinalDamageEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Iterator;

import static net.kyori.adventure.title.Title.DEFAULT_TIMES;

public class PVPListener {

    private static final Title YOU_DIED_TITLE = Title.title(
            Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
            Component.empty(),
            DEFAULT_TIMES
    );

    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
//    public static final Tag<Boolean> INVULNERABLE_TAG = Tag.Boolean("invulnerable");

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        eventNode.addListener(FinalDamageEvent.class, e -> {
            if (e.isCancelled()) return;
            if (!(e.getEntity() instanceof Player player)) return;
            if (player.isInvulnerable()) {
                e.setCancelled(true);
                return;
            }

            if (e.getDamageType().getEntity() != null) {
                game.instance.sendGroupedPacket(new HitAnimationPacket(e.getEntity().getEntityId(), e.getDamageType().getEntity().getPosition().yaw()));
            } else {
                game.instance.sendGroupedPacket(new HitAnimationPacket(e.getEntity().getEntityId(), -1));
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

        eventNode.addListener(PlayerTickEvent.class, e -> {
            if (e.getPlayer().isInvulnerable()) {
                return;
            }

//            if (borderActive && player.gameMode == GameMode.ADVENTURE) {
//                val point = player.position
//                val radius: Double = (e.instance.worldBorder.diameter / 2.0) + 1.5
//                val checkX = point.x() <= e.instance.worldBorder.centerX + radius && point.x() >= e.instance.worldBorder.centerX - radius
//                val checkZ = point.z() <= e.instance.worldBorder.centerZ + radius && point.z() >= e.instance.worldBorder.centerZ - radius
//
//                if (!checkX || !checkZ) {
//                    kill(player)
//                }
//            }


            Iterator<Point> blocksInHitbox = e.getPlayer().getBoundingBox().getBlocks(e.getPlayer().getPosition());

            while (blocksInHitbox.hasNext()) {
                Block block = e.getInstance().getBlock(blocksInHitbox.next(), Block.Getter.Condition.TYPE);

                // TODO: Could probably be cleaner
                if (block.compare(Block.WATER)) {
                    e.getPlayer().setOnFire(false);
                } else if (block.compare(Block.FIRE)) {
                    EntityUtils.setFireForDuration(e.getPlayer(), Duration.ofSeconds(6));

                    if (e.getPlayer().getAliveTicks() % 10L == 0L) {
                        boolean hasFireResistance = false;
                        for (TimedPotion activeEffect : e.getPlayer().getActiveEffects()) {
                            if (activeEffect.getPotion().effect() == PotionEffect.FIRE_RESISTANCE) {
                                hasFireResistance = true;
                                break;
                            }
                        }

                        if (hasFireResistance) return;
                        e.getPlayer().damage(CustomDamageType.IN_FIRE, 1.0f);
                    }
                } else if (block.compare(Block.LAVA)) {
                    EntityUtils.setFireForDuration(e.getPlayer(), Duration.ofSeconds(12));

                    if (e.getPlayer().getAliveTicks() % 10L == 0L) {
                        boolean hasFireResistance = false;
                        for (TimedPotion activeEffect : e.getPlayer().getActiveEffects()) {
                            if (activeEffect.getPotion().effect() == PotionEffect.FIRE_RESISTANCE) {
                                hasFireResistance = true;
                                break;
                            }
                        }

                        if (hasFireResistance) return;
                        e.getPlayer().damage(CustomDamageType.LAVA, 4.0f);
                    }
                }
            }
        });
    }

    public static void playerDied(BattleGame game, Player player, @Nullable Player killer) {
        game.playSound(Sound.sound(Key.key("battle.death"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());


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
                            .append(Component.text("☠ " + player.getUsername(), NamedTextColor.RED))
                            .build(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
            ));

            game.sendMessage(
                    Component.text()
                            .append(Component.text("☠", NamedTextColor.RED))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(player.getUsername(), NamedTextColor.RED))
                            .append(Component.text(" was slain by ", NamedTextColor.GRAY))
                            .append(Component.text(killer.getUsername(), NamedTextColor.WHITE))
            );
        } else {
            game.sendMessage(
                    Component.text()
                            .append(Component.text("☠", NamedTextColor.RED))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(player.getUsername(), NamedTextColor.RED))
                            .append(Component.text(" died", NamedTextColor.GRAY))
            );
        }

    }

}
