# Transactions (Orders) Implementation - Complete Guide

## Überblick

Dieses Dokument beschreibt die vollständige Implementierung des Transaction-Management-Systems für das Foodopia-Backend. Das System verwaltet Bestellungen (Transaktionen) zwischen Kunden und Verkäufern für Angebote (Items).

## Architektur

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API (Controller)                     │
│           com.foodopia.backend.rest.v1                      │
│              TransactionController.java                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Business Logic (Service)                  │
│           com.foodopia.backend.service                      │
│              TransactionService.java                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Data Access Layer (JPA)                    │
│           com.foodopia.backend.data.transaction            │
│              TransactionRepository.java                     │
│              Transaction.java (Entity)                      │
│              TransactionStatus.java (Enum)                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Database (PostgreSQL)                      │
│              business_data.transactions                      │
└─────────────────────────────────────────────────────────────┘
```

## Datenbankschema

### Tabelle: `business_data.transactions`

```sql
CREATE TABLE business_data.transactions (
    id VARCHAR(255) PRIMARY KEY,
    
    -- Item Reference
    item_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES business_data.items(id),
    
    -- Parteien
    customer_email VARCHAR(255) NOT NULL,
    seller_email VARCHAR(255) NOT NULL,
    
    -- Bestelldetails
    quantity_ordered DOUBLE PRECISION NOT NULL,
    price_per_unit DOUBLE PRECISION NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED')),
    
    -- Metadaten
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indizes für schnelle Abfragen
CREATE INDEX idx_transactions_customer_email ON business_data.transactions(customer_email);
CREATE INDEX idx_transactions_seller_email ON business_data.transactions(seller_email);
CREATE INDEX idx_transactions_item_id ON business_data.transactions(item_id);
CREATE INDEX idx_transactions_status ON business_data.transactions(status);
```

## Entity-Klassen

### 1. Transaction.java (Entity)

Hauptentität, die eine Bestellung repräsentiert.

**Wichtige Felder:**
- `id`: Eindeutige UUID
- `itemId`: Referenz zum bestellten Item
- `customerEmail`: E-Mail des Kunden
- `sellerEmail`: E-Mail des Verkäufers
- `quantityOrdered`: Bestellte Menge
- `status`: TransactionStatus (siehe unten)
- `pricePerUnit`: Preis zum Zeitpunkt der Bestellung (Snapshot)
- `totalPrice`: Gesamtbetrag

**Lifecycle Hooks:**
- `@PrePersist`: Setzt `createdAt` und `updatedAt` sowie Standard-Status
- `@PreUpdate`: Aktualisiert `updatedAt`

### 2. TransactionStatus.java (Enum)

Definiert alle möglichen Transaktionsstatus und deren Übergänge.

**Werte:**
```
PENDING   → Neue Bestellung, wartet auf Bestätigung
CONFIRMED → Bestellung bestätigt, Menge reduziert
COMPLETED → Bestellung erfüllt
CANCELLED → Storniert mit evtl. Restitution
REJECTED  → Von Verkäufer abgelehnt
```

**Gültige Übergänge:**
```
PENDING   → CONFIRMED | REJECTED | CANCELLED
CONFIRMED → COMPLETED | CANCELLED
COMPLETED, CANCELLED, REJECTED → (keine Übergänge möglich)
```

**Hilfsmethoden:**
- `isValidTransition(currentStatus, newStatus)`: Validiert Status-Übergänge
- `doesReduceQuantity()`: `true` wenn Status CONFIRMED ist
- `isFinal()`: `true` wenn Status ein Endzustand ist

## Service-Klasse: TransactionService.java

Enthält die gesamte Geschäftslogik für Transaktionen.

### Hauptmethoden

#### 1. `createTransaction(authHeader, requestDTO)`

**Zweck:** Erstellt eine neue Bestellung

**Logik:**
1. Extrahiere Kunden-Email aus JWT Token
2. Validiere dass Item existiert
3. Validiere dass Kunde ≠ Verkäufer
4. Berechne Gesamtpreis: `quantity * price_per_unit`
5. Erstelle Transaction mit Status PENDING
6. Speichere in DB

**Exceptions:**
- `ItemNotFoundException`: Item existiert nicht
- `IllegalArgumentException`: Kunde versucht von sich selbst zu kaufen

**Beispiel-Flow:**
```
POST /api/v1/transactions
Authorization: Bearer <token>
Body: {
  "itemId": "item-123",
  "quantityOrdered": 2.5,
  "notes": "Bitte frisch"
}

Response: 201 Created
{
  "id": "trans-001",
  "status": "PENDING",
  "totalPrice": 12.475,
  ...
}
```

#### 2. `updateTransactionStatus(authHeader, transactionId, statusUpdateDTO)`

**Zweck:** Ändert den Status einer Bestellung

**Logik:**
1. Finde Transaktion
2. Validiere dass nur Verkäufer die Operation durchführt
3. Validiere Status-Übergang
4. Handle Quantity-Änderungen (siehe `handleStatusChange`)
5. Speichere aktualisierte Transaktion

**Quantity-Verwaltung:**

| Status-Übergang | Item.quantity | Regel |
|---|---|---|
| PENDING → CONFIRMED | Reduziert um order.quantity | Reduziere verfügbare Menge |
| CONFIRMED → CANCELLED | Erhöht um order.quantity | Restitution bei Stornierung |
| CONFIRMED → REJECTED | Erhöht um order.quantity | Restitution bei Ablehnung |
| Andere | Keine Änderung | - |

**Exceptions:**
- `TransactionNotFoundException`: Transaktion existiert nicht
- `UnauthorizedException`: Nur Verkäufer darf Status ändern
- `InvalidStatusTransitionException`: Ungültiger Status-Übergang
- `InsufficientQuantityException`: Nicht genug Menge verfügbar

**Beispiel-Flow:**
```
PUT /api/v1/transactions/trans-001/status
Authorization: Bearer <seller-token>
Body: { "status": "CONFIRMED" }

→ Item.quantity wird reduziert
Response: 200 OK
{
  "id": "trans-001",
  "status": "CONFIRMED",
  ...
}
```

#### 3. `getMyOrders(authHeader)`

**Zweck:** Ruft alle Bestellungen des aktuellen Kunden ab

**Logik:**
1. Extrahiere Kunden-Email aus Token
2. Finde alle Transaktionen wo `customerEmail = extracted_email`
3. Returniere als Liste

#### 4. `getMySales(authHeader)`

**Zweck:** Ruft alle Verkäufe des aktuellen Verkäufers ab

**Logik:**
1. Extrahiere Verkäufer-Email aus Token
2. Finde alle Transaktionen wo `sellerEmail = extracted_email`
3. Returniere als Liste

#### 5. `deleteTransaction(authHeader, transactionId)`

**Zweck:** Löscht eine Bestellung

**Einschränkungen:**
- Nur PENDING Transaktionen können gelöscht werden
- Nur Kunde oder Verkäufer können löschen

**Exceptions:**
- `TransactionNotFoundException`
- `UnauthorizedException`
- `IllegalStateException`: Status ist nicht PENDING

## REST Controller: TransactionController.java

### Endpoints

#### 1. POST /api/v1/transactions
Erstellt eine neue Bestellung

**Header:** `Authorization: Bearer <token>`

**Body:**
```json
{
  "itemId": "item-123",
  "quantityOrdered": 2.5,
  "notes": "Optional notes"
}
```

**Response:** 201 Created
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
  "notes": "Optional notes",
  "createdAt": "2025-09-24T10:30:00",
  "updatedAt": "2025-09-24T10:30:00"
}
```

#### 2. GET /api/v1/transactions/{id}
Ruft eine Bestellung anhand ihrer ID ab

**Response:** 200 OK (TransactionResponseDTO)

#### 3. GET /api/v1/transactions/customer/my-orders
Ruft alle Bestellungen des aktuellen Kunden ab

**Response:** 200 OK (Array von TransactionResponseDTO)

#### 4. GET /api/v1/transactions/seller/my-sales
Ruft alle Verkäufe des aktuellen Verkäufers ab

**Response:** 200 OK (Array von TransactionResponseDTO)

#### 5. PUT /api/v1/transactions/{id}/status
Aktualisiert den Status einer Bestellung

**Header:** `Authorization: Bearer <seller-token>`

**Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Response:** 200 OK (TransactionResponseDTO mit neuem Status)

#### 6. DELETE /api/v1/transactions/{id}
Löscht eine Bestellung (nur wenn PENDING)

**Header:** `Authorization: Bearer <token>`

**Response:** 204 No Content

## DTOs

### TransactionRequestDTO
```java
public class TransactionRequestDTO {
    String itemId;              // Required
    Double quantityOrdered;     // Required, > 0
    String notes;               // Optional, max 500 chars
}
```

### TransactionResponseDTO
```java
public class TransactionResponseDTO {
    String id;
    String itemId;
    String customerEmail;
    String sellerEmail;
    Double quantityOrdered;
    TransactionStatus status;
    Double pricePerUnit;
    Double totalPrice;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### TransactionStatusUpdateDTO
```java
public class TransactionStatusUpdateDTO {
    TransactionStatus status;   // Required, must be valid transition
}
```

## Geschäftsregeln & Validierung

### 1. Transaktionserstellung
- ✅ Item muss existieren
- ✅ Menge muss > 0 sein
- ✅ Kunde darf nicht gleich Verkäufer sein
- ✅ Status wird automatisch auf PENDING gesetzt
- ✅ Preis wird als Snapshot zum Zeitpunkt der Bestellung gespeichert

### 2. Status-Updates
- ✅ Nur der Verkäufer darf Status ändern
- ✅ Status-Übergänge müssen gültig sein (siehe TransactionStatus Enum)
- ✅ Bei PENDING → CONFIRMED: Item-Quantity muss >= order.quantity sein
- ✅ Bei CONFIRMED → CANCELLED/REJECTED: Quantity wird zurückgebucht

### 3. Quantity-Management
- ✅ Snapshot des Preises bei Erstellung
- ✅ Quantity-Reduktion erst bei CONFIRMED
- ✅ Restitution bei Stornierung/Ablehnung
- ✅ Atomare DB-Transaktionen mit `@Transactional`

### 4. Sicherheit
- ✅ Nur authentifizierte Benutzer (Bearer Token erforderlich)
- ✅ Kunden können nur ihre eigenen Bestellungen sehen
- ✅ Verkäufer können nur ihre eigenen Verkäufe sehen
- ✅ Nur Verkäufer kann Status ändern
- ✅ Nur Kunde/Verkäufer kann Bestellung löschen

## Repository-Queries

Das `TransactionRepository` stellt folgende Custom Queries zur Verfügung:

```java
// Finde Transaktionen nach Kunde
List<Transaction> findByCustomerEmail(String customerEmail);

// Finde Transaktionen nach Verkäufer
List<Transaction> findBySellerEmail(String sellerEmail);

// Finde Transaktionen nach Item
List<Transaction> findByItemId(String itemId);

// Finde Transaktionen nach Status
List<Transaction> findByStatus(TransactionStatus status);

// Komplexere Queries
List<Transaction> findByItemIdAndStatus(String itemId, TransactionStatus status);
List<Transaction> findByCustomerEmailAndStatus(String customerEmail, TransactionStatus status);
List<Transaction> findBySellerEmailAndStatus(String sellerEmail, TransactionStatus status);

// Prüfe ob aktive Transaktion existiert
boolean existsActiveTransactionForCustomerAndItem(String itemId, String customerEmail);
```

## Exception Handling

### Custom Exceptions

| Exception | HTTP Status | Reason |
|---|---|---|
| `TransactionNotFoundException` | 404 | Transaktion existiert nicht |
| `ItemNotFoundException` | 404 | Item existiert nicht |
| `InsufficientQuantityException` | 409 | Nicht genug Menge verfügbar |
| `InvalidStatusTransitionException` | 400 | Ungültiger Status-Übergang |
| `UnauthorizedException` | 403 | Benutzer nicht berechtigt |
| `IllegalArgumentException` | 400 | Validierungsfehler |
| `IllegalStateException` | 409 | Illegaler Zustand (z.B. nicht PENDING) |

## Logging

Die Service-Klasse nutzt SLF4J für umfassendes Logging auf verschiedenen Ebenen:

- **INFO**: Wichtige Geschäftsvorgänge (Erstellung, Status-Update, Löschung)
- **DEBUG**: Detaillierte Ablauf-Informationen (Email-Extraktion, Queries)
- **WARN**: Verdächtige oder fehlerhafte Vorgänge (ungültige Übergänge)
- **ERROR**: Kritische Fehler (Quantity-Probleme)

**Beispiel Logs:**
```
INFO Creating new transaction for item: item-123
DEBUG Customer email extracted: customer@example.com
DEBUG Seller email: seller@example.com
INFO Transaction created successfully: trans-001 with status PENDING
INFO Updating transaction status: trans-001 to CONFIRMED
INFO Item quantity reduced by 2.5 for transaction trans-001
INFO Transaction status updated successfully: trans-001 -> CONFIRMED
```

## Testing-Strategie

### Unit Tests
```java
@SpringBootTest
public class TransactionServiceTest {
    
    @Test
    public void testCreateTransaction() { }
    
    @Test
    public void testInsufficientQuantity() { }
    
    @Test
    public void testInvalidStatusTransition() { }
    
    @Test
    public void testQuantityReduction() { }
    
    @Test
    public void testUnauthorizedUpdate() { }
}
```

### Integration Tests
```java
@SpringBootTest
public class TransactionControllerIntegrationTest {
    
    @Test
    public void testCreateTransactionEndpoint() { }
    
    @Test
    public void testUpdateStatusEndpoint() { }
    
    @Test
    public void testGetMyOrdersEndpoint() { }
}
```

## Postman Collection

Siehe separates Dokument: `POSTMAN_TRANSACTIONS.json`

## Bekannte Limitationen

1. **Optimistic Locking**: Item.java sollte ein `@Version` Feld haben für Concurrency-Control
2. **Batch Operations**: Keine Bulk-Status-Updates implementiert
3. **Caching**: Keine Query-Result-Caching
4. **Audit Trail**: Nur basic Timestamps, kein detailliertes Audit-Log
5. **Notifications**: Keine automatischen Benachrichtigungen bei Status-Änderungen

## Zukünftige Verbesserungen

- [ ] Event Publishing (Spring Events) für Status-Änderungen
- [ ] Elasticsearch Integration für schnelle Such-Queries
- [ ] Request-ID für End-to-End Tracing
- [ ] Rate Limiting für API-Endpoints
- [ ] Comprehensive Audit Logging
- [ ] Email-Benachrichtigungen
- [ ] Transactional Outbox Pattern für Konsistenz
- [ ] Analytics Dashboard

## Zusammenfassung der Dateien

| Datei | Pfad | Beschreibung |
|---|---|---|
| Transaction.java | `data/transaction/` | Entity mit JPA Annotations |
| TransactionStatus.java | `data/transaction/` | Enum mit Status und Validierung |
| TransactionRepository.java | `data/transaction/` | JPA Repository mit Custom Queries |
| TransactionRequestDTO.java | `rest/v1/dto/` | Request-DTO |
| TransactionResponseDTO.java | `rest/v1/dto/` | Response-DTO |
| TransactionStatusUpdateDTO.java | `rest/v1/dto/` | Status-Update DTO |
| TransactionService.java | `service/` | Business Logic |
| TransactionController.java | `rest/v1/` | REST Controller |
| Exceptions | `exception/` | Custom Exceptions |
| create.sql | Root | DDL für Transactions-Tabelle |

---

**Version:** 1.0
**Status:** Production Ready
**Letzte Aktualisierung:** 2025-02-25
