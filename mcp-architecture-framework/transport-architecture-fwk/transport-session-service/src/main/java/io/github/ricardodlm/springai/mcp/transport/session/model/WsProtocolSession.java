package io.github.ricardodlm.springai.mcp.transport.session.model;

import java.time.Instant;

public record WsProtocolSession(
        String sessionId,
        String clientId,
        String jwt,
        String wsSessionId,     // específico WebSocket — id sesión nativa
        Instant connectedAt,
        Instant lastRequestAt,
        Instant lastActivityAt,
        SessionStatus status
) implements ProtocolSession {

    /**
     * Crea una nueva sesión WebSocket activa.
     */
    public static WsProtocolSession create(
            String sessionId,
            String clienteId,
            String jwt,
            String wsSessionId){
        Instant now = Instant.now();
        return new WsProtocolSession(
                sessionId,
                clienteId,
                jwt,
                wsSessionId,
                now,
                now,
                now,
                SessionStatus.ACTIVE
        );
    }

    @Override
    public TransportType transportType() {
        return TransportType.WEBSOCKET;
    }

    /**
     * Renueva la actividad de la sesión.
     */
    @Override
    public ProtocolSession renewActivity() {
        return new WsProtocolSession(
                sessionId,
                clientId,
                jwt,
                wsSessionId,
                connectedAt,
                Instant.now(),
                Instant.now(),
                status
        );
    }


    /**
     * Termina la sesión.
     */
    @Override
    public ProtocolSession terminate() {
        return new WsProtocolSession(
                sessionId,
                clientId,
                jwt,
                wsSessionId,
                connectedAt,
                Instant.now(),
                Instant.now(),
                SessionStatus.TERMINATED
        );
    }
}
