
//this is a subclass of onceperrequestfilter, it reads the authorization header
// then it checks if it contains a jwt
//then it validates the jwt
//creates an authentication object and put it to the securitycontextholder




package com.JavaWebToken.jwtAuthentication.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
            } catch (UnsupportedJwtException ex) {
                logger.error("JWT Token is unsupported");
                handleException(response, "Unsupported token", HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (MalformedJwtException ex) {
                logger.error("JWT Token is malformed");
                handleException(response, "Invalid token format", HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (SignatureException ex) {
                logger.error("JWT signature does not match");
                handleException(response, "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
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
                }
            } catch (UsernameNotFoundException ex) {
                logger.error("User not found with username: " + username);
                handleException(response, "User not found", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}