package com.felipe.projectmanagerapi.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final SecurityFilter securityFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  public SecurityConfiguration(
    SecurityFilter securityFilter,
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
    CustomAccessDeniedHandler customAccessDeniedHandler
  ) {
    this.securityFilter = securityFilter;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    this.customAccessDeniedHandler = customAccessDeniedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    String authBaseUrl = "/api/auth";
    String userBaseUrl = "/api/users";
    String workspaceBaseUrl = "/api/workspaces";
    String projectBaseUrl = "/api/projects";
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
        .requestMatchers(HttpMethod.POST, authBaseUrl + "/register").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, authBaseUrl + "/login").permitAll()
        .requestMatchers(HttpMethod.GET, userBaseUrl).hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, userBaseUrl + "/me").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.PATCH, userBaseUrl + "/{userId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.GET, userBaseUrl + "/{userId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, userBaseUrl + "/{userId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, userBaseUrl + "/{userId}/role").hasRole("ADMIN")
        .requestMatchers(workspaceBaseUrl).hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, workspaceBaseUrl + "/{workspaceId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, workspaceBaseUrl + "/{workspaceId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, workspaceBaseUrl + "/{workspaceId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.GET, workspaceBaseUrl + "/{workspaceId}/members").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(workspaceBaseUrl + "/{workspaceId}/members/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, projectBaseUrl).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.PATCH, projectBaseUrl + "/{projectId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(projectBaseUrl + "/workspace/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, projectBaseUrl + "/{projectId}/workspace/**").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.POST, "/api/projects/test").permitAll()
        .anyRequest().authenticated())
      .addFilterBefore(this.securityFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exceptionHandling -> exceptionHandling
        .authenticationEntryPoint(this.customAuthenticationEntryPoint)
        .accessDeniedHandler(this.customAccessDeniedHandler))
      .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration
  ) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
