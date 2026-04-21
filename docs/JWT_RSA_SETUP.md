# JWT RSA Key Setup Guide

This document explains how to generate RSA key pairs for JWT token signing and verification, and how to configure them for both local development and AWS production environments.

## Overview

The JWT service has been migrated from symmetric (HMAC-HS256) to asymmetric (RSA-RS256) cryptography:
- **Private Key**: Used to sign tokens (kept secure on the server)
- **Public Key**: Used to verify tokens (can be shared)

## Generating RSA Key Pairs

### Option 1: OpenSSL (Recommended)

```bash
# Generate private key (2048-bit RSA)
openssl genrsa -out private-key.pem 2048

# Extract public key from private key
openssl rsa -in private-key.pem -pubout -out public-key.pem
```

### Option 2: Java keytool

```bash
# Generate keystore with RSA keypair
keytool -genkeypair -alias jwt-key -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore jwt-keystore.p12 -storepass your-password

# Export private key (requires additional tools)
# Export public certificate
keytool -export -alias jwt-key -storetype PKCS12 -keystore jwt-keystore.p12 \
  -file public-cert.pem -storepass your-password
```

## Local Development Setup

### Step 1: Create certificates directory

```bash
mkdir -p src/main/resources/certs
```

### Step 2: Generate keys

```bash
cd src/main/resources/certs

# Generate private key
openssl genrsa -out private-key.pem 2048

# Generate public key
openssl rsa -in private-key.pem -pubout -out public-key.pem
```

### Step 3: Application configuration

The `application.properties` will automatically load from classpath:

```properties
jwt.private-key=classpath:certs/private-key.pem
jwt.public-key=classpath:certs/public-key.pem
jwt.token-expiration=3600000  # 1 hour in milliseconds
```

### Step 4: Load keys in Git

Add the certificate files (optional for development):

```bash
git add src/main/resources/certs/
```

**Note**: Never commit private keys to version control in production!

## AWS Production Setup

### Step 1: Store keys in AWS Secrets Manager

```bash
# Create secret for private key
aws secretsmanager create-secret \
  --name foodopia/jwt/private-key \
  --secret-string file://private-key.pem

# Create secret for public key
aws secretsmanager create-secret \
  --name foodopia/jwt/public-key \
  --secret-string file://public-key.pem
```

### Step 2: Update application.properties

```properties
jwt.private-key=aws-secretsmanager://foodopia/jwt/private-key
jwt.public-key=aws-secretsmanager://foodopia/jwt/public-key
jwt.token-expiration=3600000
```

### Step 3: Add AWS SDK dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId>
    <version>2.20.0</version>
</dependency>
```

### Step 4: Implement AWS Secrets Manager integration

Update `KeyConfig.java` `loadFromAwsSecretsManager()` method:

```java
private String loadFromAwsSecretsManager(String secretName) {
    SecretsManagerClient client = SecretsManagerClient.builder()
        .region(Region.US_EAST_1)
        .build();
    
    GetSecretValueRequest request = GetSecretValueRequest.builder()
        .secretId(secretName)
        .build();
    
    GetSecretValueResponse response = client.getSecretValue(request);
    String secret = response.secretString();
    client.close();
    return secret;
}
```

### Step 5: Configure IAM permissions

Ensure your application's IAM role has permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:region:account:secret:foodopia/jwt/*"
      ]
    }
  ]
}
```

## Environment Variables

You can override key paths using environment variables:

```bash
export JWT_PRIVATE_KEY=classpath:certs/private-key.pem
export JWT_PUBLIC_KEY=classpath:certs/public-key.pem
export JWT_TOKEN_EXPIRATION=3600000
```

## Security Best Practices

1. **Private Key Protection**
   - Never commit private keys to version control
   - Store only in secure vaults (AWS Secrets Manager, HashiCorp Vault)
   - Restrict file permissions: `chmod 600 private-key.pem`

2. **Key Rotation**
   - Implement key rotation strategy (recommend annually or per compliance requirements)
   - Maintain multiple valid public keys during rotation period

3. **Access Control**
   - Use AWS IAM roles to restrict access to secrets
   - Enable Secrets Manager audit logging
   - Monitor for unauthorized access attempts

4. **Token Expiration**
   - Keep token expiration short (1-2 hours recommended)
   - Implement refresh token mechanism for long-lived sessions

## Troubleshooting

### "Cannot find key file" error
- Verify file path matches configuration
- For classpath resources, ensure files are in `src/main/resources/`
- Check file permissions (readable by application user)

### "Invalid key format" error
- Ensure PEM format is correct
- Verify no extra whitespace or invalid characters
- Check file was not corrupted during transfer

### "AWS Secrets Manager integration not yet implemented"
- Add AWS SDK dependency to pom.xml
- Implement the `loadFromAwsSecretsManager()` method
- Verify IAM permissions are configured

## Testing Keys

Generate test keypairs for testing:

```bash
# 1024-bit keys for faster testing (not for production)
openssl genrsa -out test-private-key.pem 1024
openssl rsa -in test-private-key.pem -pubout -out test-public-key.pem
```

## References

- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [JJWT Library Documentation](https://github.com/jwtk/jjwt)
- [RSA Key Generation](https://en.wikipedia.org/wiki/RSA_(cryptosystem))
