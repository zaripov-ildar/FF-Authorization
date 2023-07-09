package ru.findFood.auth.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.entities.Role;
import ru.findFood.auth.exceptions.WrongJwtException;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtils {

    @Value("${security_params.secret_key}")
    private String SECRET_KEY;

    @Value("${security_params.expiration.rt}")
    private Long refreshTokenExpiration;

    @Value("${security_params.expiration.jwt}")
    private Long accessTokenExpiration;


    public boolean isTokenValid(String token, UserDetails person) {
        String jwtEmail = extractEmail(token);
        String personEmail = person.getUsername();
        log.warn("notExpired:" + isNotExpired(token) + "\t\tcorrectEmail: " + jwtEmail.equals(personEmail));
        return jwtEmail.equals(personEmail)
                && isNotExpired(token);
    }

    private boolean isNotExpired(String token) {
        return (new Date()).before(extractExpiration(token));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public String generateJwt(AuthUser person) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles =person.getRoles()
                .stream()
                .map(Role::getTitle)
                .toList();
        claims.put("roles", roles);
        log.warn(claims.toString());
        return generateJwt(person, claims);
    }

    public String generateJwt(AuthUser person, Map<String, Object> extraClaims) {
        return createToken(person, extraClaims, accessTokenExpiration);
    }


    public String generateRefreshToken(AuthUser person) {
        return createToken(person, new HashMap<>(), refreshTokenExpiration);
    }

    private String createToken(AuthUser person, Map<String, Object> claims, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(person.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error(e.getMessage() + " >>> " + token);
            throw new WrongJwtException(token);
        }
    }

    private Key getSigningKey() {
        byte[] byteArray = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(byteArray);
    }
}
