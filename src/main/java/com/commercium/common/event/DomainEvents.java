package com.commercium.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEvents {

    private static ApplicationEventPublisher publisher;

    public DomainEvents(ApplicationEventPublisher publisher) {
        DomainEvents.publisher = publisher;
    }

    public static void raise(DomainEvent event) {
        if (publisher != null) {
            publisher.publishEvent(event);
        }
    }
}

