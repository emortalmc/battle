package dev.emortal.minestom.battle.map;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record MapData(double circleRadius, @NotNull Pos circleCenter) {
}
