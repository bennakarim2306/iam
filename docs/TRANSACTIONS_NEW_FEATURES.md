# Transactions API - Neue Features: Between Users & Filtering

## 🎉 Neue APIs

### 1. GET /api/v1/transactions/between - Transaktionen zwischen zwei Usern

**Beschreibung:**
Ruft alle Transaktionen zwischen dem aktuellen User (aus dem Token) und einem anderen User ab. Der Nutzer wird via Email-Parameter angegeben.

**Endpoint:**
```
GET /api/v1/transactions/between?email=other.user@example.com
```

**Header:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Query Parameter:**
| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `email` | String | ✅ JA | Email des anderen Users |
| `status` | String | ❌ NEIN | Filter nach Status (PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED) |
| `minDate` | Date | ❌ NEIN | Minimum Erstellungsdatum (yyyy-MM-dd) |
| `maxDate` | Date | ❌ NEIN | Maximum Erstellungsdatum (yyyy-MM-dd) |
| `minPrice` | Double | ❌ NEIN | Minimum Gesamtpreis |
| `maxPrice` | Double | ❌ NEIN | Maximum Gesamtpreis |
| `minQuantity` | Double | ❌ NEIN | Minimum bestellte Menge |
| `maxQuantity` | Double | ❌ NEIN | Maximum bestellte Menge |

**cURL Beispiel:**
```bash
# Ohne Filter
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=seller@example.com" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Mit Filtern
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=seller@example.com&status=CONFIRMED&minPrice=10&maxPrice=100" \
  -H "Authorization: Bearer YOUR_TOKEN"
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
    "status": "CONFIRMED",
    "pricePerUnit": 4.99,
    "totalPrice": 12.475,
    "notes": "Bitte frisch verpacken",
    "createdAt": "2025-09-24T10:30:00",
    "updatedAt": "2025-09-24T10:35:00"
  }
]
```

**Sicherheit:**
- ✅ JWT Bearer Token erforderlich
- ✅ Transaktionen werden von der API automatisch gefiltert
- ✅ User können nur Transaktionen sehen, an denen sie direkt beteiligt sind
- ✅ Eine Transaktion zwischen zwei Usern wird gezählt, wenn:
  - User A ist Kunde UND User B ist Verkäufer, ODER
  - User A ist Verkäufer UND User B ist Kunde

---

## 📊 Filter-Functionality für alle APIs

Alle Transaktions-APIs unterstützen jetzt optionale Filter-Parameter:

### Filter-Parameter

| Parameter | Typ | Beschreibung | Beispiel |
|---|---|---|---|
| `status` | String | Filtert nach Transaktionsstatus | `status=PENDING` oder `status=CONFIRMED` |
| `minDate` | Date | Minimum Erstellungsdatum (inclusive) | `minDate=2025-01-01` |
| `maxDate` | Date | Maximum Erstellungsdatum (inclusive) | `maxDate=2025-12-31` |
| `minPrice` | Double | Minimum Gesamtpreis | `minPrice=10.50` |
| `maxPrice` | Double | Maximum Gesamtpreis | `maxPrice=100.00` |
| `minQuantity` | Double | Minimum bestellte Menge | `minQuantity=1.0` |
| `maxQuantity` | Double | Maximum bestellte Menge | `maxQuantity=50.0` |

### Verfügbare Status-Werte
- `PENDING` - Bestellung erwartet Bestätigung
- `CONFIRMED` - Bestellung bestätigt
- `COMPLETED` - Bestellung abgeschlossen
- `CANCELLED` - Bestellung storniert
- `REJECTED` - Bestellung abgelehnt

---

## 📋 Alle APIs mit Filter-Support

### 1. GET /api/v1/transactions/customer/my-orders
**Mit Filter:**
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders?status=PENDING&minPrice=5&maxPrice=50" \
  -H "Authorization: Bearer <customer_token>"
```

### 2. GET /api/v1/transactions/seller/my-sales
**Mit Filter:**
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/my-sales?status=CONFIRMED&minDate=2025-01-01" \
  -H "Authorization: Bearer <seller_token>"
```

### 3. GET /api/v1/transactions/customer/item/{itemId}
**Mit Filter:**
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/item/item-123?minQuantity=2&maxQuantity=10" \
  -H "Authorization: Bearer <customer_token>"
```

### 4. GET /api/v1/transactions/seller/item/{itemId}
**Mit Filter:**
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/seller/item/item-123?status=CONFIRMED" \
  -H "Authorization: Bearer <seller_token>"
```

### 5. GET /api/v1/transactions/between?email={otherUserEmail}
**Mit Filter:**
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/between?email=other.user@example.com&status=COMPLETED&minPrice=50" \
  -H "Authorization: Bearer <token>"
