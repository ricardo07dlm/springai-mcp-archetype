package io.github.ricardodlm.springai.mcp.transport.streamable.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

/**
 * Propiedades configurables del transporte Streamable HTTP.
 * Configuración en application.yml del proyecto MCP Server:
 *
 * sca:
 *   transport:
 *     streamable:
 *       endpoint:         /mcp
 *       session-timeout:  30m
 *       max-sessions:     500
 *       keep-alive:
 *         enabled:  true
 *         interval: 30s
 */
@ConfigurationProperties(prefix = "sca.mcp.transport.streamable")
public record StreamableProperties(

    /**
     * Endpoint Streamable HTTP del servidor MCP.
     * Gestiona POST, GET y DELETE.
     * Default: /mcp
     */
    String endpoint,

    /**
     * Timeout de sesión MCP.
     * La sesión expira si no hay actividad durante este tiempo.
     * Default: 30 minutos
     */
    Duration sessionTimeout,

    /**
     * Máximo de sesiones MCP simultáneas.
     * Default: 500
     */
    int maxSessions,

    /**
     * Configuración del heartbeat.
     */
    KeepAlive keepAlive

) {
    public StreamableProperties {
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "/mcp";
        }
        if (sessionTimeout == null) {
            sessionTimeout = Duration.ofMinutes(30);
        }
        if (maxSessions == 0) {
            maxSessions = 500;
        }
        if (keepAlive == null) {
            keepAlive = new KeepAlive(true, Duration.ofSeconds(30));
        }

    }
    /**
     * Configuración del keep-alive SSE para streaming.
     */
    public record KeepAlive(
            boolean  enabled,
            Duration interval
    ) {
        public KeepAlive {
            if (interval == null) interval = Duration.ofSeconds(30);
        }
    }

}
