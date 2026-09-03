package io.github.ricardodlm.springai.mcp.transport.session.config;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Propiedades configurables del servicio de sesión de transporte MCP.
 *
 * Ejemplo de configuración en el proyecto MCP Server:
 *
 * sca:
 *   transport:
 *     session:
 *       max-sessions: 500
 *       log-events:   false
 */

@ConfigurationProperties(prefix = "sca.mcp.transport.session")
public record SessionProperties(

    /**
     * Máximo de sesiones simultáneas permitidas.
     * Protege contra sobrecarga del servidor MCP.
     * Default: 500
     */
    int maxSessions,

    /**
     * Activa logging detallado de eventos de sesión.
     * Solo para desarrollo — desactivar en producción.
     * Default: false
     */
    boolean logEvents
){
    public SessionProperties {
        if (maxSessions == 0){
            maxSessions = 500;
        }
    }
}
