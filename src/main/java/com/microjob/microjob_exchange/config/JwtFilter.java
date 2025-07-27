package com.microjob.microjob_exchange.config;

import com.microjob.microjob_exchange.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException,IOException{

        //Extract Authorization header
        final String authHeader=request.getHeader("Authorization");
        String jwtToken=null;
        String email=null;

        //checking if header contains bearer token
        if(authHeader!=null&&authHeader.startsWith("Bearer ")) {
            jwtToken=authHeader.substring(7);//to remove bearer prefix
            try{
                email=jwtUtil.extractEmail(jwtToken);
            }catch (Exception e){
                System.out.println("JWT Extraction failed: "+e.getMessage());
            }
        }

        //validate token
        if(email!=null&&SecurityContextHolder.getContext().getAuthentication()==null)
        {
            UserDetails userDetails=userDetailsService.loadUserByUsername(email);

            if(jwtUtil.validateToken(jwtToken)){
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        //continue with next filter
        filterChain.doFilter(request,response);
    }
}
