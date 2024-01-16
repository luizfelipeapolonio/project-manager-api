package com.felipe.projectmanagerapi.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final AuthorizationService authorizationService;

  public SecurityFilter(TokenService tokenService, AuthorizationService authorizationService) {
    this.tokenService = tokenService;
    this.authorizationService = authorizationService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String token = this.recoverToken(request);

      if(token != null) {
        String email = this.tokenService.validateToken(token);
        UserDetails userPrincipal = this.authorizationService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      filterChain.doFilter(request, response);

    } catch(Exception exception) {
      // TODO: trocar para o resolver
      System.out.println(exception.getMessage());
    }

  }

  private String recoverToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    if(authorizationHeader == null) return null;
    return authorizationHeader.replace("Bearer ", "");
  }
}
