package com.ntd.unsaid.infrastructure.messaging.worker;

import com.ntd.unsaid.domain.event.ActionMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActionWorker {

    SimpMessagingTemplate messagingTemplate;

    public void handle(ActionMessage message) {


    }
}

