package com.vidracariaCampos.security;

import com.vidracariaCampos.model.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain){

        var token = this.recoverToken(request);
        try {
            if(token != null ){
                var email = tokenService.validateToken(token);
                UserDetails user = userRepository.findByEmail(email);
                System.out.println(user.toString());
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                System.out.println(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request,response);
        }catch (NullPointerException e) {
            throw new RuntimeException("user not found");
        }catch (Exception e){
           throw new RuntimeException(e);
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
