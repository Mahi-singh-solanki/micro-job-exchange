package com.microjob.microjob_exchange.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Stack;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtsecret;

    private final long JWT_EXPIRATION=3600000;

    private Key getSigningKey(){
          return Keys.hmacShaKeyFor(jwtsecret.getBytes());
    }

    //generate token
    public String generateToken(Long id,String email,String role)
    {
        return Jwts.builder().setSubject(email).claim("id",id).claim("role","ROLE_"+role).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+JWT_EXPIRATION)).signWith(getSigningKey(),SignatureAlgorithm.HS256).compact();
    }

    //extract email
    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }
    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    // Method to extract the role string (which includes the "ROLE_" prefix)
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    //extract all claims
    private Claims extractAllClaims(String token)
    {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    //validate token
    public boolean validateToken(String token)
    {
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }catch(Exception e){
            System.out.println("JWT validation failed: "+e.getMessage());
            return false;
        }
    }

}
