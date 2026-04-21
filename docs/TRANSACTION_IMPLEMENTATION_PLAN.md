# Transaktionen (Orders) - Implementierungsplan

## 1. Übersicht

Dieses Dokument beschreibt die Implementierung eines vollständigen Transaction-Management-Systems für das Foodopia-Backend. Transaktionen repräsentieren Bestellungen von Kunden bei Verkäufern für konkrete Angebote (Items).

## 2. Zielarchitektur

```
API Controller
    ↓
REST DTO (Request/Response)
    ↓
Service Layer
    ↓
Repository (JPA)
    ↓
Database (PostgreSQL)
```

## 3. Datenmodell

### 3.1 Transaction Entity

**Entität**: `Transaction`
**Schema**: `business_data`
**Tabelle**: `transactions`

#### Attribute:
- `id` (VARCHAR, PK): Eindeutige Transaktions-ID (UUID)
- `item_id` (VARCHAR, FK): Referenz zur Angebots-ID
- `customer_email` (VARCHAR): E-Mail des Kunden (eindeutige Kennung)
- `seller_email` (VARCHAR): E-Mail des Verkäufers (eindeutige Kennung)
- `quantity_ordered` (DOUBLE PRECISION): Bestellmenge
- `status` (VARCHAR): Transaktionsstatus (PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED)
- `price_per_unit` (DOUBLE PRECISION): Preis pro Einheit zum Zeitpunkt der Bestellung
- `total_price` (DOUBLE PRECISION): Gesamtpreis (quantity_ordered * price_per_unit)
- `created_at` (TIMESTAMP): Erstellungszeitpunkt
- `updated_at` (TIMESTAMP): Letzter Aktualisierungszeitpunkt
- `notes` (TEXT): Optionale Notizen/Kommentare

### 3.2 Transaction Status Enum

```
PENDING      → Neue Bestellung, wartet auf Bestätigung
CONFIRMED    → Bestellung bestätigt, Menge reduziert im Item
COMPLETED    → Bestellung erfüllt/abgeschlossen
CANCELLED    → Von Kunden oder System storniert
REJECTED     → Von Verkäufer abgelehnt
```

## 4. Geschäftslogik-Regeln

### 4.1 Status-Übergänge

```
PENDING → CONFIRMED (Verkäufer akzeptiert)
PENDING → REJECTED (Verkäufer lehnt ab)
PENDING → CANCELLED (Kunde oder System storniert)
CONFIRMED → COMPLETED (Bestellung erfüllt)
CONFIRMED → CANCELLED (Kunde oder Verkäufer storniert mit Restitution)
```

### 4.2 Quantity-Management

**Regel 1: Status → CONFIRMED**
- Prüfe: Item.quantity >= transaction.quantity_ordered
- Wenn OK: Item.quantity -= transaction.quantity_ordered
- Wenn Fehler: Transaktion bleibt PENDING, Fehler zurückgeben

**Regel 2: Status CONFIRMED → Anderer Status (außer COMPLETED)**
- Prüfe: Die reduzierte Menge muss zurückgebucht werden
- Item.quantity += transaction.quantity_ordered (wenn CANCELLED oder REJECTED)
- Bei COMPLETED: Keine Änderung, Verkauf abgeschlossen

**Regel 3: Concurrent Updates**
- Nutze Optimistic Locking (Version-Feld) auf Item-Entity
- Verhindert Race Conditions bei gleichzeitigen Transaktionen

## 5. Implementierungs-Schritte

### Phase 1: Datenbank-Schema (create.sql)
- [ ] Neue Tabelle `transactions` erstellen
- [ ] Foreign Key zu `items` (item_id)
- [ ] Indizes für häufige Abfragen (customer_email, seller_email, item_id, status)

### Phase 2: Entity & JPA Repository
- [ ] `Transaction.java` Entity-Klasse mit Annotations
- [ ] `TransactionRepository.java` mit Custom Queries:
  - findByCustomerEmail(String email)
  - findBySellerEmail(String email)
  - findByItemId(String itemId)
  - findByStatus(TransactionStatus status)
  - findById(String id)

### Phase 3: DTO Layer
- [ ] `TransactionRequestDTO.java` (POST/PUT Input)
- [ ] `TransactionResponseDTO.java` (GET Output)
- [ ] Mapper zwischen Entity und DTOs

### Phase 4: Service Layer
- [ ] `TransactionService.java` mit Geschäftslogik:
  - `createTransaction(authHeader, TransactionRequestDTO)` → TransactionResponseDTO
  - `updateTransactionStatus(authHeader, transactionId, newStatus)` → TransactionResponseDTO
  - `getTransactionById(String id)` → TransactionResponseDTO
  - `getTransactionsByCustomer(authHeader)` → List<TransactionResponseDTO>
  - `getTransactionsBySeller(authHeader)` → List<TransactionResponseDTO>
  - `deleteTransaction(String id)` → void
  - Private Helper: `handleStatusChange(transaction, oldStatus, newStatus)`
  - Private Helper: `updateItemQuantity(itemId, quantityDelta)`
  - Private Helper: `extractEmailFromToken(authHeader)`

### Phase 5: REST Controller
- [ ] `TransactionController.java` mit Endpoints:
  - `POST /api/v1/transactions` → addTransaction
  - `GET /api/v1/transactions/{id}` → getTransactionById
  - `GET /api/v1/transactions/customer/my-orders` → getMyOrders
  - `GET /api/v1/transactions/seller/my-sales` → getMySales
  - `PUT /api/v1/transactions/{id}/status` → updateStatus
  - `DELETE /api/v1/transactions/{id}` → deleteTransaction

