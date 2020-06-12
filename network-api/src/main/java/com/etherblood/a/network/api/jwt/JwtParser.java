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
import org.bouncycastle.openssl.PEMReader;

public class JwtParser {

    private final JWTVerifier verifier;
    private final URL verifyUrl;

    JwtParser(JWTVerifier verifier, URL verifyUrl) {
        this.verifier = verifier;
        this.verifyUrl = verifyUrl;
    }

    public static JwtParser withPublicKeyFile(String path) {
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) readPublicKey(path), null);
        return new JwtParser(JWT.require(algorithm).build(), null);
    }

    public static JwtParser withVerifyUrl(String url) {
        try {
            return new JwtParser(null, new URL(url));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JwtAuthentication verify(String jwt) {
        JwtAuthentication result;
        if (verifier != null) {
            result = fileVerify(verifier, jwt);
        } else {
            result = webVerify(jwt);
        }
        result.rawJwt = jwt;
        return result;
    }

    private JwtAuthentication webVerify(String jwt) {
        try {
            HttpURLConnection con = (HttpURLConnection) verifyUrl.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("authToken", jwt);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            try ( InputStream in = new BufferedInputStream(con.getInputStream())) {
                return new Gson().fromJson(new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8)), JwtAuthentication.class);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private JwtAuthentication fileVerify(JWTVerifier verifier, String jwt) {
        DecodedJWT decodedJWT = verifier.verify(jwt);
        JwtAuthentication result = new JwtAuthentication();
        result.iat = decodedJWT.getIssuedAt().toInstant();
        result.user = decodedJWT.getClaim("user").as(JwtAuthenticationUser.class);
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

}
