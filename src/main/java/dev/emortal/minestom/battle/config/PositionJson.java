package dev.emortal.minestom.battle.config;

import net.minestom.server.coordinate.Vec;

public class PositionJson {
    public double x;
    public double y;
    public double z;

    public Vec asVec() {
        return new Vec(x, y, z);
    }
}