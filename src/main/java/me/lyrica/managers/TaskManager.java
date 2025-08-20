package me.lyrica.managers;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.utils.IMinecraft;

import java.util.ArrayList;

public class TaskManager implements IMinecraft {
    private final ArrayList<Runnable> tasks = new ArrayList<>();

    public TaskManager() {
        Lyrica.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!tasks.isEmpty()) {
            tasks.getFirst().run();
            tasks.removeFirst();
        }
    }

    public void submit(Runnable runnable) {
        tasks.add(runnable);
    }
}