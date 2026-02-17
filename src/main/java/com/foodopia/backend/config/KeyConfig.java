package com.foodopia.backend.config;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class KeyConfig {

    @Value("${jwt.private-key}")
    private String privateKeyPath;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    private final ResourceLoader resourceLoader;

    public KeyConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public PrivateKey privateKey() throws Exception {
        String keyContent = loadKeyContent(privateKeyPath);
        return parsePrivateKey(keyContent);
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        String keyContent = loadKeyContent(publicKeyPath);
        return parsePublicKey(keyContent);
    }

    private String loadKeyContent(String keyPath) throws IOException {
        // Handle base64 encoded format: base64:ENCODED_CONTENT
        if (keyPath.startsWith("base64:")) {
            String base64Content = keyPath.substring("base64:".length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

        // Handle environment variable format: env:ENV_VAR_NAME
        if (keyPath.startsWith("env:")) {
            String envVarName = keyPath.substring("env:".length());
            String content = System.getenv(envVarName);
            if (content == null || content.isEmpty()) {
                throw new IllegalStateException("Environment variable '" + envVarName + "' not found or empty");
            }
            // Check if the env var contains base64 encoded content
            if (content.startsWith("base64:")) {
                return loadKeyContent(content);
            }
            return content;
        }

        // Handle classpath resources: classpath:path/to/file.pem
        if (keyPath.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(keyPath);
            return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        }

        // Handle file system paths: /path/to/file.pem
        return new String(Files.readAllBytes(Paths.get(keyPath)), StandardCharsets.UTF_8);
    }

    private PrivateKey parsePrivateKey(String keyContent) throws Exception {
        try (StringReader reader = new StringReader(keyContent)) {
            PEMParser pemParser = new PEMParser(reader);
            Object obj = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (obj == null) {
                throw new IOException("PEM parser returned null. Key content might be malformed or empty.");
            }

            if (obj instanceof PEMKeyPair) {
                return converter.getPrivateKey(((PEMKeyPair) obj).getPrivateKeyInfo());
            } else if (obj instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) obj);
            } else {
                throw new IOException("Unsupported key format. Expected RSA PRIVATE KEY or PRIVATE KEY, got: " + obj.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to parse private key");
            throw e;
        }
    }

    private PublicKey parsePublicKey(String keyContent) throws Exception {
        try {
            String publicKeyPEM = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to parse public key");
            throw e;
        }
    }
}
