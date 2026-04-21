# Transactions API - cURL Examples

## Basis-Setup

```bash
# Setze Umgebungsvariablen
BASE_URL="http://localhost:8080"
CUSTOMER_TOKEN="your_customer_jwt_token"
SELLER_TOKEN="your_seller_jwt_token"
ITEM_ID="item-123"
```

## 1. Neue Bestellung erstellen (als Kunde)

```bash
curl -X POST "$BASE_URL/api/v1/transactions" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "'$ITEM_ID'",
    "quantityOrdered": 2.5,
    "notes": "Bitte frisch verpacken"
  }'
```

**Response (201 Created):**
```json
{
  "id": "trans-001",
  "itemId": "item-123",
  "customerEmail": "customer@example.com",
  "sellerEmail": "seller@example.com",
  "quantityOrdered": 2.5,
  "status": "PENDING",
  "pricePerUnit": 4.99,
  "totalPrice": 12.475,
  "notes": "Bitte frisch verpacken",
  "createdAt": "2025-09-24T10:30:00",
  "updatedAt": "2025-09-24T10:30:00"
}
```

---

## 2. Bestellung abrufen

```bash
curl -X GET "$BASE_URL/api/v1/transactions/trans-001" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Response (200 OK):** Transaction-Details

---

## 3. Meine Bestellungen abrufen (als Kunde)

```bash
curl -X GET "$BASE_URL/api/v1/transactions/customer/my-orders" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Response (200 OK):**
```json
[
  {
    "id": "trans-001",
    "itemId": "item-123",
    "customerEmail": "customer@example.com",
    "sellerEmail": "seller@example.com",
    "quantityOrdered": 2.5,
    "status": "PENDING",
    ...
  },
  {
    "id": "trans-002",
    ...
  }
]
```

---

## 4. Meine Verkäufe abrufen (als Verkäufer)

```bash
curl -X GET "$BASE_URL/api/v1/transactions/seller/my-sales" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

**Response (200 OK):** Array von Transaction-Details

---

## 5. Meine Bestellungen für ein Item abrufen (als Kunde)

```bash
curl -X GET "$BASE_URL/api/v1/transactions/customer/item/$ITEM_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Beschreibung:**
- Ruft alle Bestellungen des aktuellen Kunden für ein bestimmtes Item ab
- Der Kunde wird automatisch aus dem Bearer Token extrahiert
- Nur Transaktionen, bei denen der Kunde als `customer_email` eingetragen ist, werden zurückgegeben

**Response (200 OK):**
```json
[
  {
    "id": "trans-001",
    "itemId": "item-123",
    "customerEmail": "customer@example.com",
    "sellerEmail": "seller@example.com",
    "quantityOrdered": 2.5,
    "status": "PENDING",
    ...
  }
]
```

---

## 6. Meine Verkäufe für ein Item abrufen (als Verkäufer)

```bash
curl -X GET "$BASE_URL/api/v1/transactions/seller/item/$ITEM_ID" \
  -H "Authorization: Bearer $SELLER_TOKEN"
```

**Beschreibung:**
- Ruft alle Verkäufe des aktuellen Verkäufers für ein bestimmtes Item ab
- Der Verkäufer wird automatisch aus dem Bearer Token extrahiert
- Nur Transaktionen, bei denen der Verkäufer als `seller_email` eingetragen ist, werden zurückgegeben

**Response (200 OK):** Array von Transaction-Details für dieses Item

---

## 7. Bestellung bestätigen (als Verkäufer: PENDING → CONFIRMED)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

**Effekt:**
- Status wird auf CONFIRMED geändert
- Item.quantity wird um 2.5 reduziert
- Bei Fehler: InsufficientQuantityException (409 Conflict)

**Response (200 OK):**
```json
{
  "id": "trans-001",
  "status": "CONFIRMED",
  "updatedAt": "2025-09-24T10:35:00",
  ...
}
```

---

## 8. Bestellung ablehnen (PENDING → REJECTED)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REJECTED"
  }'
```

**Effekt:**
- Status wird auf REJECTED geändert
- Keine Quantity-Änderung (war noch PENDING)

---

## 9. Bestellung als abgeschlossen markieren (CONFIRMED → COMPLETED)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```

**Effekt:**
- Status wird auf COMPLETED geändert
- Keine weitere Quantity-Änderung (war bereits reduziert)
- Endzustand erreicht

---

