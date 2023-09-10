package dev.emortal.minestom.battle;

import io.github.bloepiloepi.pvp.entity.CustomPlayer;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fixes issues with MinestomPVP while testing.
 */
public final class AllPermissionCustomPlayer extends CustomPlayer {

    public AllPermissionCustomPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String permissionName) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String permissionName, @Nullable PermissionVerifier permissionVerifier) {
        return true;
    }
}
