package io.github.ricardodlm.springai.mcp.adapters.auth.idp.jwks;

public class JwksLoadException  extends RuntimeException {

    public JwksLoadException(String message) {
        super(message);
    }

    public JwksLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
