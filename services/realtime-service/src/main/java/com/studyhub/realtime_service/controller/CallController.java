package com.studyhub.realtime_service.controller;

import com.studyhub.realtime_service.dto.CallEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/call/start")
    public void startCall(@DestinationVariable String roomId, @Payload CallEvent event) {
        log.info("Call started in room {} by {}", roomId, event.getParticipantId());
        event.setType(CallEvent.Type.START);
        event.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/call", event);
    }

    @MessageMapping("/room/{roomId}/call/end")
    public void endCall(@DestinationVariable String roomId, @Payload CallEvent event) {
        log.info("Call ended in room {} by {}", roomId, event.getParticipantId());
        event.setType(CallEvent.Type.END);
        event.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/call", event);
    }

    @MessageMapping("/room/{roomId}/call/join")
    public void joinCall(@DestinationVariable String roomId, @Payload CallEvent event) {
        log.info("User {} joining call in room {}", event.getParticipantId(), roomId);
        event.setType(CallEvent.Type.JOIN);
        event.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/call", event);
    }
}
