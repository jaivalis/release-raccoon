package com.raccoon.notify;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class NotifyScheduler {

    final NotifyService service;

    @Inject
    public NotifyScheduler(NotifyService service) {
        this.service = service;
    }

    @Scheduled(cron="{notify.cron.expr}")
    public void notifyCronJob() {
        log.info("Notifying cronjob triggered");
        service.notifyUsers().await().indefinitely();
    }

}
