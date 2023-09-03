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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Items {

    private static final int common = 40;
    private static final int lesscommon = 35;
    private static final int uncommon = 30;
    private static final int rare = 24;
    private static final int veryrare = 14;
    private static final int epic = 7;
    private static final int legendary = 1;


    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static ItemStack randomItem() {
        int totalWeight = 0;
        for (Item item : items) {
            totalWeight += item.getWeight();
        }

        int idx = 0;
        int r = ThreadLocalRandom.current().nextInt(totalWeight + 1);
        while (idx < items.length - 1) {
            r -= items[idx].getWeight();
            if (r <= 0.0) break;
            ++idx;
        }

        return items[idx].getItemStack();
    }

    public static Consumer<ItemStack.Builder> potionHelper(PotionEffect potionEffect, int secondsDuration) {
        return builder -> builder.meta(PotionMeta.class, (meta) -> {
            meta.potionType(PotionType.WATER);
            meta.displayName(Component.text("Potion of " + titleCase(potionEffect.namespace().value().replace('_', ' ')), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            meta.effects(List.of(new CustomPotionEffect((byte) potionEffect.id(), (byte) 0, secondsDuration * 20, false, true, true)));
        });
    }
    private static String titleCase(String myinput) { // fire resistance -> Fire Resistance
        char[] charAray = myinput.toCharArray();
        for(int i = 0; i < charAray.length; i++) {
            charAray[0] = Character.toUpperCase(charAray[0]);
            if(charAray[i] == ' ') {
                charAray[i+1] = Character.toUpperCase(charAray[i+1]);
            }
        }
        return String.valueOf(charAray);
    }


    public static Item[] items = new Item[]{
            // Consumables
            new Item(Material.APPLE, common),
            new Item(Material.APPLE, common),
            new Item(Material.COOKED_BEEF, common),
            new Item(Material.COOKED_PORKCHOP, common),
            new Item(Material.COOKED_PORKCHOP, common),
            new Item(Material.GOLDEN_APPLE, veryrare),

            // Weapons
            new Item(Material.BOW, uncommon),
            new Item(Material.ARROW, lesscommon, (it) -> it.amount(random.nextInt(2, 6))),
            new Item(Material.TIPPED_ARROW, rare, potionHelper(PotionEffect.POISON, 5)),

            // Swords
            new Item(Material.WOODEN_SWORD, uncommon),
            new Item(Material.GOLDEN_SWORD, uncommon),
            new Item(Material.STONE_SWORD, veryrare),
            new Item(Material.IRON_SWORD, legendary),
            //new Item(Material.DIAMOND_SWORD, legendary) { it.meta { it.damage(781) } }, // half durability

            // Axes
            new Item(Material.WOODEN_AXE, rare),
            new Item(Material.GOLDEN_AXE, veryrare),
            new Item(Material.STONE_AXE, legendary),
            //new Item(Material.IRON_AXE, epic),
            //new Item(Material.DIAMOND_AXE, legendary),

            // Pickaxes
            new Item(Material.WOODEN_PICKAXE, lesscommon),
            new Item(Material.GOLDEN_PICKAXE, lesscommon),
            new Item(Material.STONE_PICKAXE, uncommon),
            new Item(Material.IRON_PICKAXE, rare),
            //new Item(Material.DIAMOND_PICKAXE, veryrare),

            // Shovels
            new Item(Material.WOODEN_SHOVEL, lesscommon),
            new Item(Material.GOLDEN_SHOVEL, lesscommon),
            new Item(Material.STONE_SHOVEL, uncommon),
            new Item(Material.IRON_SHOVEL, rare),
            //new Item(Material.DIAMOND_SHOVEL, veryrare),

            // Misc
            new Item(Material.WOODEN_HOE, rare, (it) -> it.meta((meta) -> meta.enchantment(Enchantment.FIRE_ASPECT, (short) 1))),
            //new Item(Material.SHEARS, lesscommon),
            new Item(Material.STICK, uncommon, (it) -> it.meta((meta) -> meta.enchantment(Enchantment.KNOCKBACK, (short) 1))),
            //new Item(Material.FISHING_ROD, rare),
            //        new Item(Material.TNT, common) { // TODO: Re change to uncommon
            //            it.amount(random.nextInt(1, 3))
            //            it.meta {
            //                it.canPlaceOn(Block.values().toMutableSet())
            ////                it.hideFlag(ItemHideFlag.HIDE_PLACED_ON)
            //            }
            //        },

            // Potions
            //new Item(Material.TOTEM_OF_UNDYING, legendary),
            new Item(Material.POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.STRONG_HEALING))),
            new Item(Material.POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.FIRE_RESISTANCE))),
            new Item(Material.POTION, rare, potionHelper(PotionEffect.INVISIBILITY, 20)),
            new Item(Material.POTION, rare, potionHelper(PotionEffect.STRENGTH, 20)),
            new Item(Material.POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.REGENERATION))),
            new Item(Material.SPLASH_POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> meta.potionType(PotionType.STRONG_HARMING))),
            //        new Item(Material.LINGERING_POTION, rare) {
            //            it.meta(PotionMeta::class.java) {
            //                it.potionType(PotionType.REGENERATION)
            //            }
            //        },
            //        new Item(Material.LINGERING_POTION, rare) {
            //            it.meta(PotionMeta::class.java) {
            //                it.potionType(PotionType.STRONG_HARMING)
            //            }
            //        },
            new Item(Material.SPLASH_POTION, rare, potionHelper(PotionEffect.POISON, 16)),
            new Item(Material.SPLASH_POTION, rare, potionHelper(PotionEffect.SLOWNESS, 40)),
            new Item(Material.SPLASH_POTION, rare, potionHelper(PotionEffect.WEAKNESS, 45)),

            // Armour
            // Helmets
            //new Item(Material.LEATHER_HELMET, lesscommon),
            new Item(Material.GOLDEN_HELMET, uncommon),
            new Item(Material.CHAINMAIL_HELMET, rare),
            new Item(Material.IRON_HELMET, veryrare),
            new Item(Material.DIAMOND_HELMET, legendary),

            // Chestplates
            //new Item(Material.LEATHER_CHESTPLATE, lesscommon),
            new Item(Material.GOLDEN_CHESTPLATE, uncommon),
            new Item(Material.CHAINMAIL_CHESTPLATE, rare),
            new Item(Material.IRON_CHESTPLATE, veryrare),
            new Item(Material.DIAMOND_CHESTPLATE, legendary),

            // Leggings
            //new Item(Material.LEATHER_LEGGINGS, lesscommon),
            new Item(Material.GOLDEN_LEGGINGS, uncommon),
            new Item(Material.CHAINMAIL_LEGGINGS, rare),
            new Item(Material.IRON_LEGGINGS, veryrare),
            new Item(Material.DIAMOND_LEGGINGS, legendary),

            // Boots
            //new Item(Material.LEATHER_BOOTS, lesscommon),
            new Item(Material.GOLDEN_BOOTS, uncommon),
            new Item(Material.CHAINMAIL_BOOTS, rare),
            new Item(Material.IRON_BOOTS, veryrare),
            new Item(Material.DIAMOND_BOOTS, legendary)
    };

}
