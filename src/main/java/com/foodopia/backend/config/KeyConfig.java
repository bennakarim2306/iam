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
import java.security.spec.PKCS8EncodedKeySpec;
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
        // Handle AWS Secrets Manager format: aws-secretsmanager://secret-name
        if (keyPath.startsWith("aws-secretsmanager://")) {
            return loadFromAwsSecretsManager(keyPath.substring("aws-secretsmanager://".length()));
        }

        // Handle classpath resources: classpath:path/to/file.pem
        if (keyPath.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(keyPath);
            return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        }

        // Handle file system paths: /path/to/file.pem
        return new String(Files.readAllBytes(Paths.get(keyPath)), StandardCharsets.UTF_8);
    }

    private String loadFromAwsSecretsManager(String secretName) {
        // TODO: Implement AWS Secrets Manager integration
        // Example implementation:
        // SecretsManagerClient client = SecretsManagerClient.builder().region(Region.US_EAST_1).build();
        // GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretName).build();
        // GetSecretValueResponse response = client.getSecretValue(request);
        // return response.secretString();
        throw new UnsupportedOperationException("AWS Secrets Manager integration not yet implemented. " +
                "Use classpath or file system paths. For AWS integration, add aws-java-sdk-secretsmanager dependency.");
    }

    private PrivateKey parsePrivateKey(String keyContent) throws Exception {
        try (StringReader reader = new StringReader(keyContent)) {
            PEMParser pemParser = new PEMParser(reader);
            Object obj = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (obj instanceof PEMKeyPair) {
                // Handle RSA PRIVATE KEY format (PKCS1)
                return converter.getPrivateKey(((PEMKeyPair) obj).getPrivateKeyInfo());
            } else if (obj instanceof PrivateKeyInfo) {
                // Handle PRIVATE KEY format (PKCS8)
                return converter.getPrivateKey((PrivateKeyInfo) obj);
            } else {
                throw new IOException("Unsupported key format. Expected RSA PRIVATE KEY or PRIVATE KEY");
            }
        }
    }

    private PublicKey parsePublicKey(String keyContent) throws Exception {
        // Remove PEM headers and whitespace
        String publicKeyPEM = keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
