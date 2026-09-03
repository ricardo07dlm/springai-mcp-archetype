package io.github.ricardodlm.springai.mcp.transport.session.manager;

import io.github.ricardodlm.springai.mcp.transport.session.model.StdioProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.ProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.StreamableProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.session.model.WsProtocolSession;

import java.util.Optional;

public sealed interface SessionManagerPort
        permits SessionManager {

    // Methods Crear Sesion cualquer tipo transporte
    StreamableProtocolSession createStreamableSession(String clientId, String jwt);
    WsProtocolSession createWsSession(String clientId, String jwt, String wsSessionId);
    StdioProtocolSession createStdioSession(String clientId, String jwt, Long processId);

    // Actualizar JWT (token rotado entre requests)
    void refreshJwt(String sessionId, String newJwt);

    //VALIDAR - CHECK SESSION
    ProtocolSession validate(String sessionId);

    // DESTRUIR - DELETE SESSION
    void destroy(String sessionId);

    // CONSULTAR - SESSION
    Optional<ProtocolSession> findById(String sessionId);
    boolean isActive(String sessionId);


}
