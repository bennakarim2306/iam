#!/bin/bash

# Account APIs - CURL Test Collection
# Aktualisiere <token> mit deinem JWT Bearer Token
# Aktualisiere http://localhost:8080 mit deiner API-URL falls nötig

TOKEN="<your_jwt_token_here>"
BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Account APIs - CURL Test Collection"
echo "=========================================="
echo ""

# 1. GET Account Details
echo "1. GET /api/v1/account/details"
echo "   Ruft die kompletten Account-Details ab"
echo ""
echo "CURL Command:"
echo "curl -X GET \"$BASE_URL/api/v1/account/details\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\""
echo ""
curl -X GET "$BASE_URL/api/v1/account/details" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# 2. UPDATE Account Details
echo "2. PUT /api/v1/account/details"
echo "   Aktualisiert Account-Details (ohne Bild)"
echo ""
echo "CURL Command:"
echo "curl -X PUT \"$BASE_URL/api/v1/account/details\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{...}'"
echo ""
curl -X PUT "$BASE_URL/api/v1/account/details" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "age": 30,
    "birthDay": "1995-05-15",
    "address": {
      "street": "Main Street 123",
      "city": "Berlin",
      "zip": "10115",
      "lat": 52.5200,
      "lng": 13.4050
    },
    "availabilityDays": "MON,TUE,WED,THU,FRI",
    "availabilityTimes": "09:00-17:00",
    "needConfirmation": false
  }' | jq .
echo ""
echo ""

# 3. UPDATE Profile Image
echo "3. POST /api/v1/account/image"
echo "   Aktualisiert das Profilbild"
echo ""
echo "CURL Command:"
echo "curl -X POST \"$BASE_URL/api/v1/account/image\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\" \\"
echo "  -F \"image=@/path/to/image.jpg\""
echo ""
echo "Hinweis: Bitte ein gültiges Bild verwenden!"
echo "Beispiel mit Test-Bild:"
echo "curl -X POST \"$BASE_URL/api/v1/account/image\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\" \\"
echo "  -F \"image=@test-image.jpg\""
echo ""
echo ""

# 4. DELETE Account
echo "4. DELETE /api/v1/account"
echo "   WARNUNG: Löscht das Benutzerkonto UNWIEDERBRINGLICH!"
echo ""
echo "CURL Command:"
echo "curl -X DELETE \"$BASE_URL/api/v1/account\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\""
echo ""
echo "Um diese Aktion auszuführen, entkommentieren Sie die Zeile unten:"
echo "# curl -X DELETE \"$BASE_URL/api/v1/account\" \\"
echo "#   -H \"Authorization: Bearer $TOKEN\""
echo ""

# 5. GET Address
echo "5. GET /api/v1/account/getAddressByEmail"
echo "   Ruft die Adresse des aktuellen Benutzers ab"
echo ""
echo "CURL Command:"
echo "curl -X GET \"$BASE_URL/api/v1/account/getAddressByEmail\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\""
echo ""
curl -X GET "$BASE_URL/api/v1/account/getAddressByEmail" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""
echo ""

# 6. SET Address
echo "6. POST /api/v1/account/setAddressByEmail"
echo "   Setzt die Adresse des aktuellen Benutzers"
echo ""
echo "CURL Command:"
echo "curl -X POST \"$BASE_URL/api/v1/account/setAddressByEmail\" \\"
echo "  -H \"Authorization: Bearer $TOKEN\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{...}'"
echo ""
curl -X POST "$BASE_URL/api/v1/account/setAddressByEmail" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "street": "New Street 456",
    "city": "Munich",
    "zip": "80331",
    "lat": 48.1351,
    "lng": 11.5820
  }' | jq .
echo ""
echo ""

echo "=========================================="
echo "Test-Collection abgeschlossen"
echo "=========================================="

