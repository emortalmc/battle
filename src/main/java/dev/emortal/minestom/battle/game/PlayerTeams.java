package dev.emortal.minestom.battle.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public final class PlayerTeams {
    public static final @NotNull Team ALIVE = MinecraftServer.getTeamManager().createBuilder("alive")
            .teamColor(NamedTextColor.RED)
            .nameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS)
            .updateTeamPacket()
            .build();
    public static final @NotNull Team DEAD = MinecraftServer.getTeamManager().createBuilder("dead")
            .teamColor(NamedTextColor.GRAY)
            .prefix(Component.text("☠ ", NamedTextColor.GRAY))
            .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
            .updateTeamPacket()
            .build();
}
