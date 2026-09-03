package io.github.ricardodlm.springai.mcp.adapters.auth.idp.validator;

import io.github.ricardodlm.springai.mcp.common.jwt.provider.JwtClaims;

public sealed interface TokenValidationResult permits
        TokenValidationResult.Valid,
        TokenValidationResult.Invalid{

    record Valid(JwtClaims claims) implements  TokenValidationResult{}
    record Invalid(String reason) implements TokenValidationResult{}

    static Valid valid(JwtClaims claims){
        return new Valid(claims);
    }

    static Invalid invalid(String reason){
        return new Invalid(reason);
    }

}
