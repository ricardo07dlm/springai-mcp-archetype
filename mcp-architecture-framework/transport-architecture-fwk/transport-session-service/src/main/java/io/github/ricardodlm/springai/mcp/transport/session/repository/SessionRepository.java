package io.github.ricardodlm.springai.mcp.transport.session.repository;

import io.github.ricardodlm.springai.mcp.adapters.cache.manager.CacheServiceManager;
import io.github.ricardodlm.springai.mcp.transport.session.model.ProtocolSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Repositorio de sesiones de transporte MCP.
 *
 * Persiste sesiones en Cafeína via cache-service.jar.
 * Agnóstico del tipo de sesión — soporta SSE, WebSocket y Local.
 *
 * * Clave de cache: "transport:session:{sessionId}"
 *  * TTL: configurable via sca.transport.session.ttl
 *  *      gestionado por CacheServiceManager de cache-service.jar
*/
public final class SessionRepository {

    private static final Logger log =
            LoggerFactory.getLogger(SessionRepository.class);

    private static final String CACHE_NAME = "transport-sessions";
    private static final String KEY_PREFIX = "trasport:session";

    private final CacheServiceManager cacheServiceManager;

    public SessionRepository(CacheServiceManager cacheServiceManager){
        this.cacheServiceManager = cacheServiceManager;
    }

    /**
     * SAVE- Crear o Actualizar sesion
     */
    public void save(ProtocolSession session){
        String key = buildKey(session.sessionId());
        cacheServiceManager.put(CACHE_NAME, key, session);
        log.debug("SESSION SAVE sessionId={} type={}",
                session.sessionId(), session.transportType());
    }

    /**
     * FIND -leer sesion
     * Return Optional.empty()
     */
    public Optional<ProtocolSession> findById(String sessionId){
        String key = buildKey(sessionId);
        return cacheServiceManager.get(CACHE_NAME, key, ProtocolSession.class);
    }

    /**
     * CHECK EXISTs -Existencia Sesion
     * Return Bool
     */
    public boolean exists(String sessionId){
        return findById(sessionId)
                .map(session -> session.status() ==
                        ProtocolSession.SessionStatus.ACTIVE)
                .orElse(false);
    }

    /**
     * DELETE SESION - Eliminar Sesion
     * Return
     */
    public void delete(String sessionId){
        String key = buildKey(sessionId);
        cacheServiceManager.evict(CACHE_NAME,key);
        log.debug("SESSION DELETE sessionId={}", sessionId);
    }

    private String buildKey(String sessionId){
        return KEY_PREFIX + sessionId;
    }

    /**
     * Retorna el total de sesiones activas en Cafeína.
     * Usa estimatedSize() de Cafeína via CacheModel.
     */
    public int count() {
        return (int) cacheServiceManager
                .stats(CACHE_NAME)
                .estimatedSize();
    }




}
