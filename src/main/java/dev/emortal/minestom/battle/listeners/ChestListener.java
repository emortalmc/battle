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
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ChestListener {

    private static TaskSchedule CHEST_REFILL_INTERVAL = TaskSchedule.seconds(80);

    public static void registerListener(EventNode<InstanceEvent> eventNode, BattleGame game) {
        Set<Point> chests = new HashSet<>();

        // Chest periodic refill task
        game.instance.scheduler().buildTask(() -> {
            for (Point chestPos : chests) {
                ChestHandler handler = (ChestHandler) game.instance.getBlock(chestPos).handler();
                handler.refillInventory();
            }

            doChestAnimation(game.getAudience(), game.instance.scheduler());
        }).delay(CHEST_REFILL_INTERVAL).repeat(CHEST_REFILL_INTERVAL).schedule();
//        }).delay(TaskSchedule.seconds(5)).repeat(CHEST_REFILL_INTERVAL).schedule();

        eventNode.addListener(PlayerBlockInteractEvent.class, e -> {
            if (e.getBlock().compare(Block.CHEST)) {
                BlockHandler handler = e.getBlock().handler();
                if (!(handler instanceof ChestHandler)) {
                    ChestHandler newHandler = new ChestHandler();
                    e.getInstance().setBlock(e.getBlockPosition(), e.getBlock().withHandler(newHandler));
                    handler = newHandler;

                    chests.add(e.getBlockPosition());
                }

                e.getPlayer().openInventory(((ChestHandler) handler).getInventory());
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
