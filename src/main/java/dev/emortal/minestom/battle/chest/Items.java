package dev.emortal.minestom.battle.chest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class Items {

    private static final int COMMON = 40;
    private static final int LESS_COMMON = 35;
    private static final int UNCOMMON = 30;
    private static final int RARE = 24;
    private static final int VERY_RARE = 14;
    private static final int EPIC = 7;
    private static final int LEGENDARY = 1;

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private static final @NotNull List<Item> ALL = List.of(
            // Consumables
            new Item(Material.APPLE, COMMON),
            new Item(Material.APPLE, COMMON),
            new Item(Material.COOKED_BEEF, COMMON),
            new Item(Material.COOKED_PORKCHOP, COMMON),
            new Item(Material.COOKED_PORKCHOP, COMMON),
            new Item(Material.GOLDEN_APPLE, VERY_RARE),

            // Weapons
            new Item(Material.BOW, UNCOMMON),
            new Item(Material.ARROW, LESS_COMMON, builder -> builder.amount(RANDOM.nextInt(2, 6))),
            new Item(Material.TIPPED_ARROW, RARE, potionHelper(PotionEffect.POISON, 5)),

            // Swords
            new Item(Material.WOODEN_SWORD, UNCOMMON),
            new Item(Material.GOLDEN_SWORD, UNCOMMON),
            new Item(Material.STONE_SWORD, VERY_RARE),
            new Item(Material.IRON_SWORD, LEGENDARY),
//            new Item(Material.DIAMOND_SWORD, LEGENDARY, builder -> builder.meta(meta -> meta.damage(781))), // half durability

            // Axes
            new Item(Material.WOODEN_AXE, RARE),
            new Item(Material.GOLDEN_AXE, VERY_RARE),
            new Item(Material.STONE_AXE, LEGENDARY),
            //new Item(Material.IRON_AXE, EPIC),
            //new Item(Material.DIAMOND_AXE, LEGENDARY),

            // Pickaxes
            new Item(Material.WOODEN_PICKAXE, LESS_COMMON),
            new Item(Material.GOLDEN_PICKAXE, LESS_COMMON),
            new Item(Material.STONE_PICKAXE, UNCOMMON),
            new Item(Material.IRON_PICKAXE, RARE),
            //new Item(Material.DIAMOND_PICKAXE, VERY_RARE),

            // Shovels
            new Item(Material.WOODEN_SHOVEL, LESS_COMMON),
            new Item(Material.GOLDEN_SHOVEL, LESS_COMMON),
            new Item(Material.STONE_SHOVEL, UNCOMMON),
            new Item(Material.IRON_SHOVEL, RARE),
            //new Item(Material.DIAMOND_SHOVEL, VERY_RARE),

            // Misc
            new Item(Material.WOODEN_HOE, RARE, (it) -> it.meta((meta) -> meta.enchantment(Enchantment.FIRE_ASPECT, (short) 1))),
            //new Item(Material.SHEARS, LESS_COMMON),
            new Item(Material.STICK, UNCOMMON, (it) -> it.meta((meta) -> meta.enchantment(Enchantment.KNOCKBACK, (short) 1))),
//            new Item(Material.FISHING_ROD, RARE, builder -> {
//                builder.amount(RANDOM.nextInt(1, 3));
//                builder.meta(meta -> {
//                    meta.canPlaceOn(Set.copyOf(Block.values()));
//                    meta.hideFlag(ItemHideFlag.HIDE_PLACED_ON);
//                });
//            }),

            // Potions
            //new Item(Material.TOTEM_OF_UNDYING, LEGENDARY),
            new Item(Material.POTION, RARE, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.STRONG_HEALING))),
            new Item(Material.POTION, RARE, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.FIRE_RESISTANCE))),
            new Item(Material.POTION, RARE, potionHelper(PotionEffect.INVISIBILITY, 20)),
            new Item(Material.POTION, RARE, potionHelper(PotionEffect.STRENGTH, 20)),
            new Item(Material.POTION, RARE, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.REGENERATION))),
            new Item(Material.SPLASH_POTION, RARE, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.STRONG_HARMING))),
