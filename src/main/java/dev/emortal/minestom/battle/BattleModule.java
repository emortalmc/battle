package dev.emortal.minestom.battle;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.emortal.minestom.core.module.Module;
import dev.emortal.minestom.core.module.ModuleData;
import dev.emortal.minestom.core.module.ModuleEnvironment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.battle.config.MapConfigJson;
import dev.emortal.minestom.battle.map.MapManager;
import io.github.bloepiloepi.pvp.PvpExtension;
import io.github.bloepiloepi.pvp.config.DamageConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.enchantment.CustomEnchantments;
import io.github.bloepiloepi.pvp.entity.CustomPlayer;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ModuleData(name = "battle", softDependencies = {GameSdkModule.class}, required = true)
public class BattleModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleModule.class);
    private static final Gson GSON = new Gson();

    public static Map<String, MapConfigJson> MAP_CONFIG_MAP;

    protected BattleModule(@NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("maps.json");
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Type type = new TypeToken<HashMap<String, MapConfigJson>>(){}.getType();
        MAP_CONFIG_MAP = GSON.fromJson(reader, type);

        MapManager mapManager = new MapManager();

        GameSdkModule.init(
                new GameSdkConfig.Builder()
                        .minPlayers(BattleGame.MIN_PLAYERS)
                        .maxGames(10)
                        .gameSupplier((info, node) -> new BattleGame(info, node, mapManager.getMap(info.mapId())))
                        .build()
        );

        PvpExtension.init();

        if (GameSdkModule.TEST_MODE) {
            // Fix all permissions not working due to MinestomPVP's custom player provider
            MinecraftServer.getConnectionManager().setPlayerProvider(AllPermissionCustomPlayer::new);
        }

        MinecraftServer.getGlobalEventHandler().addChild(
                PvPConfig.legacyBuilder()
                        .damage(DamageConfig.legacyBuilder().shield(false))
                        .build().createNode()
        );

        return true;
    }

    @Override
    public void onUnload() {

    }
}