# Transactions API - cURL Examples (Neue Features)

## 1. Transaktionen zwischen zwei Usern abrufen

### Basis-Anfrage (alle Transaktionen zwischen zwei Usern)
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Response:**
```json
[
  {
    "id": "trans-001",
    "itemId": "item-123",
    "customerEmail": "you@example.com",
    "sellerEmail": "other.user@example.com",
    "quantityOrdered": 2.5,
    "status": "CONFIRMED",
    "pricePerUnit": 4.99,
    "totalPrice": 12.475,
    ...
  }
]
```

### Mit Status-Filter
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com&status=CONFIRMED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Mit Preis-Filter
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com&minPrice=50&maxPrice=500" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Mit Datums-Filter
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com&minDate=2025-01-01&maxDate=2025-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Mit kombiniertem Filter
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com&status=COMPLETED&minPrice=100&minQuantity=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 2. Meine Bestellungen mit Filtern abrufen

### Nur ausstehende Bestellungen
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?status=PENDING" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Bestellungen in Preisrange
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?minPrice=10&maxPrice=100" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Bestellungen mit großem Volumen
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?minQuantity=10" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Alle bestätigten Bestellungen von Januar
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?status=CONFIRMED&minDate=2025-01-01&maxDate=2025-01-31" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Abgeschlossene Bestellungen über 500€
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?status=COMPLETED&minPrice=500" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

---

## 3. Meine Verkäufe mit Filtern abrufen

### Nur bestätigte Verkäufe
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?status=CONFIRMED" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Stornierte Verkäufe
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?status=CANCELLED" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Hochwertiger Verkauf (über 200€)
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?minPrice=200" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Alle Verkäufe im Februar 2025
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?minDate=2025-02-01&maxDate=2025-02-28" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Bulkverkäufe (hohe Menge)
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?minQuantity=50" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

---

## 4. Transaktionen für Item mit Filtern

### Meine Bestellungen für ein Item (alle Status)
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/item/item-123" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Nur ausstehende Bestellungen für Item
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/item/item-123?status=PENDING" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Große Mengen für Item bestellt
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/item/item-123?minQuantity=10" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Meine Verkäufe für ein Item (bestätigt)
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/item/item-123?status=CONFIRMED" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

---

## 5. PowerShell-Beispiele (Windows)

### PowerShell: Transaktionen zwischen Usern
```powershell
$BASE_URL = "http://localhost:8080"
$TOKEN = "YOUR_JWT_TOKEN"
$OTHER_EMAIL = "other.user@example.com"

$response = Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions/between?email=$OTHER_EMAIL" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

### PowerShell: Mit Filtern
```powershell
$BASE_URL = "http://localhost:8080"
$TOKEN = "YOUR_JWT_TOKEN"

$response = Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions/customer/my-orders?status=PENDING&minPrice=50&maxPrice=500" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

---

## 6. Filter-Kombinationen Übersicht

| Use Case | Query Parameter | cURL |
|---|---|---|
| Ausstehende Bestellungen | `status=PENDING` | `/customer/my-orders?status=PENDING` |
| Transaktionen im Preisbereich | `minPrice=X&maxPrice=Y` | `?minPrice=50&maxPrice=500` |
| Transaktionen in Zeitbereich | `minDate=2025-01-01&maxDate=2025-12-31` | `?minDate=2025-01-01&maxDate=2025-12-31` |
| Hohe Mengen | `minQuantity=X` | `?minQuantity=10` |
| Kombiniert | `status=CONFIRMED&minPrice=100&minQuantity=5` | `?status=CONFIRMED&minPrice=100&minQuantity=5` |

---

## 7. Praktische Szenarien

### Szenario 1: Dispute-Auflösung zwischen zwei Partnern
"Ich möchte alle Transaktionen mit Partner X anschauen"

```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=partner@example.com" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Szenario 2: Finanzreporting
"Gib mir alle abgeschlossenen Verkäufe mit mindestens 1000€ Umsatz"

```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?status=COMPLETED&minPrice=1000" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Szenario 3: Bulk-Order-Übersicht
"Zeige alle Bestellungen mit mehr als 50 Einheiten"

```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?minQuantity=50" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Szenario 4: Aktuelle Probleme
"Zeige alle stornierten Transaktionen in diesem Monat"

```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?status=CANCELLED&minDate=2025-02-01&maxDate=2025-02-28" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

### Szenario 5: Item-spezifische Analyse
"Wieviele Bestellungen für Item X sind noch ausstehend?"

```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/item/item-123?status=PENDING" \
  -H "Authorization: Bearer SELLER_TOKEN"
```

---

## 8. Error-Szenarien

### Error: Fehlender Email-Parameter
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
**Response (400):** Email Parameter erforderlich

### Error: Falscher Token
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other@example.com" \
  -H "Authorization: Bearer INVALID_TOKEN"
```
**Response (401):** Nicht authentifiziert

### Error: Mit sich selbst vergleichen
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=yourself@example.com" \
  -H "Authorization: Bearer YOUR_TOKEN_WITH_SAME_EMAIL"
```
**Response (400):** Cannot get transactions between the same user

---

## 9. Umgebungsvariablen-Setup

### Bash/Linux/Mac
```bash
export BASE_URL="http://localhost:8080"
export CUSTOMER_TOKEN="your_customer_jwt_token"
export SELLER_TOKEN="your_seller_jwt_token"
export ITEM_ID="item-123"
export OTHER_EMAIL="other.user@example.com"

# Verwendung
curl -X GET "$BASE_URL/api/v1/transactions/between?email=$OTHER_EMAIL" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### PowerShell (Windows)
```powershell
$env:BASE_URL = "http://localhost:8080"
$env:CUSTOMER_TOKEN = "your_customer_jwt_token"
$env:SELLER_TOKEN = "your_seller_jwt_token"
$env:ITEM_ID = "item-123"
$env:OTHER_EMAIL = "other.user@example.com"

# Verwendung
Invoke-WebRequest -Uri "$env:BASE_URL/api/v1/transactions/between?email=$env:OTHER_EMAIL" `
  -Headers @{"Authorization"="Bearer $env:CUSTOMER_TOKEN"}
```

---

**Version**: 2.0
**Status**: ✅ Production Ready
**Letzte Aktualisierung**: 2026-02-25
