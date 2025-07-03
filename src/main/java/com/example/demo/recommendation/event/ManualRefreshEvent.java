package com.example.demo.recommendation.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
public class ManualRefreshEvent extends ApplicationEvent {
    private final String triggerBy;
    private final LocalDateTime triggeredAt;

    public ManualRefreshEvent(Object source, String triggerBy) {
        super(source);
        this.triggerBy = triggerBy;
        this.triggeredAt = LocalDateTime.now();
    }
}
