package dev.emortal.minestom.battle;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.emortal.minestom.battle.config.MapConfigJson;
import dev.emortal.minestom.battle.map.MapManager;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import io.github.bloepiloepi.pvp.PvpExtension;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Gson GSON = new Gson();

    public static Map<String, MapConfigJson> MAP_CONFIG_MAP;

    public static void main(String[] args) {
        InputStream is = Main.class.getClassLoader().getResourceAsStream("maps.json");
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Type type = new TypeToken<HashMap<String, MapConfigJson>>(){}.getType();
        MAP_CONFIG_MAP = GSON.fromJson(reader, type);


        MinestomGameServer.create(() -> {
            MapManager mapManager = new MapManager();

            PvpExtension.init();

            if (MinestomGameServer.TEST_MODE) {
                // Fix all permissions not working due to MinestomPVP's custom player provider
                MinecraftServer.getConnectionManager().setPlayerProvider(AllPermissionCustomPlayer::new);
            }

            return GameSdkConfig.builder()
                    .minPlayers(BattleGame.MIN_PLAYERS)
                    .maxGames(10)
                    .gameCreator(info -> new BattleGame(info, mapManager.getMap(info.mapId())))
                    .build();
        });
    }
}