#!/bin/bash

# JWT RSA Key Generation Script for Local Development
# This script generates the RSA key pair needed for JWT token signing

set -e

# Create certificates directory
mkdir -p src/main/resources/certs
cd src/main/resources/certs

echo "Generating RSA key pair for JWT tokens..."

# Generate 2048-bit RSA private key
openssl genrsa -out private-key.pem 2048
echo "✓ Private key generated: private-key.pem"

# Extract public key from private key
openssl rsa -in private-key.pem -pubout -out public-key.pem
echo "✓ Public key generated: public-key.pem"

# Display key information
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RSA Key Generation Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Keys have been created in: src/main/resources/certs/"
echo ""
echo "Private Key:"
openssl rsa -in private-key.pem -text -noout | head -3
echo ""
echo "Public Key:"
openssl rsa -in private-key.pem -pubout -text -noout | head -3
echo ""
echo "Key Details:"
openssl rsa -in private-key.pem -text -noout | grep -E "Public-Key|modulus"
echo ""
echo "⚠️  IMPORTANT SECURITY NOTES:"
echo "   • NEVER commit private-key.pem to version control"
echo "   • In production, store keys in AWS Secrets Manager"
echo "   • Restrict file permissions: chmod 600 private-key.pem"
echo "   • See JWT_RSA_SETUP.md for detailed setup instructions"
echo ""
