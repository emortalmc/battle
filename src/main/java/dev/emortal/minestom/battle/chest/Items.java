package dev.emortal.minestom.battle.chest;

import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class Items {

    private static int common = 40;
    private static int lesscommon = 35;
    private static int uncommon = 30;
    private static int rare = 24;
    private static int veryrare = 14;
    private static int epic = 7;
    private static int legendary = 1;


    private static ThreadLocalRandom random = ThreadLocalRandom.current();

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
            new Item(Material.TIPPED_ARROW, rare, (it) -> {
                it.meta(PotionMeta.class, (meta) -> {
                    meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.POISON.id(), (byte) 0, 5 * 20, false, true, true)));
                    meta.potionType(PotionType.POISON);
                });
            }),

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
            new Item(Material.POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> {
                meta.potionType(PotionType.INVISIBILITY);
                meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.INVISIBILITY.id(), (byte) 0, 20 * 20, false, true, true)));
            })),
            new Item(Material.POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> {
                meta.potionType(PotionType.STRENGTH);
                meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.STRENGTH.id(), (byte) 0, 20 * 20, false, true, true)));
            })),
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
            new Item(Material.SPLASH_POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> {
                meta.potionType(PotionType.POISON);
                meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.POISON.id(), (byte) 0, 17 * 20, false, true, true)));
            })),
            new Item(Material.SPLASH_POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> {
                meta.potionType(PotionType.SLOWNESS);
                meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.SLOWNESS.id(), (byte) 0, 40 * 20, false, true, true)));
            })),
            new Item(Material.SPLASH_POTION, rare, (it) -> it.meta(PotionMeta.class, (meta) -> {
                meta.potionType(PotionType.WEAKNESS);
                meta.effects(List.of(new CustomPotionEffect((byte) PotionEffect.WEAKNESS.id(), (byte) 0, 45 * 20, false, true, true)));
            })),

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
