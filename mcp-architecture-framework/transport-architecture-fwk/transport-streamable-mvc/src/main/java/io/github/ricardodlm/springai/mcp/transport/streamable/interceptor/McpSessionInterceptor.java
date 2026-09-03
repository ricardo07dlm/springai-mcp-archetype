package io.github.ricardodlm.springai.mcp.transport.streamable.interceptor;

import io.github.ricardodlm.springai.mcp.observability.logging.handler.McpAuthLoggingHandler;
import io.github.ricardodlm.springai.mcp.observability.logging.mdc.McpMdcPopulator;
import io.github.ricardodlm.springai.mcp.observability.logging.model.McpLogEntry;
import io.github.ricardodlm.springai.mcp.transport.auth.filter.ClientIdExtractor;
import io.github.ricardodlm.springai.mcp.transport.auth.filter.McpAuthenticationToken;
import io.github.ricardodlm.springai.mcp.transport.session.manager.SessionManagerPort;
import io.github.ricardodlm.springai.mcp.transport.session.model.StreamableProtocolSession;
import io.github.ricardodlm.springai.mcp.transport.streamable.registry.StreamableSessionRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Interceptor de sesión MCP — ejecuta ANTES del handler de Spring AI MCP.
 *
 * Flujo en preHandle():
 *   1. Extrae clientId del SecurityContext (McpAuthenticationFilter ya autenticó)
 *   2. Obtiene sesión existente o crea una nueva
 *   3. Valida que la sesión está activa
 *   4. Renueva actividad en Cafeína
 *   5. Añade Mcp-Session-Id al response header
 *
 * Si retorna false → Spring AI MCP no procesa la petición.
 * Si retorna true  → Spring AI MCP procesa el JSON-RPC directamente.
 */

public class McpSessionInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(McpSessionInterceptor.class);

    private static final String SESSION_HEADER = "Mcp-Session-Id";

    private final SessionManagerPort sessionManager;
    private final StreamableSessionRegistry   registry;
    private final McpAuthLoggingHandler logMcp;   // ← inyecta

    private final ThreadLocal<String> clientIdHolder = new ThreadLocal<>();
    private final ThreadLocal<String> jwtHolder = new ThreadLocal<>();

    public McpSessionInterceptor(SessionManagerPort sessionManager, StreamableSessionRegistry registry, McpAuthLoggingHandler logMcp) {
        this.sessionManager = sessionManager;
        this.registry = registry;
        this.logMcp = logMcp;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 1. Extrae clientId del SecurityContext
        //    McpAuthenticationFilter ya validó el token — si llegamos aquí es válido
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String clientId = ClientIdExtractor.extract(authentication);
        McpMdcPopulator.putClientId(clientId);
        String jwt = extractRawJwt(authentication);
        McpMdcPopulator.putClientId(clientId);

        // 2. Obtiene sesión existente o crea una nueva
        String sessionId = request.getHeader(SESSION_HEADER);
        if (sessionId != null && !sessionId.isBlank()) {
            // ── Sesión existente ──────────────────────────────
            if (!registry.exists(sessionId)) {

                // ── Traza SECURITY — sesión no encontrada ────────────────
                logMcp.security(McpLogEntry.builder()
                        .level("WARN")
                        .component(McpAuthLoggingHandler.COMPONENT_SESSION)
                        .sessionId(sessionId)
                        .mcpOperation("session/invalid")
                        .authResult(McpAuthLoggingHandler.RESULT_FAILURE)
                        .authFailReason("Session not found")
                        .errorCode("SESSION_001"));

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Session not found\"}");
                return false;
            }
            // Valida sesión activa
            try {
                sessionManager.validate(sessionId);
            } catch (Exception ex) {
                // ── Traza SECURITY — sesión inválida ─────────────────────
                logMcp.security(McpLogEntry.builder()
                        .level("WARN")
                        .component(McpAuthLoggingHandler.COMPONENT_SESSION)
                        .sessionId(sessionId)
                        .mcpOperation("session/invalid")
                        .authResult(McpAuthLoggingHandler.RESULT_FAILURE)
                        .authFailReason(ex.getMessage())
                        .errorCode(logMcp.resolveErrorCode(ex.getMessage())));


                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Session invalid\"}");
                return false;
            }

            // Renueva actividad en Cafeína
            registry.renewActivityAndJwt(sessionId, jwt);

            log.debug("SESSION OK sessionId={}... clientId={}", sessionId.substring(0, 8), clientId);
        } else {
            // ── Nueva sesión ──────────────────────────────────
            // NO crear sesión aquí — Spring AI genera el Mcp-Session-Id
            // La sesión se registra en afterCompletion con el ID de Spring AI
            if (registry.isOverloaded()) {
                logMcp.technical(McpLogEntry.builder()
                        .level("WARN")
                        .component(McpAuthLoggingHandler.COMPONENT_SESSION)
                        .clientId(clientId)
                        .httpStatus(503)
                        .errorCode("SESSION_002")
                        .errorMessage("Max sessions reached"));

                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Max sessions reached\"}");
                return false;
            }
            // Guarda clientId para afterCompletion
            clientIdHolder.set(clientId);
            jwtHolder.set(jwt);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        String clientId = clientIdHolder.get();
        String jwt = jwtHolder.get();
        clientIdHolder.remove(); // evitar memory leak
        jwtHolder.remove();

        if (clientId == null) return; // sesión existente — nada que registrar

        // Lee el Mcp-Session-Id que Spring AI escribió
        String mcpSessionId = response.getHeader(SESSION_HEADER);
        if (mcpSessionId == null || mcpSessionId.isBlank()) {
            log.warn("Spring AI did not set Mcp-Session-Id — session not registered");
            return;
        }

        // Registra en Cafeína con el sessionId de Spring AI
        StreamableProtocolSession session =
                StreamableProtocolSession.create(mcpSessionId, clientId, jwt);
        boolean registered = registry.register(session);

        if (registered) {
            // ── Traza TECHNICAL — sesión registrada ───────────────────
            logMcp.technical(McpLogEntry.builder()
                    .level("INFO")
                    .component(McpAuthLoggingHandler.COMPONENT_SESSION)
                    .sessionId(mcpSessionId)
                    .clientId(clientId)
                    .httpStatus(response.getStatus())
                    .mcpOperation("session/registered"));
        }
    }

    private String extractRawJwt(Authentication authentication) {
        if (authentication instanceof McpAuthenticationToken mcpAuth) {
            return (String) mcpAuth.getCredentials();
        }
        return null;
    }
}
