package io.github.ricardodlm.springai.mcp.transport.session.model;

import java.time.Instant;

/**
 * Sesión de transporte Local (stdio).
 */
public record StdioProtocolSession(
        String sessionId,
        String clientId,
        String jwt,
        Long processId,       // específico Local — PID del proceso
        Instant connectedAt,
        Instant lastActivityAt,
        SessionStatus status
) implements ProtocolSession {

    /**
     * Crea una nueva sesión Local activa.
     */
    public static StdioProtocolSession create(
            String sessionId,
            String clientId,
            String jwt,
            Long processId){

        Instant now = Instant.now();
        return new StdioProtocolSession(
                sessionId,
                clientId,
                jwt,
                processId,
                now,
                now,
                SessionStatus.ACTIVE
        );
    }

    @Override
    public TransportType transportType() {
        return TransportType.STDIO;
    }

    /**
     * Renueva la actividad de la sesión.
     */
    @Override
    public ProtocolSession renewActivity() {
        return new StdioProtocolSession(
                sessionId,
                clientId,
                jwt,
                processId,
                connectedAt,
                Instant.now(),
                status
        );
    }

    /**
     * Termina la sesión.
     */
    @Override
    public ProtocolSession terminate() {
        return new StdioProtocolSession(
                sessionId,
                clientId,
                jwt,
                processId,
                connectedAt,
                Instant.now(),
                SessionStatus.TERMINATED
        );
    }


}
