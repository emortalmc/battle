package dev.emortal.minestom.battle.chest;

import dev.emortal.minestom.battle.listeners.ChestListener;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class ChestHandler implements BlockHandler {
    private final Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "");
//    private boolean unopenedSinceRefill = true;

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return Block.CHEST.namespace();
    }

//    public boolean isUnopenedSinceRefill() {
//        return unopenedSinceRefill;
//    }

    public ChestHandler() {
        super();
        refillInventory();
    }

    public void refillInventory() {
//        unopenedSinceRefill = true

        inventory.clear();
        for (int i = 0; i < 7; i++) {
            addRandomly(inventory, Items.randomItem());
        }
    }


    private static void addRandomly(Inventory inventory, ItemStack itemStack) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        while (true) {
            int randomSlot = rand.nextInt(inventory.getSize());
            if (inventory.getItemStack(randomSlot) == ItemStack.AIR) {
                inventory.setItemStack(randomSlot, itemStack);
                break;
            }
        }
    }
}
