package dev.emortal.minestom.parkourtag;

import dev.emortal.api.kurushimi.KurushimiMinestomUtils;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import dev.emortal.minestom.parkourtag.listeners.PVPListener;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class BattleGame extends Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleGame.class);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Pos SPAWN_POINT = new Pos(0.5, 65.0, 0.5);

    public static final Team ALIVE_TEAM = MinecraftServer.getTeamManager().createBuilder("alive")
            .teamColor(NamedTextColor.RED)
            .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
            .updateTeamPacket()
            .build();
    public static final Team DEAD_TEAM = MinecraftServer.getTeamManager().createBuilder("dead")
            .teamColor(NamedTextColor.GRAY)
            .prefix(Component.text("☠ ", NamedTextColor.GRAY))
            .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
            .updateTeamPacket()
            .build();

    public static final int MIN_PLAYERS = 2;

    public final @NotNull Instance instance;


    private final BossBar bossBar = BossBar.bossBar(Component.empty(), 0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);

    private @Nullable Task gameTimerTask;

    protected BattleGame(@NotNull GameCreationInfo creationInfo, @NotNull EventNode<Event> gameEventNode, @NotNull Instance instance) {
        super(creationInfo, gameEventNode);

        instance.setTimeRate(0);
        instance.setTimeUpdate(null);
        this.instance = instance;

        gameEventNode.addListener(PlayerDisconnectEvent.class, event -> {
            if (this.players.remove(event.getPlayer())) this.checkPlayerCounts();
        });
    }

    @Override
    public void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!getGameCreationInfo().playerIds().contains(player.getUuid())) {
            player.kick("Unexpected join (" + Environment.getHostname() + ")");
            LOGGER.info("Unexpected join for player {}", player.getUuid());
            return;
        }

        player.setRespawnPoint(SPAWN_POINT);
        event.setSpawningInstance(this.instance);
        this.players.add(player);

        player.setFlying(false);
        player.setAllowFlying(false);
        player.setAutoViewable(true);
        player.setTeam(ALIVE_TEAM);
        player.setGlowing(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.showBossBar(this.bossBar);
    }

    public void start() {
        for (Player player : this.players) {
            player.hideBossBar(this.bossBar);
        }

        this.audience.playSound(Sound.sound(SoundEvent.BLOCK_PORTAL_TRIGGER, Sound.Source.MASTER, 0.45f, 1.27f));

        this.instance.scheduler().submitTask(new Supplier<>() {
            int i = 3;

            @Override
            public TaskSchedule get() {
                if (i == 0) {
                    beginGame();
                    return TaskSchedule.stop();
                }

                audience.playSound(Sound.sound(Key.key("battle.countdown.begin"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

                audience.showTitle(
                        Title.title(
                                Component.empty(),
                                Component.text(i, NamedTextColor.LIGHT_PURPLE),
                                Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500))
                        )
                );

                i--;
                return TaskSchedule.seconds(1);
            }
        });
    }

    private void beginGame() {
        beginTimer();
    }

    private void beginTimer() {
        final int playTime = 120 + (40 * getPlayers().size());
        this.gameTimerTask = this.instance.scheduler().submitTask(new Supplier<>() {
            int secondsLeft = playTime;
            final int glowing = secondsLeft - 60;

            @Override
            public TaskSchedule get() {
                if (secondsLeft == 0) {
                    victory(null);

                    return TaskSchedule.stop();
                }

                if (secondsLeft <= 10) {
                    audience.playSound(Sound.sound(Key.key("minecraft:battle.showdown.count" + (secondsLeft % 2 + 1)), Sound.Source.MASTER, 0.7f, 1f), Sound.Emitter.self());
                    audience.showTitle(
                            Title.title(
                                    Component.empty(),
                                    Component.text(secondsLeft, TextColor.lerp(secondsLeft / 10f, NamedTextColor.RED, NamedTextColor.GREEN)),
                                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO)
                            )
                    );
                }

                if (secondsLeft == glowing) {
                    audience.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_HAT, Sound.Source.MASTER, 1f, 1.5f), Sound.Emitter.self());
                    audience.showTitle(
                            Title.title(
                                    Component.empty(),
                                    Component.text("Everyone is now glowing!", NamedTextColor.GRAY),
                                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(2))
                            )
                    );

                    for (Player alive : getAlivePlayers()) {
                        alive.setGlowing(true);
                    }
                }

                bossBar.progress((float) secondsLeft / (float) playTime);

                secondsLeft--;
                return TaskSchedule.seconds(1);
            }
        });
    }

    public void checkPlayerCounts() {
        Set<Player> alivePlayers = getAlivePlayers();

        if (alivePlayers.isEmpty()) {
            victory(null);
            return;
        }
        if (alivePlayers.size() == 1) {
            victory(alivePlayers.iterator().next());
            return;
        }

        // TODO: update player count in a scoreboard
        bossBar.name(
                Component.text()
                        .append(Component.text(alivePlayers.size(), TextColor.fromHexString("#cdffc4"), TextDecoration.BOLD))
                        .append(Component.text(" remaining", TextColor.fromHexString("#8fff82")))
                        .build()
        );
    }

    public Set<Player> getAlivePlayers() {
        Set<Player> alive = new HashSet<>();
        for (Player player : players) {
            if (player.getGameMode() != GameMode.ADVENTURE) alive.add(player);
        }

        return alive;
    }

    private void victory(@Nullable Player winner) {
        if (gameTimerTask != null) gameTimerTask.cancel();

        Title victoryTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ffc570:gold><bold>VICTORY!"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );
        Title defeatTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ff474e:#ff0d0d><bold>DEFEAT!"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );

        Sound defeatSound = Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1f, 1f);
        Sound victorySound = Sound.sound(SoundEvent.BLOCK_BEACON_POWER_SELECT, Sound.Source.MASTER, 1f, 0.8f);

        // If there is no winner, choose the player with the highest kills
        if (winner == null) {
            int killsRecord = 0;
            Player highestKiller = null;
            for (Player player : players) {
                int playerKills = player.getTag(PVPListener.KILLS_TAG);
                if (playerKills > killsRecord) {
                    killsRecord = playerKills;
                    highestKiller = player;
                }
            }

            winner = highestKiller;
        }

        for (Player player : players) {
            player.hideBossBar(bossBar);

            if (winner == player) {
                player.showTitle(victoryTitle);
                player.playSound(victorySound, Sound.Emitter.self());
            } else {
                player.showTitle(defeatTitle);
                player.playSound(defeatSound, Sound.Emitter.self());
            }
        }

        instance.scheduler().buildTask(this::sendBackToLobby)
                .delay(TaskSchedule.seconds(6))
                .schedule();
    }

    @Override
    public void cancel() {
        LOGGER.warn("Game cancelled");
        sendBackToLobby();
    }

    private void sendBackToLobby() {
        for (final Player player : players) {
            player.setTeam(null);
        }
        KurushimiMinestomUtils.sendToLobby(players, this::removeGame, this::removeGame);
    }

    private void removeGame() {
        GameSdkModule.getGameManager().removeGame(this);
        cleanUp();
    }

    private void cleanUp() {
        for (final Player player : this.players) {
            player.kick(Component.text("The game ended but we weren't able to connect you to a lobby. Please reconnect", NamedTextColor.RED));
        }
        MinecraftServer.getInstanceManager().unregisterInstance(this.instance);
        MinecraftServer.getBossBarManager().destroyBossBar(this.bossBar);
        if (this.gameTimerTask != null) this.gameTimerTask.cancel();
    }
}
