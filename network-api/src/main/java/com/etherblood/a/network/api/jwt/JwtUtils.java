package com.etherblood.a.network.api.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import org.bouncycastle.openssl.PEMReader;

public class JwtUtils {

    private static final boolean DEBUG_MODE = false;

    public static Token randomToken() {
        if (!DEBUG_MODE) {
            throw new AssertionError();
        }
        Token result = new Token();
        result.iat = Instant.now();
        result.user = new TokenUser();
        result.user.id = new SecureRandom().nextLong();
        result.user.login = Long.toUnsignedString(result.user.id, 16);
        return result;
    }

    public static String randomJwt() {
        return new Gson().toJson(randomToken());
    }

    public static Token verify(String jwt) {
        if (DEBUG_MODE) {
            return new Gson().fromJson(jwt, Token.class);
        }
        return fileVerify(jwt);
    }

    private static Token fileVerify(String jwt) {
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) readPublicKey("../assets/public.pem"), null);
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decodedJWT = verifier.verify(jwt);
        Token result = new Token();
        result.iat = decodedJWT.getIssuedAt().toInstant();
        result.user = decodedJWT.getClaim("user").as(TokenUser.class);
        return result;
    }

    private static PublicKey readPublicKey(String pathFilePath) {
        try {
            PEMReader pemReader = new PEMReader(new FileReader(pathFilePath));
            byte[] publicKeyBytes = pemReader.readPemObject().getContent();
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Token webVerify(String jwt) {
        try {
            URL url = URI.create("https://destrostudios.com:8080/authToken/verify").toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("authToken", jwt);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            try ( InputStream in = new BufferedInputStream(con.getInputStream())) {
                return new Gson().fromJson(new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8)), Token.class);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
