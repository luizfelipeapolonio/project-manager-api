package com.felipe.projectmanagerapi.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

  @Value("${api.security.token.secret}")
  private String secretKey;

  public String generateToken(UserPrincipal userPrincipal) throws JWTCreationException {
    try {
      Algorithm algorithm = Algorithm.HMAC256(this.secretKey);
      return JWT.create()
        .withIssuer("project-manager-api")
        .withSubject(userPrincipal.getUsername())
        .withExpiresAt(this.generateExpirationDate())
        .sign(algorithm);
    } catch(JWTCreationException exception) {
      throw new JWTCreationException("Erro ao gerar token", exception);
    }
  }

  public String validateToken(String token) throws JWTVerificationException {
    try {
      Algorithm algorithm = Algorithm.HMAC256(this.secretKey);
      return JWT.require(algorithm)
        .withIssuer("project-manager-api")
        .build()
        .verify(token)
        .getSubject();
    } catch(JWTVerificationException exception) {
      throw new JWTVerificationException("O Token de acesso fornecido expirou, foi revogado ou é inválido");
    }
  }

  private Instant generateExpirationDate() {
    // TODO: trocar minutos para horas
    return LocalDateTime.now().plusMinutes(1).toInstant(ZoneOffset.of("-03:00"));
  }
}
