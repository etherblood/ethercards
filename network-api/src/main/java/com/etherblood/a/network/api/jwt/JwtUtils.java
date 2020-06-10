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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.atomic.AtomicReference;
import org.bouncycastle.openssl.PEMReader;

public class JwtUtils {

    private static final AtomicReference<JWTVerifier> VERIFIER_REF = new AtomicReference<>(null);
    private static final AtomicReference<URL> VERIFY_URL = new AtomicReference<>(null);

    public static void setPublicKeyFilePath(String path) {
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) readPublicKey(path), null);
        VERIFIER_REF.set(JWT.require(algorithm).build());
    }

    public static void setVerifyUrl(String url) {
        try {
            VERIFY_URL.set(new URL(url));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Token verify(String jwt) {
        JWTVerifier verifier = VERIFIER_REF.get();
        if (verifier != null) {
            return fileVerify(verifier, jwt);
        }
        return webVerify(jwt);
    }

    private static Token fileVerify(JWTVerifier verifier, String jwt) {
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
            URL url = VERIFY_URL.get();
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
