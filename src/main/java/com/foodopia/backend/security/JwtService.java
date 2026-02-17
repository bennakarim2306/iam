package com.foodopia.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long tokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;
    private final String audience;

    public JwtService(
            PrivateKey privateKey,
            PublicKey publicKey,
            @Value("${jwt.token-expiration:3600000}") long tokenExpiration,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration,
            @Value("${jwt.issuer:foodopia-backend}") String issuer,
            @Value("${jwt.audience:foodopia-api}") String audience) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.tokenExpiration = tokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        extraClaims.put("token_type", "access");
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .setId(UUID.randomUUID().toString())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .setId(UUID.randomUUID().toString())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean isRefreshToken(String token) {
        String tokenType = extractClaim(token, claims -> claims.get("token_type", String.class));
        return "refresh".equals(tokenType);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserEmail(token);
        final String tokenIssuer = extractClaim(token, Claims::getIssuer);
        final String tokenAudience = extractClaim(token, Claims::getAudience);
        
        return Objects.equals(userName, userDetails.getUsername()) 
                && Objects.equals(tokenIssuer, issuer)
                && Objects.equals(tokenAudience, audience)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
