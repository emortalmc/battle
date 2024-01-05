package dev.emortal.minestom.battle.game;

import dev.emortal.minestom.battle.chest.ChestUpdateHandler;
import dev.emortal.minestom.battle.entity.NoPhysicsEntity;
import dev.emortal.minestom.battle.listeners.HungerListener;
import dev.emortal.minestom.battle.listeners.PvpListener;
import dev.emortal.minestom.battle.map.LoadedMap;
import dev.emortal.minestom.battle.map.MapData;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import io.github.bloepiloepi.pvp.PvpExtension;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

final class GameStartHandler {

    private final @NotNull BattleGame game;
    private final @NotNull LoadedMap map;

    private final Set<Entity> freezeEntities = new HashSet<>();

    GameStartHandler(@NotNull BattleGame game, @NotNull LoadedMap map) {
        this.game = game;
        this.map = map;
    }

    @NotNull Task createTimerTask(@NotNull BattleBossBar bossBar) {
        return this.map.instance().scheduler().submitTask(new InitialTimerTask(this.game, bossBar, this.freezeEntities));
    }

    void freezePlayers() {
        double playerStep = 2 * Math.PI / 8;

        double circleIndex = 0.0;
        for (Player player : this.game.getPlayers()) {
            PvpExtension.setLegacyAttack(player, true);
            player.setFlying(false);
            player.setAllowFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            player.setInvulnerable(true);

            this.moveToInitialPositionAndFreeze(player, circleIndex);
            circleIndex += playerStep;
        }
    }

    private void moveToInitialPositionAndFreeze(@NotNull Player player, double circleIndex) {
        Pos pos = this.findInitialPosition(circleIndex);
        player.teleport(pos);
        this.freezePlayer(player, pos);
    }

    private @NotNull Pos findInitialPosition(double circleIndex) {
        MapData mapData = this.map.data();

        double x = Math.sin(circleIndex) * mapData.circleRadius();
        double z = Math.cos(circleIndex) * mapData.circleRadius();

        return mapData.circleCenter().add(x, 0, z).withLookAt(mapData.circleCenter());
    }

    private void freezePlayer(@NotNull Player player, @NotNull Pos pos) {
        // Freeze players by using riding entity
        Entity freezeEntity = new NoPhysicsEntity(EntityType.AREA_EFFECT_CLOUD);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) freezeEntity.getEntityMeta();
        meta.setRadius(0f);

        freezeEntity.setInstance(this.map.instance(), pos).thenRun(() -> freezeEntity.addPassenger(player));
        this.freezeEntities.add(freezeEntity);
    }

    private static final class InitialTimerTask implements Supplier<TaskSchedule> {
        private static final Title.Times DEFAULT_TIMES = Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500));

        private final @NotNull BattleGame game;
        private final @NotNull BattleBossBar bossBar;
        private final @NotNull Set<Entity> freezeEntities;

        private int secondsLeft = MinestomGameServer.TEST_MODE ? 2 : 10;

        InitialTimerTask(@NotNull BattleGame game, @NotNull BattleBossBar bossBar, @NotNull Set<Entity> freezeEntities) {
            this.game = game;
            this.bossBar = bossBar;
            this.freezeEntities = freezeEntities;
        }

        @Override
        public TaskSchedule get() {
            if (this.secondsLeft == 0) {
                this.startGame();
                return TaskSchedule.stop();
            }

            this.game.showTitle(Title.title(Component.empty(), Component.text(this.secondsLeft), DEFAULT_TIMES));
            if (this.secondsLeft <= 5) {
                this.game.playSound(Sound.sound(Key.key("battle.countdown.begin2"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
            }

            this.secondsLeft--;
            return TaskSchedule.seconds(1);
        }

        private void startGame() {
            this.notifyGameStarted();
            this.unfreezeAll();

            this.registerEvents();
            this.bossBar.show(this.game);

            this.game.checkPlayerCounts();
            this.game.beginTimer();
        }

        private void notifyGameStarted() {
            this.game.showTitle(Title.title(Component.empty(), Component.text("Round start!"), DEFAULT_TIMES));
            this.game.playSound(Sound.sound(Key.key("battle.countdown.beginover"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        }

        private void unfreezeAll() {
            for (Entity freezeEntity : this.freezeEntities) {
                freezeEntity.remove();
            }
            this.freezeEntities.clear();
        }

        private void registerEvents() {
            new PvpListener(this.game);
            new ChestUpdateHandler(this.game);
            HungerListener.register(this.game.getInstance());
        }
    }
}
