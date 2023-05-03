package dev.emortal.minestom.battle.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class NoPhysicsEntity extends Entity {

    public NoPhysicsEntity(EntityType type) {
        super(type);

        this.setNoGravity(true);
        this.hasPhysics = false;
    }

}
