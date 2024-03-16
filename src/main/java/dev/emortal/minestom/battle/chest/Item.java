package dev.emortal.minestom.battle.chest;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class Item {

    private final @NotNull Material material;
    private final int weight;
    private final @NotNull Consumer<ItemStack.Builder> itemCreate;
    private final @NotNull ItemStack itemStack;

    public Item(@NotNull Material material, int weight, @NotNull Consumer<ItemStack.Builder> itemCreate) {
        this.material = material;
        this.weight = weight;
        this.itemCreate = itemCreate;
        this.itemStack = this.createItemStack();
    }

    public Item(@NotNull Material material, int weight) {
        this(material, weight, builder -> {});
    }

    private @NotNull ItemStack createItemStack() {
        ItemStack.Builder builder = ItemStack.builder(this.material);
        this.itemCreate.accept(builder);

        // TODO: maybe fix someday, MinestomPVP is a pain!!
//        Tool tool = Tool.fromMaterial(this.material);
//        if (tool != null) {
//            int damage = (int) tool.legacyAttackDamage;
//            if (damage > 0) {
//                builder.lore(Component.text()
//                        .decoration(TextDecoration.ITALIC, false)
//                        .append(Component.text("Deals ", NamedTextColor.GRAY))
//                        .append(Component.text("❤".repeat(damage), NamedTextColor.RED))
//                        .append(Component.text(" (" + damage + ")", NamedTextColor.GRAY))
//                        .build());
//            } else {
//                builder.lore(Component.text("Deals no damage", NamedTextColor.GRAY));
//            }
//
//            builder.meta(b -> b.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES));
//        }

        return builder.build();
    }

    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getWeight() {
        return weight;
    }
}
