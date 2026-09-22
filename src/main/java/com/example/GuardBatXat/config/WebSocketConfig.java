package com.example.GuardBatXat.config;

import com.example.GuardBatXat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() == null) return message;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    boolean authenticated = false;
                    boolean demo = false;
                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        String token = authorization.substring(7);
                        try {
                            jwtService.extractUsername(token);
                            authenticated = true;
                            demo = jwtService.isDemoToken(token);
                        } catch (Exception ignored) {
                            authenticated = false;
                        }
                    }
                    if (accessor.getSessionAttributes() != null) {
                        accessor.getSessionAttributes().put("riskAuthenticated", authenticated);
                        accessor.getSessionAttributes().put("riskDemo", demo);
                    }
                }
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/topic/citizen/")) {
                        // Kenh demo chi phat snapshot da gan nhan MO PHONG va khong
                        // cho phep client ghi du lieu, nen moi man hinh Citizen co
                        // the theo doi truc tiep ma khong can phien QR rieng.
                        if (destination.equals("/topic/citizen/demo-updates")) {
                            return message;
                        }
                        Object authenticated = accessor.getSessionAttributes() == null
                                ? null : accessor.getSessionAttributes().get("riskAuthenticated");
                        if (!Boolean.TRUE.equals(authenticated)) {
                            throw new IllegalArgumentException("Citizen risk topic yêu cầu JWT");
                        }
                    }
                }
                return message;
            }
        });
    }
}
