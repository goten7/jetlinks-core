package org.jetlinks.core.message.codec.http.websocket;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

@AllArgsConstructor(staticName = "of")
public class WebSocketSessionMessageWrapper implements WebSocketSessionMessage {

    private final WebSocketMessage message;

    private final WebSocketSession session;

    @Override
    public WebSocketSession getWebSocketSession() {
        return session;
    }

    @Override
    public Type getType() {
        return message.getType();
    }

    @Nonnull
    @Override
    public ByteBuf getPayload() {
        return message.getPayload();
    }

    @Override
    public String toString() {
        return session + "\n" + message.print();
    }
}
