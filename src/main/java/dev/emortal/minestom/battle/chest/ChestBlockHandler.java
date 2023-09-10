package dev.emortal.minestom.battle.chest;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

final class ChestBlockHandler implements BlockHandler {

    private final Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "");
    private final AtomicInteger playersInside = new AtomicInteger(0);
//    private boolean unopenedSinceRefill = true;

    ChestBlockHandler() {
        super();
        this.refillInventory();
    }

    @NotNull Inventory getInventory() {
        return this.inventory;
    }

    int addPlayerInside() {
        return this.playersInside.incrementAndGet();
    }

    int removePlayerInside() {
        return this.playersInside.decrementAndGet();
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return Block.CHEST.namespace();
    }

//    public boolean isUnopenedSinceRefill() {
//        return unopenedSinceRefill;
//    }

    void refillInventory() {
//        unopenedSinceRefill = true

        this.inventory.clear();
        for (int i = 0; i < 7; i++) {
            addRandomly(this.inventory, Items.random());
        }
    }


    private static void addRandomly(@NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (true) {
            int randomSlot = random.nextInt(inventory.getSize());
            if (inventory.getItemStack(randomSlot) == ItemStack.AIR) {
                inventory.setItemStack(randomSlot, itemStack);
                break;
            }
        }
    }
}
