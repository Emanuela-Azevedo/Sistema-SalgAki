package com.salgaki.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        log.info("JwtAuthorizationFilter interceptou requisição para: {}", path);

        // Ignora autenticação para endpoints liberados
        if (path.startsWith("/api/auth")) {
            log.info("Liberado sem autenticação: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader(JwtUtils.JWT_AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(JwtUtils.JWT_BEARER)) {
            log.warn("Nenhum token JWT encontrado para {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(JwtUtils.JWT_BEARER.length());

        if (!JwtUtils.isTokenValid(token)) {
            log.warn("Token inválido ou expirado para {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String username = JwtUtils.getUserNameFromTokem(token);
        log.info("Token válido para usuário {}", username);
        authenticate(request, username);

        filterChain.doFilter(request, response);
    }
    private void authenticate(HttpServletRequest request, String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Usuário autenticado no contexto de segurança: {}", username);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
