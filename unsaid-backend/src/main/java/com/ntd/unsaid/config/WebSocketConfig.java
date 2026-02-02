package com.ntd.unsaid.config;

import com.ntd.unsaid.security.CustomJwtDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomJwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/unsaid")
                .setAllowedOriginPatterns("http://localhost:5173")
                .withSockJS();
    }

    // Cấu hình Interceptor để xác thực Token từ STOMP Header
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor != null ? accessor.getCommand() : null)) {
                    // Lấy token từ header "Authorization" của gói tin STOMP CONNECT
                    List<String> authorization = accessor.getNativeHeader("Authorization");

                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.getFirst().substring(7); // Bỏ chữ "Bearer "
                        try {
                            // Decode và Validate Token thủ công
                            Jwt jwt = jwtDecoder.decode(token);

                            // Convert JWT thành Authentication Object
                            // UsernamePasswordAuthenticationToken authentication =
                            // (UsernamePasswordAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

                            var authentication = jwtAuthenticationConverter.convert(jwt);
                            accessor.setUser(authentication);

                            // Set User cho phiên WebSocket này
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                        } catch (JwtException e) {
                            log.error("WebSocket Authentication failed: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}