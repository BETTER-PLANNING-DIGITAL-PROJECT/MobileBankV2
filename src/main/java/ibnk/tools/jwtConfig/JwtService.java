package ibnk.tools.jwtConfig;

import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.UserEntity;
import ibnk.tools.response.TokenResponse;
import ibnk.tools.security.SecuritySubscriptionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    public final String SECRET_KEY = "and0U2VjcmV0QCM0NTMyQmV0dGVyUGxhbm5pbmdMaW1pdGVkUGhpbGZvbnRhaCgxMUBnbWFpbC5jb205Nzg3MTg3ODY4MzY4MjYzODQ4NjQyNjI1NDY4MjE5Z2VuYXJhdGU5MjYzODQ3OTk=";
    public  String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public  String extractUserID(String token){
        return extractClaim(token, Claims::getId);
    }

    public  String extractClientID(String token){
        return extractClaim(token, Claims::getId);
    }
    public <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    public String getTokenType(String token) {
        // Parse the JWT.
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);

        // Extract the token type from the JWT claims.

        return jws.getBody().get("tokenType", String.class);
    }

    public TokenResponse<String, Date, Integer> generateToken(UserEntity userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }
    public TokenResponse<String, Long,Integer> generateTokenForClient(Subscriptions ClientDetails){
        return generateTokenForClient(new HashMap<>(), ClientDetails);
    }

    public boolean isTokenValid(String token , UserEntity userDetails){
        final String Id = extractUserID(token);
        return  !isTokenExpired(token);
    }
    public boolean isTokenValidForClient(String token , Subscriptions clientDetails){
        final String Id = extractUserID(token);
        return (Id.equals(clientDetails.getUuid())) && !isTokenExpired(token);
    }

   private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public TokenResponse<String,Date, Integer> generateToken(Map<String,Object> extractClaims , UserEntity userDetails){
        extractClaims.put("tokenType", "User");
        var jt =Jwts
                .builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setId(userDetails.getUuid())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (5 *60 * 60 * 1000 )))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        Date expiresAt = extractExpiration(jt);
        var expiresIn = 5 *60 * 60 * 1000 ;
        return new TokenResponse<>(jt,expiresAt, expiresIn);
    }

    public TokenResponse<String,Long,Integer> generateTokenForClient(Map<String,Object> extractClaims , Subscriptions clientDetails){
        extractClaims.put("tokenType", "Client");
        var jt =Jwts
                .builder()
                .setClaims(extractClaims)
                .setSubject(clientDetails.getUsername())
                .setId(clientDetails.getUuid())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()  + (5 * 60 * 60 * 1000 )))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();
        Long expiresAt = extractExpiration(jt).getTime();
        var expiresIn = 5 *60 * 60 * 1000;
        return new TokenResponse<>(jt,expiresAt,expiresIn);
    }


    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