### Phase 6: Error Handling & Logging
- [ ] Custom Exceptions (TransactionNotFoundException, InvalidQuantityException, etc.)
- [ ] Request-ID für Logging (MDC)
- [ ] Standardisierte Error-Response DTO
- [ ] Audit Logging für Status-Änderungen

### Phase 7: Tests & Validierung
- [ ] Unit Tests für Service-Logik
- [ ] Integration Tests für DB-Operationen
- [ ] API Tests mit MockMvc
- [ ] Test-Szenarien: Quantity-Reduktion, Status-Übergänge, Fehler-Fälle

## 6. API-Spezifikation

### 6.1 POST /api/v1/transactions (Neue Bestellung)

**Request:**
```json
{
  "itemId": "item-123",
  "quantityOrdered": 2.5,
  "notes": "Bitte frisch verpacken"
}
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
  "createdAt": "2025-09-24T10:30:00Z",
  "updatedAt": "2025-09-24T10:30:00Z",
  "notes": "Bitte frisch verpacken"
}
```

### 6.2 PUT /api/v1/transactions/{id}/status (Status aktualisieren)

**Request:**
```json
{
  "status": "CONFIRMED"
}
```

**Response (200 OK):**
```json
{
  "id": "trans-001",
  "status": "CONFIRMED",
  "updatedAt": "2025-09-24T10:35:00Z",
  ...
}
```

### 6.3 GET /api/v1/transactions/customer/my-orders (Meine Bestellungen)

**Response (200 OK):**
```json
[
  {
    "id": "trans-001",
    "itemId": "item-123",
    "sellerEmail": "seller@example.com",
    "quantityOrdered": 2.5,
    "status": "CONFIRMED",
    ...
  }
]
```

### 6.4 GET /api/v1/transactions/seller/my-sales (Meine Verkäufe)

**Response (200 OK):**
```json
[
  {
    "id": "trans-001",
    "itemId": "item-123",
    "customerEmail": "customer@example.com",
    "quantityOrdered": 2.5,
    "status": "CONFIRMED",
    ...
  }
]
```

### 6.5 GET /api/v1/transactions/{id} (Bestellung abrufen)

**Response (200 OK):** Transaction-Objekt

### 6.6 DELETE /api/v1/transactions/{id} (Bestellung löschen)

**Response (204 No Content)**

## 7. Datenbankmigrationen

### create.sql Änderungen:

```sql
-- Transactions-Tabelle
CREATE TABLE IF NOT EXISTS business_data.transactions (
    id VARCHAR(255) PRIMARY KEY,
    item_id VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    seller_email VARCHAR(255) NOT NULL,
    quantity_ordered DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    price_per_unit DOUBLE PRECISION NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (item_id) REFERENCES business_data.items(id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED'))
);

-- Indizes für Performance
CREATE INDEX IF NOT EXISTS idx_transactions_customer ON business_data.transactions(customer_email);
CREATE INDEX IF NOT EXISTS idx_transactions_seller ON business_data.transactions(seller_email);
CREATE INDEX IF NOT EXISTS idx_transactions_item ON business_data.transactions(item_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON business_data.transactions(status);
```

### Item-Tabelle Anpassungen:

```sql
-- Version-Feld für Optimistic Locking hinzufügen (falls nicht vorhanden)
ALTER TABLE business_data.items
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
```

## 8. Fehlerbehandlung

### Exceptions:

- `TransactionNotFoundException` → 404
- `InvalidQuantityException` → 400
- `InsufficientQuantityException` → 409
- `InvalidStatusTransitionException` → 400
- `UnauthorizedException` → 403
- `ItemNotFoundException` → 404

### Error Response Format:

```json
{
  "errorMessage": "Insufficient quantity available",
  "errorCode": "ERR_INSUFFICIENT_QTY",
  "errorUUID": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-09-24T10:30:00Z"
}
```

## 9. Logging-Strategie

- Request-ID in MDC für Request-Tracing
- Separate Logs für Quantity-Änderungen
- Audit-Log für Status-Übergänge
- Error-Logs mit ErrorCode, nicht vollständigem Stack (nur mit Debug-Flag)

## 10. Testing-Strategie

### Unit Tests:
- Service-Methoden mit Mock-Repository
- Quantity-Logik bei Status-Änderungen
- Status-Validierung

### Integration Tests:
- DB-Operationen mit Test-DB
- Transaction-Rollback bei Fehlern
- Concurrent Updates

### API Tests:
- MockMvc für Endpoint-Tests
- Authorization Tests (nur Kunden/Verkäufer können ihre eigenen Transaktionen sehen)
- Error-Response Validierung

## 11. Sicherheit & Berechtigungen

- **Kunden**: Können nur ihre eigenen Bestellungen sehen/löschen
- **Verkäufer**: Können nur ihre eigenen Verkäufe sehen und Status ändern
- **Admin**: Vollzugriff
- Validation: E-Mail aus Token muss mit customer_email oder seller_email übereinstimmen

## 12. Implementierungs-Reihenfolge

1. ✅ Dieses Plan-Dokument erstellen
2. ⏳ SQL-Schema in create.sql hinzufügen
3. ⏳ Transaction Entity erstellen
4. ⏳ TransactionRepository erstellen
5. ⏳ TransactionRequestDTO und TransactionResponseDTO erstellen
6. ⏳ TransactionService mit Geschäftslogik erstellen
7. ⏳ TransactionController mit REST-Endpoints erstellen
8. ⏳ Exception-Handling implementieren
9. ⏳ Tests schreiben
10. ⏳ Dokumentation (Postman Collection, Swagger)

---

**Status**: Draft - Bereit zur Implementierung in Phasen
**Letzte Aktualisierung**: 2025-02-25
