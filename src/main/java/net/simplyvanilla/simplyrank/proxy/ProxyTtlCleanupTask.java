package net.simplyvanilla.simplyrank.proxy;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.function.Consumer;

public class ProxyTtlCleanupTask implements Consumer<ScheduledTask> {
    private final ProxyService proxyService;
    private final int minutes;

    public ProxyTtlCleanupTask(ProxyService proxyService, int minutes) {
        this.proxyService = proxyService;
        this.minutes = minutes;
    }

    @Override
    public void accept(ScheduledTask scheduledTask) {
        this.proxyService.deleteExpiredEntries(this.minutes);
    }
}
