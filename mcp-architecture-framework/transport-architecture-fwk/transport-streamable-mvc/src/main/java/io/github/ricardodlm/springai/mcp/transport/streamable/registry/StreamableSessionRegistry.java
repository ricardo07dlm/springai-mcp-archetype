package io.github.ricardodlm.springai.mcp.transport.streamable.registry;

import io.github.ricardodlm.springai.mcp.transport.session.model.StreamableProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Registro de sesiones Streamable HTTP activas.
 * Persiste exclusivamente en Cafeína via SessionRepository.
 */
public final class StreamableSessionRegistry {

    private static final Logger log =
            LoggerFactory.getLogger(StreamableSessionRegistry.class);

    private final SessionRepository repository;
    private final int maxSessions;

    public StreamableSessionRegistry(SessionRepository repository, int maxSessions) {
        this.repository = repository;
        this.maxSessions = maxSessions;
    }

    // ================================================
    // REGISTRAR
    // ================================================

    /**
     * Registra sesión en Cafeína.
     */
    public boolean register(StreamableProtocolSession session) {
        if (isOverloaded()) {
            log.warn("STREAMABLE MAX SESSIONS reached max={}",
                    maxSessions);
            return false;
        }
        repository.save(session);

        log.debug("STREAMABLE REGISTER sessionId={}...",
                session.sessionId().substring(0, 8));

        return true;
    }

    // ================================================
    // ELIMINAR
    // ================================================

    /**
     * Elimina sesión de Cafeína.
     */
    public void unregister(String sessionId) {
        repository.delete(sessionId);

        log.debug("STREAMABLE UNREGISTER sessionId={}...",
                sessionId.substring(0, 8));
    }

    // ================================================
    // CONSULTAR
    // ================================================

    /**
     * Busca sesión en Cafeína.
     */
    public StreamableProtocolSession findById(String sessionId) {
        return repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> (StreamableProtocolSession) s)
                .orElse(null);
    }

    /**
     * Actualiza actividad en Cafeína — renueva TTL.
     */
    public void updateActivity(String sessionId) {
        repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> (StreamableProtocolSession) s)
                .map(StreamableProtocolSession::renewActivity)
                .ifPresent(repository::save);
    }

    // ================================================
    // ACTUALIZAR JWT
    // ================================================

    /**
     * Actualiza el JWT de una sesión existente en Cafeína.
     */
    public void updateJwt(String sessionId, String newJwt) {
        repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> (StreamableProtocolSession) s)
                .map(s -> s.withJwt(newJwt))
                .ifPresent(repository::save);

        log.debug("STREAMABLE JWT UPDATED sessionId={}...",
                sessionId.substring(0, 8));
    }

    /**
     * Recupera el JWT de una sesión existente en Cafeína.
     */
    public String getJwt(String sessionId) {
        return repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> (StreamableProtocolSession) s)
                .map(StreamableProtocolSession::jwt)
                .orElse(null);
    }

    public void renewActivityAndJwt(String sessionId, String newJwt) {
        repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> (StreamableProtocolSession) s)
                .map(s -> s.renewActivityAndJwt(newJwt))
                .ifPresent(repository::save);

        log.debug("STREAMABLE ACTIVITY+JWT RENEWED sessionId={}...",
                sessionId.substring(0, 8));
    }

    /**
     * Verifica si una sesión existe en Cafeína.
     */
    public boolean exists(String sessionId) {
        return repository.exists(sessionId);
    }

    /**
     * Verifica si el servidor está saturado.
     */
    public boolean isOverloaded() {
        return repository.count() >= maxSessions;
    }

    /**
     * Número de sesiones activas en Cafeína.
     */
    public int activeCount() {
        return repository.count();
    }

}
