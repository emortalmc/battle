package dev.emortal.minestom.battle;

import dev.emortal.minestom.battle.game.BattleGame;
import dev.emortal.minestom.battle.map.MapManager;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import io.github.bloepiloepi.pvp.PvpExtension;
import net.minestom.server.MinecraftServer;

public final class Main {
    private static final int MIN_PLAYERS = 2;

    public static void main(String[] args) {
        MinestomGameServer.create(() -> {
            PvpExtension.init();
            if (MinestomGameServer.TEST_MODE) {
                // Fix all permissions not working due to MinestomPVP's custom player provider
                MinecraftServer.getConnectionManager().setPlayerProvider(AllPermissionCustomPlayer::new);
            }

            MapManager mapManager = new MapManager();

            return GameSdkConfig.builder()
                    .minPlayers(MIN_PLAYERS)
                    .gameCreator(info -> new BattleGame(info, mapManager.getMap(info.mapId())))
                    .build();
        });
    }
}