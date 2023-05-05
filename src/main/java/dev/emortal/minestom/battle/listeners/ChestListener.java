package dev.emortal.minestom.battle.listeners;

import dev.emortal.minestom.battle.BattleGame;
import dev.emortal.minestom.battle.chest.ChestHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.antlr.v4.runtime.misc.Array2DHashSet;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

public class ChestListener {

    private static TaskSchedule CHEST_REFILL_INTERVAL = TaskSchedule.seconds(80);

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        Set<Point> chests = new HashSet<>();
        Set<Point> unopenedChests = new HashSet<>();
        Map<UUID, Point> playerChestMap = new HashMap<>();

        // Chest periodic refill task
        game.instance.scheduler().buildTask(() -> {
            for (Point chestPos : chests) {
                ChestHandler handler = (ChestHandler) game.instance.getBlock(chestPos).handler();
                handler.refillInventory();
            }

            unopenedChests.clear();
            unopenedChests.addAll(chests);

            doChestAnimation(game.getAudience(), game.instance.scheduler());
        }).delay(CHEST_REFILL_INTERVAL).repeat(CHEST_REFILL_INTERVAL).schedule();
//        }).delay(TaskSchedule.seconds(5)).repeat(CHEST_REFILL_INTERVAL).schedule();

        game.instance.scheduler().buildTask(() -> {
            for (Point chest : unopenedChests) {

            }
        });

        eventNode.addListener(PlayerBlockInteractEvent.class, e -> {
            if (e.getBlock().compare(Block.CHEST)) {
                BlockHandler handler = e.getBlock().handler();
                if (!(handler instanceof ChestHandler)) {
                    ChestHandler newHandler = new ChestHandler();
                    e.getInstance().setBlock(e.getBlockPosition(), e.getBlock().withHandler(newHandler));
                    handler = newHandler;

                    chests.add(e.getBlockPosition());
                }

                ChestHandler chestHandler = (ChestHandler) handler;
                final int playersInside = chestHandler.playersInside.incrementAndGet();
                e.getInstance().sendGroupedPacket(new BlockActionPacket(e.getBlockPosition(), (byte) 1, (byte) playersInside, Block.CHEST));

                e.getPlayer().openInventory(chestHandler.getInventory());

                unopenedChests.remove(e.getBlockPosition());

                playerChestMap.put(e.getPlayer().getUuid(), e.getBlockPosition());

                if (playersInside == 1) {
                    game.getAudience().playSound(Sound.sound(SoundEvent.BLOCK_CHEST_OPEN, Sound.Source.BLOCK, 1f, 1f), e.getBlockPosition().x(), e.getBlockPosition().y(), e.getBlockPosition().z());
                }
            }
        });

        eventNode.addListener(InventoryCloseEvent.class, e -> {
            Point openChestPos = playerChestMap.get(e.getPlayer().getUuid());
            if (openChestPos == null) return;

            ChestHandler handler = (ChestHandler) e.getInstance().getBlock(openChestPos).handler();
            final int playersInside = handler.playersInside.decrementAndGet();

            e.getInstance().sendGroupedPacket(new BlockActionPacket(openChestPos, (byte) 1, (byte) playersInside, Block.CHEST));

            if (playersInside == 0) {
                game.getAudience().playSound(Sound.sound(SoundEvent.BLOCK_CHEST_CLOSE, Sound.Source.BLOCK, 1f, 1f), openChestPos.x(), openChestPos.y(), openChestPos.z());
            }
        });
    }



    private static void doChestAnimation(Audience audience, Scheduler scheduler) {

        audience.playSound(Sound.sound(Key.key("battle.refill.open"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

        scheduler.submitTask(new Supplier<>() {
            final Component[] frames = new Component[] {Component.text('\uE00E'), Component.text('\uE00F'), Component.text('\uE010')};
            int frame = 0;

            @Override
            public TaskSchedule get() {
                if (frame >= frames.length) {
                    scheduler.submitTask(new Supplier<>() {
                        boolean firstTick = true;
                        boolean playedSound = false;

                        @Override
                        public TaskSchedule get() {
                            if (firstTick) {
                                firstTick = false;
                                return TaskSchedule.seconds(1);
                            }
                            if (!playedSound) {
                                playedSound = true;
                                audience.playSound(Sound.sound(Key.key("battle.refill.close"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
                            }

                            frame--;

                            audience.showTitle(
                                    Title.title(
                                            frames[frame],
                                            Component.empty(),
                                            Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(200))
                                    )
                            );

                            if (frame == 0) return TaskSchedule.stop();

                            return TaskSchedule.tick(2);
                        }
                    });

                    return TaskSchedule.stop();
                }



                audience.showTitle(
                        Title.title(
                                frames[frame],
                                Component.empty(),
                                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(200))
                        )
                );

                frame++;

                return TaskSchedule.tick(2);
            }
        });
    }

}
