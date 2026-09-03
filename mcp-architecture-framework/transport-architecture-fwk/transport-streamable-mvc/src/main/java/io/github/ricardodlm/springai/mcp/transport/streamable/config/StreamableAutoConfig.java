package io.github.ricardodlm.springai.mcp.transport.streamable.config;

import io.github.ricardodlm.springai.mcp.common.jwt.provider.JwtProvider;
import io.github.ricardodlm.springai.mcp.observability.logging.handler.McpAuthLoggingHandler;
import io.github.ricardodlm.springai.mcp.transport.session.repository.SessionRepository;
import io.github.ricardodlm.springai.mcp.transport.streamable.interceptor.McpSessionInterceptor;
import io.github.ricardodlm.springai.mcp.transport.streamable.provider.McpJwtProvider;
import io.github.ricardodlm.springai.mcp.transport.streamable.registry.StreamableSessionRegistry;
import io.github.ricardodlm.springai.mcp.transport.session.manager.SessionManagerPort;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autoconfiguracion del transporte Streamable HTTP para servidores MCP.
 * - StreamableSessionRegistry  → registro de sesiones activas
 * - StreamableHttpController   → endpoints POST/GET/DELETE /mcp
 *
 */

@AutoConfiguration
@EnableConfigurationProperties(StreamableProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean({
        SessionManagerPort.class,
        SessionRepository.class
})
@ConditionalOnProperty(
        prefix       = "sca.mcp.transport.streamable",
        name         = "enabled",
        havingValue  = "true",
        matchIfMissing = true     // activo por defecto
)
public class StreamableAutoConfig {

    /**
     * Registro de sesiones Streamable HTTP activas.
     */
    @Bean
    @ConditionalOnMissingBean(StreamableSessionRegistry.class)
    public StreamableSessionRegistry streamableSessionRegistry(
            SessionRepository repository,
            StreamableProperties       properties) {
        return new StreamableSessionRegistry(
                repository,
                properties.maxSessions()
        );
    }

    /**
     * Endpoint Streamable HTTP del servidor MCP.
     * Interceptor /mcp.
     */
    @Bean
    @ConditionalOnMissingBean
    public McpSessionInterceptor mcpSessionInterceptor(
            SessionManagerPort sessionManager,
            StreamableSessionRegistry registry,
            McpAuthLoggingHandler authLoggingHandler
            ) {
        return new McpSessionInterceptor(sessionManager, registry, authLoggingHandler);
    }

    @Bean
    public WebMvcConfigurer mcpInterceptorConfigurer(
            McpSessionInterceptor interceptor,
            StreamableProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                String endpoint = properties.endpoint();
                registry.addInterceptor(interceptor)
                        .addPathPatterns(endpoint, endpoint + "/**");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(JwtProvider.class)
    public McpJwtProvider mcpJwtProvider(
            StreamableSessionRegistry registry,
            HttpServletRequest request) {
        return new McpJwtProvider(registry, request);
    }
}
