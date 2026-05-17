package com.portal.job.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
