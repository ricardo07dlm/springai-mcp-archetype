package io.github.ricardodlm.springai.mcp.transport.session.manager;

import io.github.ricardodlm.springai.mcp.transport.session.config.SessionProperties;
import io.github.ricardodlm.springai.mcp.transport.session.exception.SessionException;
import io.github.ricardodlm.springai.mcp.transport.session.model.StdioProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.ProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.StreamableProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.WsProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestor del ciclo de vida de sesiones de transporte MCP.
 *
 * Responsabilidades:
 * - Crear sesión en handshake del protocolo
 * - Validar sesión en cada invocación de tool
 * - Renovar TTL con cada actividad
 * - Destruir sesión en desconexión
 *
 * Agnóstico del protocolo — soporta SSE, WebSocket y Local
 * via métodos de fábrica específicos por tipo.
 *
 * La persistencia delega en TransportSessionRepository
 * que usa CacheServiceManager de cache-service.jar.
 */
public final class SessionManager
        implements SessionManagerPort {

    private static final Logger log =
            LoggerFactory.getLogger(SessionManager.class);

    private final SessionRepository repository;
    private final SessionProperties properties;

    public SessionManager(SessionRepository repository,
                          SessionProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    // ================================================
    // CREAR SESSION — handshake del protocolo
    // ================================================

    @Override
    public StreamableProtocolSession createStreamableSession(String clientId, String jwt) {
        String sessionId = generateSessionId();
        StreamableProtocolSession session =
                StreamableProtocolSession.create(sessionId, clientId, jwt);

        repository.save(session);

        log.info("SESSION FACTORY STREAMABLE  sessionId={}... clientId={}",
                sessionId.substring(0, 8), clientId);
        return session;
    }

    @Override
    public WsProtocolSession createWsSession(String clientId, String jwt, String wsSessionId) {
        String sessionId = generateSessionId();
        WsProtocolSession session =
                WsProtocolSession.create(sessionId, clientId, jwt, wsSessionId);

        repository.save(session);

        log.info("SESSION FACTORY WebSocket sessionId={}... clientId={}",
                sessionId.substring(0, 8), clientId);
        return session;
    }

    @Override
    public StdioProtocolSession createStdioSession(String clientId, String jwt, Long processId) {
        String sessionId = generateSessionId();
        StdioProtocolSession session =
                StdioProtocolSession.create(sessionId, clientId, jwt, processId);

        repository.save(session);

        log.info("SESSION FACTORY Sdtio/Local sessionId={}... clientId={}",
                sessionId.substring(0, 8), clientId);
        return session;
    }

    // ================================================
    // VALIDAR — cada invocación de tool
    // ================================================

    @Override
    public ProtocolSession validate(String sessionId) {
        ProtocolSession session = repository.findById(sessionId)
                .orElseThrow(() ->
                        new SessionException
                                .SessionNotFoundException(sessionId));

        if (session.status() == ProtocolSession.SessionStatus.TERMINATED){
            throw new SessionException
                    .SessionExpiredException(sessionId);
        }
        // Renueva actividad — actualiza lastActivityAt
        ProtocolSession renewed = renewActivity(session);
        repository.save(renewed);

        log.debug("SESSION VALIDATE sessionId={} type={}",
                sessionId, session.transportType());

        return renewed;
    }

    // ================================================
    // DESTRUIR — desconexión del cliente
    // ================================================

    @Override
    public void destroy(String sessionId) {
        repository.findById(sessionId).ifPresent(session -> {
            //Define como terminada
            ProtocolSession terminated = terminateSession(session);
            repository.save(terminated);

            // Delete Cache
            repository.delete(sessionId);


            log.info("SESSION DESTROY sessionId={} type={} duration={}ms",
                    sessionId,
                    session.transportType(),
                    calculateDuration(session));
        });

    }

    // ================================================
    // Actualiza Session
    // ================================================

    @Override
    public void refreshJwt(String sessionId, String newJwt) {
        repository.findById(sessionId)
                .filter(s -> s instanceof StreamableProtocolSession)
                .map(s -> ((StreamableProtocolSession) s).withJwt(newJwt))  // ← usa withJwt del record
                .ifPresent(repository::save);
    }

    // ================================================
    // CONSULTAR
    // ================================================

    /**
     * Busca una sesión por ID sin validar ni renovar.
     */
    @Override
    public Optional<ProtocolSession> findById(String sessionId) {
        return repository.findById(sessionId);
    }
    /**
     * Verifica si una sesión existe y está activa.
     */
    @Override
    public boolean isActive(String sessionId) {
        return repository.exists(sessionId);
    }

    // ================================================
    // Lógica interna transacional SESSION/MCP
    // ================================================

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private String resolveNodeId() {
        String nodeId = System.getenv("NODE_ID");
        return nodeId != null ? nodeId : "node-default";
    }

    /**
     * Renueva la actividad
     */
    private ProtocolSession renewActivity(ProtocolSession session){
        return  switch(session){
            case StreamableProtocolSession streamable  -> streamable.renewActivity();
            case WsProtocolSession ws -> ws.renewActivity();
            case StdioProtocolSession stdio -> stdio.renewActivity();
        };
    }

    /**
     * Termina la sesión
     */
    private ProtocolSession terminateSession(ProtocolSession sesion){
        return switch (sesion){
            case StreamableProtocolSession streamable -> streamable.terminate();
            case WsProtocolSession ws -> ws.terminate();
            case StdioProtocolSession std -> std.terminate();
        };
    }

    /**
     * Calcula duración de la sesión en milisegundos para añadir en la traza .
     */
    private long calculateDuration(ProtocolSession session) {
        return java.time.Duration
                .between(session.connectedAt(), session.lastActivityAt())
                .toMillis();
    }


}
