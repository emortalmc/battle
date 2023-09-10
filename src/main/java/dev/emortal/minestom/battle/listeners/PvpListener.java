package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.game.BattleGame;
import dev.emortal.minestom.battle.game.PlayerTeams;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.events.EntityPreDeathEvent;
import io.github.bloepiloepi.pvp.events.FinalDamageEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Iterator;

public final class PvpListener {
    public static final @NotNull Tag<Integer> KILLS_TAG = Tag.Integer("kills");
//    public static final @NotNull Tag<Boolean> INVULNERABLE_TAG = Tag.Boolean("invulnerable");

    private static final Title YOU_DIED_TITLE = Title.title(
            Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
            Component.empty(),
            Title.DEFAULT_TIMES
    );

    private final @NotNull BattleGame game;

    public PvpListener(@NotNull BattleGame game) {
        this.game = game;

        game.getEventNode().addListener(FinalDamageEvent.class, this::onFinalDamage);
        game.getEventNode().addListener(EntityPreDeathEvent.class, this::onPreDeath);
        game.getEventNode().addListener(PlayerTickEvent.class, this::onTick);
    }

    private void onFinalDamage(@NotNull FinalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.isInvulnerable()) {
            event.setCancelled(true);
            return;
        }

        CustomDamageType type = event.getDamageType();
        Entity source = type.getEntity();

        float animation = source != null ? source.getPosition().yaw() : -1F;
        this.sendHitAnimation(player, animation);
    }

    private void sendHitAnimation(@NotNull Entity entity, float animation) {
        this.game.sendGroupedPacket(new HitAnimationPacket(entity.getEntityId(), animation));
    }

    private void onPreDeath(@NotNull EntityPreDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        event.setCancelDeath(true);

        // Get last player that hit
        DamageType lastDamage = player.getLastDamageSource();
        // CustomEntityDamage should always be used over EntityDamage due to MinestomPvP
        if (lastDamage instanceof CustomEntityDamage damage && damage.getEntity() instanceof Player killer) {
            this.handlePlayerDeath(player, killer);
        } else {
            this.handlePlayerDeath(player, null);
        }

        this.game.checkPlayerCounts();
    }

    private void handlePlayerDeath(@NotNull Player target, @Nullable Player killer) {
        this.game.playSound(Sound.sound(Key.key("battle.death"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        this.resetPlayerAfterDeath(target);

        if (killer != null) {
            Integer lastKills = target.getTag(KILLS_TAG);
            if (lastKills == null) {
                lastKills = 0;
            }
            target.setTag(KILLS_TAG, lastKills + 1);

            killer.showTitle(Title.title(
                    Component.empty(),
                    Component.text().append(Component.text("☠ " + target.getUsername(), NamedTextColor.RED)).build(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
            ));
        }

        this.sendDeathMessage(target, killer);
    }

    private void resetPlayerAfterDeath(@NotNull Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setTeam(PlayerTeams.DEAD);
        player.heal();
        player.setFood(20);
        player.setAutoViewable(false);
        player.showTitle(YOU_DIED_TITLE);
    }

    private @NotNull Component sendDeathMessage(@NotNull Player target, @Nullable Player killer) {
        TextComponent.Builder result = Component.text()
                .append(Component.text("☠", NamedTextColor.RED))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(target.getUsername(), NamedTextColor.RED));

        if (killer != null) {
            result.append(Component.text(" was slain by ", NamedTextColor.GRAY));
            result.append(Component.text(killer.getUsername(), NamedTextColor.WHITE));
        } else {
            result.append(Component.text(" died", NamedTextColor.GRAY));
        }

        return result.build();
    }

    private void onTick(@NotNull PlayerTickEvent event) {
        Player player = event.getPlayer();
        if (player.isInvulnerable()) return;

//        if (borderActive && player.gameMode == GameMode.ADVENTURE) {
//            val point = player.position
//            val radius: Double = (e.instance.worldBorder.diameter / 2.0) + 1.5
//            val checkX = point.x() <= e.instance.worldBorder.centerX + radius && point.x() >= e.instance.worldBorder.centerX - radius
//            val checkZ = point.z() <= e.instance.worldBorder.centerZ + radius && point.z() >= e.instance.worldBorder.centerZ - radius
//
//            if (!checkX || !checkZ) {
//                kill(player)
//            }
//        }

        Iterator<Point> blocksInHitbox = player.getBoundingBox().getBlocks(player.getPosition());

        while (blocksInHitbox.hasNext()) {
            Block block = event.getInstance().getBlock(blocksInHitbox.next(), Block.Getter.Condition.TYPE);
            this.updateBurning(player, block);
        }
    }

    private void updateBurning(@NotNull Player player, @NotNull Block block) {
        if (block.compare(Block.WATER)) {
            player.setOnFire(false);
        } else if (block.compare(Block.FIRE)) {
            this.doBurningDamage(player, 6, CustomDamageType.IN_FIRE, 1F);
        } else if (block.compare(Block.LAVA)) {
            this.doBurningDamage(player, 12, CustomDamageType.LAVA, 4F);
        }
    }

    private void doBurningDamage(@NotNull Player player, int durationSeconds, @NotNull CustomDamageType damageType, float damageAmount) {
        EntityUtils.setFireForDuration(player, Duration.ofSeconds(durationSeconds));
        if (player.getAliveTicks() % 10L != 0L) return;

        boolean hasFireResistance = false;
        for (TimedPotion activeEffect : player.getActiveEffects()) {
            if (activeEffect.getPotion().effect() == PotionEffect.FIRE_RESISTANCE) {
                hasFireResistance = true;
                break;
            }
        }

        if (hasFireResistance) return;
        player.damage(CustomDamageType.IN_FIRE, 1.0f);
    }
}