//            new Item(Material.LINGERING_POTION, RARE, builder -> builder.meta(PotionMeta.class, meta -> meta.potionType(PotionType.REGENERATION))),
//            new Item(Material.LINGERING_POTION, RARE, builder -> builder.meta(PotionMeta.class, meta -> meta.potionType(PotionType.STRONG_HARMING))),
            new Item(Material.SPLASH_POTION, RARE, potionHelper(PotionEffect.POISON, 16)),
            new Item(Material.SPLASH_POTION, RARE, potionHelper(PotionEffect.SLOWNESS, 40)),
            new Item(Material.SPLASH_POTION, RARE, potionHelper(PotionEffect.WEAKNESS, 45)),

            // Armour
            // Helmets
            //new Item(Material.LEATHER_HELMET, lesscommon),
            new Item(Material.GOLDEN_HELMET, UNCOMMON),
            new Item(Material.CHAINMAIL_HELMET, RARE),
            new Item(Material.IRON_HELMET, VERY_RARE),
            new Item(Material.DIAMOND_HELMET, LEGENDARY),

            // Chestplates
            //new Item(Material.LEATHER_CHESTPLATE, lesscommon),
            new Item(Material.GOLDEN_CHESTPLATE, UNCOMMON),
            new Item(Material.CHAINMAIL_CHESTPLATE, RARE),
            new Item(Material.IRON_CHESTPLATE, VERY_RARE),
            new Item(Material.DIAMOND_CHESTPLATE, LEGENDARY),

            // Leggings
            //new Item(Material.LEATHER_LEGGINGS, lesscommon),
            new Item(Material.GOLDEN_LEGGINGS, UNCOMMON),
            new Item(Material.CHAINMAIL_LEGGINGS, RARE),
            new Item(Material.IRON_LEGGINGS, VERY_RARE),
            new Item(Material.DIAMOND_LEGGINGS, LEGENDARY),

            // Boots
            //new Item(Material.LEATHER_BOOTS, lesscommon),
            new Item(Material.GOLDEN_BOOTS, UNCOMMON),
            new Item(Material.CHAINMAIL_BOOTS, RARE),
            new Item(Material.IRON_BOOTS, VERY_RARE),
            new Item(Material.DIAMOND_BOOTS, LEGENDARY)
    );

    private Items() {
    }

    public static @NotNull ItemStack random() {
        int totalWeight = 0;
        for (Item item : ALL) {
            totalWeight += item.getWeight();
        }

        int currentIndex = 0;
        int randomIndex = ThreadLocalRandom.current().nextInt(totalWeight + 1);

        while (currentIndex < ALL.size() - 1) {
            randomIndex -= ALL.get(currentIndex).getWeight();
            if (randomIndex <= 0.0) break;
            ++currentIndex;
        }

        return ALL.get(currentIndex).getItemStack();
    }

    private static @NotNull Consumer<ItemStack.Builder> potionHelper(@NotNull PotionEffect potionEffect, int secondsDuration) {
        String potionName = titleCase(potionEffect.namespace().value().replace('_', ' '));
        Component potionDisplayName = Component.text("Potion of " + potionName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);

        return builder -> builder.meta(PotionMeta.class, meta -> {
            meta.potionType(PotionType.WATER);
            meta.displayName(potionDisplayName);
            meta.effects(List.of(new CustomPotionEffect((byte) potionEffect.id(), (byte) 0, secondsDuration * 20, false, true, true)));
        });
    }

    private static @NotNull String titleCase(@NotNull String input) { // fire resistance -> Fire Resistance
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            chars[0] = Character.toUpperCase(chars[0]);
            if (chars[i] == ' ') {
                chars[i + 1] = Character.toUpperCase(chars[i + 1]);
            }
        }

        return String.valueOf(chars);
    }
}
