package com.JavaWebToken.jwtAuthentication.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.JavaWebToken.jwtAuthentication.service.CustomUserDetailsService;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(CustomUserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // Only extract the token if the Authorization header is present and properly formatted
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (ExpiredJwtException ex) {
                logger.error("JWT Token has expired");
                handleException(response, "Token expired", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (JwtException ex) {
                // Handle various JWT exceptions
                String errorMessage = getJwtExceptionMessage(ex);
                logger.error("JWT Token error: " + errorMessage, ex);
                handleException(response, errorMessage, HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (IllegalArgumentException ex) {
                logger.error("JWT claims string is empty");
                handleException(response, "Invalid token claims", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        // Once we get the username, validate the token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    handleException(response, "Invalid token", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (UsernameNotFoundException ex) {
                logger.error("User not found with username: " + username);
                handleException(response, "User not found", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getJwtExceptionMessage(JwtException ex) {
        if (ex.getMessage().contains("unsupported")) {
            return "Unsupported token";
        } else if (ex.getMessage().contains("malformed")) {
            return "Invalid token format";
        } else if (ex.getMessage().contains("signature") || ex.getMessage().contains("verification")) {
            return "Invalid token signature";
        } else if (ex.getMessage().contains("expired")) {
            return "Token expired";
        } else {
            return "Invalid token";
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": %d}", message, status));
        response.getWriter().flush();
    }
}