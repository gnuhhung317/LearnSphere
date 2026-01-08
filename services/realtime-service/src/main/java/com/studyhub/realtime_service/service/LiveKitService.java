package com.studyhub.realtime_service.service;

import com.studyhub.realtime_service.config.LiveKitConfig;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LiveKitService {

    private final LiveKitConfig liveKitConfig;

    public String createAccessToken(String roomName, String participantIdentity, String participantName) {
        AccessToken token = new AccessToken(liveKitConfig.getApiKey(), liveKitConfig.getApiSecret());
        token.setName(participantName);
        token.setIdentity(participantIdentity);
        token.addGrants(new RoomJoin(true), new RoomName(roomName));
        return token.toJwt();
    }
}
