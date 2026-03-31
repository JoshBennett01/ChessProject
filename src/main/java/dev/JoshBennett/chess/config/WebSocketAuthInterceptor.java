package dev.JoshBennett.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            
            if (destination != null && destination.startsWith("/topic/")) {
                logger.warn("Blocked client from sending directly to topic: {}", destination);
                throw new IllegalStateException("Clients cannot send messages directly to /topic. Use /app endpoints.");
            }

            if (destination != null && !destination.startsWith("/app/")) {
                logger.warn("Blocked message to invalid destination: {}", destination);
                throw new IllegalStateException("Invalid destination. Use /app endpoints.");
            }
        }

        return message;
    }
}
