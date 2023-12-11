package dev.emortal.minestom.battle.game;

import com.google.common.collect.Sets;
import dev.emortal.minestom.battle.listeners.PvpListener;
import dev.emortal.minestom.battle.map.LoadedMap;
import dev.emortal.minestom.battle.map.MapData;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import dev.emortal.minestom.gamesdk.util.GameWinLoseMessages;
import io.github.bloepiloepi.pvp.config.DamageConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BattleGame extends Game {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final @NotNull LoadedMap map;

    private final BattleBossBar bossBar = new BattleBossBar();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean ended = new AtomicBoolean();

    private @Nullable Task gameTimerTask;

    public BattleGame(@NotNull GameCreationInfo creationInfo, @NotNull LoadedMap map) {
        super(creationInfo);
        this.map = map;
    }

    @Override
    public void onJoin(@NotNull Player player) {
//        player.setFlying(false);
//        player.setAllowFlying(true);
        player.setAutoViewable(true);
        player.setTeam(PlayerTeams.ALIVE);
        player.setGlowing(false);
        player.setInvulnerable(true);
//        player.setGameMode(GameMode.SPECTATOR);

        MapData data = this.map.data();
        player.setRespawnPoint(data.circleCenter().add(0, 0, -data.circleRadius()));
    }

    @Override
    public void onLeave(@NotNull Player player) {
        player.setTeam(null);
        player.clearEffects();
        PotionListener.durationLeftMap.remove(player.getUuid()); // probably works fine but im paranoid

        this.checkPlayerCounts();
    }

    @Override
    public void start() {
        this.started.set(true);

        this.getEventNode().addChild(
                PvPConfig.legacyBuilder()
                        .damage(DamageConfig.legacyBuilder().shield(false))
                        .build()
                        .createNode()
        );

        GameStartHandler startHandler = new GameStartHandler(this, this.map);
        startHandler.freezePlayers();
        this.gameTimerTask = startHandler.createTimerTask(this.bossBar);
    }

    void beginTimer() {
        this.gameTimerTask = this.map.instance().scheduler().submitTask(new GameTimerTask(this, this.bossBar));
    }

    public void checkPlayerCounts() {
        Set<Player> alivePlayers = this.getAlivePlayers();

        if (alivePlayers.isEmpty()) {
            this.finish();
            return;
        }

        if (alivePlayers.size() == 1) {
            if (this.started.get()) {
                this.victory(alivePlayers.iterator().next());
            } else {
                this.finish();
            }

            return;
        }

        // TODO: update player count in a scoreboard
        this.bossBar.updateRemaining(alivePlayers.size());
    }

    public void victory(@Nullable Player winner) {
        this.ended.set(true);

        if (this.gameTimerTask != null) {
            this.gameTimerTask.cancel();
        }

        Title victoryTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ffc570:gold><bold>VICTORY!"),
                Component.text(GameWinLoseMessages.randomVictory(), NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );
        Title defeatTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ff474e:#ff0d0d><bold>DEFEAT!"),
                Component.text(GameWinLoseMessages.randomDefeat(), NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );

        Sound defeatSound = Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1f, 1f);
        Sound victorySound = Sound.sound(SoundEvent.BLOCK_BEACON_POWER_SELECT, Sound.Source.MASTER, 1f, 0.8f);

        // If there is no winner, choose the player with the highest kills
        if (winner == null) {
            winner = this.findPlayerWithHighestKills();
        }

        for (Player player : this.getPlayers()) {
            player.setInvulnerable(true);
            this.bossBar.hide(player);

            if (winner == player) {
                player.showTitle(victoryTitle);
                player.playSound(victorySound, Sound.Emitter.self());
            } else {
                player.showTitle(defeatTitle);
                player.playSound(defeatSound, Sound.Emitter.self());
            }
        }

        this.map.instance().scheduler().buildTask(this::finish)
                .delay(TaskSchedule.seconds(6))
                .schedule();
    }

    private @Nullable Player findPlayerWithHighestKills() {
        int killsRecord = 0;
        Player highestKiller = null;

        for (Player player : this.getPlayers()) {
            Integer playerKills = player.getTag(PvpListener.KILLS_TAG);
            if (playerKills == null) playerKills = 0;
            if (playerKills > killsRecord) {
                killsRecord = playerKills;
                highestKiller = player;
            }
        }

        return highestKiller;
    }

    @Override
    public void cleanUp() {
        this.map.instance().scheduleNextTick(MinecraftServer.getInstanceManager()::unregisterInstance);
        this.bossBar.delete();
    }

    @Override
    public @NotNull Instance getSpawningInstance() {
        return this.map.instance();
    }

    public @NotNull Set<Player> getAlivePlayers() {
        return Collections.unmodifiableSet(Sets.filter(this.getPlayers(), player -> player.getGameMode() == GameMode.ADVENTURE));
    }

    public boolean hasEnded() {
        return this.ended.get();
    }
}