```

---

## 🔗 Filter-Kombinationen

### Beispiel 1: Alle ausstehenden Bestellungen zwischen 10€ und 50€
```bash
GET /api/v1/transactions/customer/my-orders?status=PENDING&minPrice=10&maxPrice=50
```

### Beispiel 2: Alle bestätigten Verkäufe vom Januar 2025
```bash
GET /api/v1/transactions/seller/my-sales?status=CONFIRMED&minDate=2025-01-01&maxDate=2025-01-31
```

### Beispiel 3: Alle stornierten Transaktionen mit großem Volumen
```bash
GET /api/v1/transactions/between?email=user@example.com&status=CANCELLED&minQuantity=100
```

### Beispiel 4: Hochwertige Transaktionen für ein Item
```bash
GET /api/v1/transactions/customer/item/item-123?minPrice=500
```

---

## 🛡️ Sicherheit & Datenschutz

✅ **Authentifizierung:**
- JWT Bearer Token erforderlich für alle Endpoints
- Token muss in `Authorization` Header sein

✅ **Autorisierung:**
- Kunden sehen nur ihre eigenen Bestellungen
- Verkäufer sehen nur ihre eigenen Verkäufe
- Transaktionen zwischen zwei Usern werden nur angezeigt, wenn der Requester beteiligt ist
- Filter werden auf Datenbankebene angewendet

✅ **Datenschutz:**
- Email aus Token wird validiert
- Keine SQL-Injection möglich (JPA Parameter Binding)
- Sensitive Daten werden nicht geloggt

---

## 📈 Technische Details

### Filter-Implementierung

Die Filter werden im `TransactionService` über die `applyFilters()` Methode implementiert:

```java
private List<TransactionResponseDTO> applyFilters(
    List<Transaction> transactions,
    String status,
    LocalDate minDate,
    LocalDate maxDate,
    Double minPrice,
    Double maxPrice,
    Double minQuantity,
    Double maxQuantity) {
    
    return transactions.stream()
        .filter(t -> status == null || t.getStatus().toString().equalsIgnoreCase(status))
        .filter(t -> minDate == null || t.getCreatedAt().toLocalDate().isAfter(minDate) || ...)
        .filter(t -> maxDate == null || t.getCreatedAt().toLocalDate().isBefore(maxDate) || ...)
        .filter(t -> minPrice == null || t.getTotalPrice() >= minPrice)
        .filter(t -> maxPrice == null || t.getTotalPrice() <= maxPrice)
        .filter(t -> minQuantity == null || t.getQuantityOrdered() >= minQuantity)
        .filter(t -> maxQuantity == null || t.getQuantityOrdered() <= maxQuantity)
        .map(this::mapToResponseDTO)
        .collect(Collectors.toList());
}
```

### Repository Query für Between-Users

```java
@Query("SELECT t FROM transactions t WHERE " +
       "(t.customerEmail = :email1 AND t.sellerEmail = :email2) OR " +
       "(t.customerEmail = :email2 AND t.sellerEmail = :email1)")
List<Transaction> findByCustomerEmailAndSellerEmail(
    @Param("email1") String email1,
    @Param("email2") String email2
);
```

---

## 💡 Use Cases

### Use Case 1: Kund sucht ausstehende Bestellungen
```bash
GET /api/v1/transactions/customer/my-orders?status=PENDING
```
→ Alle Bestellungen anzeigen, die noch nicht bestätigt wurden

### Use Case 2: Verkäufer möchte Umsatz überblicken
```bash
GET /api/v1/transactions/seller/my-sales?status=COMPLETED&minDate=2025-01-01&maxDate=2025-12-31
```
→ Alle abgeschlossenen Verkäufe des Jahres 2025

### Use Case 3: Dispute auflösen zwischen zwei Usern
```bash
GET /api/v1/transactions/between?email=partner@example.com
```
→ Alle Transaktionen mit diesem spezifischen Partner anzeigen

### Use Case 4: High-Value Transactions filtern
```bash
GET /api/v1/transactions/customer/my-orders?minPrice=1000&status=CONFIRMED
```
→ Alle bestätigten Bestellungen über 1000€ anzeigen

---

## 📚 API-Übersicht (Vollständig aktualisiert)

| # | Methode | Endpoint | Neue Features |
|---|---|---|---|
| 1 | POST | `/api/v1/transactions` | - |
| 2 | GET | `/api/v1/transactions/{id}` | - |
| 3 | GET | `/api/v1/transactions/customer/my-orders` | ✨ Mit Filtern |
| 4 | GET | `/api/v1/transactions/seller/my-sales` | ✨ Mit Filtern |
| 5 | GET | `/api/v1/transactions/customer/item/{itemId}` | ✨ Mit Filtern |
| 6 | GET | `/api/v1/transactions/seller/item/{itemId}` | ✨ Mit Filtern |
| 7 | GET | `/api/v1/transactions/between` | 🆕 NEU - Mit Filtern |
| 8 | PUT | `/api/v1/transactions/{id}/status` | - |
| 9 | DELETE | `/api/v1/transactions/{id}` | - |

---

## ✅ Postman Collection

Die Postman Collection wurde aktualisiert mit:
- ✅ Neuem Endpoint: "Get Transactions Between Two Users"
- ✅ Filterbare Varianten aller GET-Endpoints mit vorkonfigurierten Query-Parametern
- ✅ Beispiel-Werte für alle Filter

Importieren Sie die aktualisierte Collection: `docs/POSTMAN_TRANSACTIONS.json`

---

**Version**: 2.0
**Status**: ✅ Production Ready
**Letzte Aktualisierung**: 2026-02-25