## 10. Bestätigte Bestellung stornieren (CONFIRMED → CANCELLED)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CANCELLED"
  }'
```

**Effekt:**
- Status wird auf CANCELLED geändert
- Item.quantity wird um 2.5 erhöht (Restitution)
- Endzustand erreicht

---

## 11. Bestellung löschen (nur PENDING)

```bash
curl -X DELETE "$BASE_URL/api/v1/transactions/trans-001" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Bedingungen:**
- Status muss PENDING sein
- Aufrufer muss Kunde oder Verkäufer sein

**Response (204 No Content):** Keine Rückgabe

---

## Fehlerszenarien

### 11a. Fehler: Bestellung nicht gefunden (404)

```bash
curl -X GET "$BASE_URL/api/v1/transactions/nonexistent-id" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Response (404 Not Found):**
```json
{
  "errorMessage": "Transaction not found: nonexistent-id",
  "errorCode": "ERR_NOT_FOUND",
  "errorUUID": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 11b. Fehler: Nicht genug Menge (409)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

**Response (409 Conflict):**
```json
{
  "errorMessage": "Insufficient quantity for item item-123: available=1.0, required=2.5",
  "errorCode": "ERR_INSUFFICIENT_QTY",
  "errorUUID": "..."
}
```

### 11c. Fehler: Ungültiger Status-Übergang (400)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PENDING"
  }'
```

**Response (400 Bad Request):**
```json
{
  "errorMessage": "Invalid status transition from CONFIRMED to PENDING",
  "errorCode": "ERR_INVALID_TRANSITION",
  "errorUUID": "..."
}
```

### 11d. Fehler: Nicht berechtigt (403)

```bash
curl -X PUT "$BASE_URL/api/v1/transactions/trans-001/status" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

**Response (403 Forbidden):**
```json
{
  "errorMessage": "Only seller can update transaction status",
  "errorCode": "ERR_UNAUTHORIZED",
  "errorUUID": "..."
}
```

### 11e. Fehler: Bestellung kann nicht gelöscht werden (409)

```bash
curl -X DELETE "$BASE_URL/api/v1/transactions/trans-001" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**Response (409 Conflict):** wenn Status nicht PENDING
```json
{
  "errorMessage": "Cannot delete transaction with status CONFIRMED",
  "errorCode": "ERR_INVALID_STATE",
  "errorUUID": "..."
}
```

---

## Workflow-Beispiel: Kompletter Ablauf

### Phase 1: Kunde platziert Bestellung

```bash
# Bestellung erstellen
RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/transactions" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "'$ITEM_ID'",
    "quantityOrdered": 2.5
  }')

TRANSACTION_ID=$(echo $RESPONSE | jq -r '.id')
echo "Bestellung erstellt: $TRANSACTION_ID mit Status PENDING"
```

### Phase 2: Verkäufer bestätigt Bestellung

```bash
# Status auf CONFIRMED ändern
curl -X PUT "$BASE_URL/api/v1/transactions/$TRANSACTION_ID/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'

echo "Bestellung bestätigt - Item-Quantity wurde reduziert"
```

### Phase 3: Verkäufer markiert als abgeschlossen

```bash
# Status auf COMPLETED ändern
curl -X PUT "$BASE_URL/api/v1/transactions/$TRANSACTION_ID/status" \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'

echo "Bestellung abgeschlossen!"
```

---

## Wichtige Hinweise

1. **Authentifizierung**: Alle Endpoints erfordern einen gültigen JWT Bearer Token
2. **Token-Extraktion**: Email wird aus dem Token extrahiert - stelle sicher dass der Token ein "sub" Claim mit der Email enthält
3. **Seller-Only Operations**: Status-Updates dürfen nur vom Verkäufer durchgeführt werden
4. **Quantity-Management**: 
   - Wird erst bei CONFIRMED reduziert
   - Wird bei Stornierung/Ablehnung restitutiert
5. **Immutable Preis**: Der Preis wird als Snapshot beim Erstellen gespeichert - spätere Item-Preis-Änderungen beeinflussen nicht existierende Transaktionen

---

## Pretty-Print JSON Responses

Verwende `jq` für lesbare Ausgaben:

```bash
curl -s -X GET "$BASE_URL/api/v1/transactions/$TRANSACTION_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | jq '.'
```

---

**Version:** 1.0
**Status:** Ready for Testing
**Letzte Aktualisierung:** 2025-02-25
