package ibnk.tools.jwtConfig;

import java.io.File;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import ibnk.models.internet.client.Subscriptions;
import io.jsonwebtoken.*;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

@NoArgsConstructor
public class MobileJwt {

    public PublicKey readPublicKey(File file) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return factory.generatePublic(pubKeySpec);
        }
    }

    public PrivateKey readPrivateKey(File file) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return factory.generatePrivate(privKeySpec);
        }
    }

    public  String generateJwtToken(Subscriptions client) throws Exception {
        Date expiration = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(expiration);
        c.add(Calendar.DATE, 60);
        expiration = c.getTime();

        return Jwts.builder()
                .setId(client.getClientMatricul())
                .setExpiration(expiration)
                .setIssuer("betterplanning.io")
                .setIssuedAt(new Date())
                .claim("phone", client.getPhoneNumber())
                .claim("client", client.getClientMatricul())
                .claim("nom", client.getClientName())
                .claim("compte", client.getPrimaryAccount())
                .claim("passwordOperation", client.getPassword() == null ? "" : client.getPassword())
                .signWith(readPrivateKey(new File(getClass().getResource("/priv.pem").getFile())), SignatureAlgorithm.RS256)
                .compact();
    }
    public Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(readPrivateKey(new File(getClass().getResource("/priv.pem").getFile())))
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception ex) {
            Logger.getLogger(Jwt.class.getName()).log(Level.SEVERE, null, ex);
            throw new JwtException("error decode", ex);
        }
    }
}


