package com.felipe.projectmanagerapi.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

  private final String AUTH_BASE_URL = "/api/auth";
  private final String USER_BASE_URL = "/api/users";
  private final String WORKSPACE_BASE_URL = "/api/workspaces";
  private final String PROJECT_BASE_URL = "/api/projects";
  private final String TASK_BASE_URL = "/api/tasks";


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
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(Customizer.withDefaults())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
        .requestMatchers(HttpMethod.POST, AUTH_BASE_URL + "/register").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, AUTH_BASE_URL + "/login").permitAll()
        .requestMatchers(HttpMethod.GET, USER_BASE_URL).hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, USER_BASE_URL + "/me").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.PATCH, USER_BASE_URL + "/{userId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.GET, USER_BASE_URL + "/{userId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, USER_BASE_URL + "/{userId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, USER_BASE_URL + "/{userId}/role").hasRole("ADMIN")
        .requestMatchers(WORKSPACE_BASE_URL).hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, WORKSPACE_BASE_URL + "/{workspaceId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, WORKSPACE_BASE_URL + "/{workspaceId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, WORKSPACE_BASE_URL + "/{workspaceId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.GET, WORKSPACE_BASE_URL + "/{workspaceId}/members").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(WORKSPACE_BASE_URL + "/{workspaceId}/members/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, PROJECT_BASE_URL).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.GET, PROJECT_BASE_URL).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.DELETE, PROJECT_BASE_URL).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.PATCH, PROJECT_BASE_URL + "/{projectId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.DELETE, PROJECT_BASE_URL + "/{projectId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(PROJECT_BASE_URL + "/workspaces/{workspaceId}/owner/{ownerId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, PROJECT_BASE_URL + "/{projectId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.GET, PROJECT_BASE_URL + "/owner/{ownerId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, PROJECT_BASE_URL + "/owner/{ownerId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, PROJECT_BASE_URL + "/workspaces/{workspaceId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.DELETE, PROJECT_BASE_URL + "/workspaces/{workspaceId}").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, TASK_BASE_URL).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.GET, TASK_BASE_URL).hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.GET, TASK_BASE_URL + "/{taskId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.DELETE, TASK_BASE_URL + "/{taskId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.PATCH, TASK_BASE_URL + "/{taskId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.GET, TASK_BASE_URL + "/projects/{projectId}").hasAnyRole("ADMIN", "WRITE_READ", "READ_ONLY")
        .requestMatchers(HttpMethod.DELETE, TASK_BASE_URL + "/projects/{projectId}").hasAnyRole("ADMIN", "WRITE_READ")
        .requestMatchers(HttpMethod.GET, TASK_BASE_URL + "/owner/{ownerId}").hasRole("ADMIN")
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
