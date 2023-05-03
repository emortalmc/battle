package dev.emortal.minestom.battle.chest;

import io.github.bloepiloepi.pvp.enums.Tool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.function.Consumer;

public class Item {

    private Material material;
    private int weight;
    private Consumer<ItemStack.Builder> itemCreate = (a) -> {};
    private ItemStack itemStack;

    public Item(Material material, int weight, Consumer<ItemStack.Builder> itemCreate) {
        this.material = material;
        this.weight = weight;
        this.itemCreate = itemCreate;
        this.itemStack = createItemStack();
    }
    public Item(Material material, int weight) {
        this.material = material;
        this.weight = weight;
        this.itemStack = createItemStack();
    }

    private ItemStack createItemStack() {
        ItemStack.Builder builder = ItemStack.builder(material);
        itemCreate.accept(builder);

        Tool tool = Tool.fromMaterial(material);
        if (tool != null) {
            int damage = (int) tool.legacyAttackDamage;
            if (damage > 0) {
                builder.lore(
                        Component.text()
                                .append(Component.text("Deals ", NamedTextColor.GRAY))
                                .append(Component.text("\u2764".repeat(damage), NamedTextColor.RED))
                                .append(Component.text(" (" + damage + ")", NamedTextColor.GRAY))
                                .build()
                                .decoration(TextDecoration.ITALIC, false)
                );
            } else {
                builder.lore(
                        Component.text("Deals no damage", NamedTextColor.GRAY)
                );
            }

            builder.meta((a) -> a.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES));
        }

        return builder.build();
    }

//    public Material getMaterial() {
//        return material;
//    }

    public int getWeight() {
        return weight;
    }

    public Consumer<ItemStack.Builder> getItemCreate() {
        return itemCreate;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
