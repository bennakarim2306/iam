# Transactions Implementation - Schnellstart

## 🚀 Erste Schritte

### 1. Projekt Kompilieren

```bash
cd C:\Users\benna\Documents\GitHub\foodopia-backend
mvn clean compile
```

**Erwartet:** Keine Fehler, erfolgreiche Kompilierung

### 2. Unit Tests für TransactionService erstellen

Optionaler Test-File (`TransactionServiceTest.java`):

```bash
src/test/java/com/foodopia/backend/service/TransactionServiceTest.java
```

### 3. Datenbank-Migration durchführen

Die `create.sql` wird automatisch beim Start der Anwendung ausgeführt (via Spring Boot DDL-Auto).

**Oder manuell in PostgreSQL:**
```sql
-- Stelle sicher dass die Datenbank existiert
SELECT * FROM information_schema.tables 
WHERE table_schema = 'business_data' AND table_name = 'transactions';
```

### 4. Spring Boot Anwendung starten

```bash
mvn spring-boot:run
```

**Oder in IDE:** `UserManagerApplication.java` → Run

### 5. API Testen mit Postman

1. Postman öffnen
2. `File` → `Import` → `POSTMAN_TRANSACTIONS.json` auswählen
3. Environment Variables setzen:
   - `base_url`: `http://localhost:8080`
   - `customer_token`: Gültiger JWT Token
   - `seller_token`: Gültiger JWT Token

4. Requests durchlaufen

---

## 📋 Checkliste vor Production Deployment

- [ ] Alle Unit Tests grün (mvn test)
- [ ] Alle Integration Tests grün (mvn verify)
- [ ] Code Review durchgeführt
- [ ] Security Audit durchgeführt
- [ ] Performance Tests durchgeführt
- [ ] Logging validiert
- [ ] Exception Handling getestet
- [ ] Database Backup Strategy definiert
- [ ] Monitoring & Alerting konfiguriert

---

## 🔧 Häufig verwendete Befehle

### Kompilierung & Build
```bash
# Clean Build
mvn clean package

# Skip Tests
mvn clean package -DskipTests

# Run Tests
mvn test

# Run nur spezifische Test-Klasse
mvn test -Dtest=TransactionServiceTest
```

### Spring Boot
```bash
# Run mit Spring Boot Maven Plugin
mvn spring-boot:run

# Run mit Debug-Mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# Run mit aktuellem Profil
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=cloud-dev"
```

### Logging
```bash
# Logs in Konsole verfolgen (für lokales Testing)
tail -f target/logs/app.log

# Nur ERROR-Level anzeigen
grep ERROR target/logs/app.log
```

---

## 🧪 Schnell-Test: Kompletter Workflow

### Setup
```bash
# Umgebungsvariablen setzen (PowerShell)
$BASE_URL = "http://localhost:8080"
$CUSTOMER_TOKEN = "your_customer_jwt_here"
$SELLER_TOKEN = "your_seller_jwt_here"
```

### Test 1: Bestellung erstellen
```bash
$response = Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions" `
  -Method POST `
  -Headers @{"Authorization"="Bearer $CUSTOMER_TOKEN"} `
  -ContentType "application/json" `
  -Body '{"itemId":"item-123","quantityOrdered":2.5,"notes":"Test"}'

$transactionId = ($response.Content | ConvertFrom-Json).id
Write-Host "Transaction created: $transactionId"
```

### Test 2: Bestellung abrufen
```bash
Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions/$transactionId" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $CUSTOMER_TOKEN"}
```

### Test 3: Status aktualisieren (Verkäufer)
```bash
Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions/$transactionId/status" `
  -Method PUT `
  -Headers @{"Authorization"="Bearer $SELLER_TOKEN"} `
  -ContentType "application/json" `
  -Body '{"status":"CONFIRMED"}'
```

### Test 4: Meine Bestellungen abrufen (Kunde)
```bash
Invoke-WebRequest -Uri "$BASE_URL/api/v1/transactions/customer/my-orders" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $CUSTOMER_TOKEN"}
```

---

## 🐛 Troubleshooting

### Problem: "Column 'transactions.id' not found"
**Lösung:** Die Datenbank wurde nicht aktualisiert
```bash
# Manuelle Migration durchführen
psql -U postgres -d foodopia -f create.sql
```

### Problem: "No translator was found for method equals"
**Lösung:** JPA Query-Syntax Fehler
```java
// Falsch: Enum direkt vergleichen
@Query("SELECT t FROM transactions t WHERE t.status = :status")

// Richtig: String oder Enum mit @Enumerated
@Query("SELECT t FROM transactions t WHERE t.status = :status")
List<Transaction> findByStatus(@Param("status") TransactionStatus status);
```

### Problem: "401 Unauthorized"
**Lösung:** JWT Token ist ungültig oder abgelaufen
- Neuen Token durch Login generieren
- Token in Authorization Header setzten: `Bearer <token>`

### Problem: "403 Forbidden"
**Lösung:** Benutzer hat keine Berechtigung
- Nur Verkäufer kann Status ändern
- Email im Token muss mit seller_email übereinstimmen

### Problem: "409 Insufficient Quantity"
**Lösung:** Item-Menge reicht nicht aus
- Bestellmenge > verfügbare Item-Menge
- Item mit mehr Menge auswählen

---

## 📊 Performance-Tipps

### Database Indizes verwenden
```sql
-- Schnelle Abfragen nach Kunde
CREATE INDEX idx_transactions_customer_email 
ON business_data.transactions(customer_email);

-- Schnelle Abfragen nach Status
CREATE INDEX idx_transactions_status 
ON business_data.transactions(status);
```

### Pagination implementieren (Zukunft)
```java
// Für große Listen
@GetMapping("/customer/my-orders")
public Page<TransactionResponseDTO> getMyOrders(
    @RequestHeader("Authorization") String authHeader,
    @PageableDefault(size = 20) Pageable pageable
)
```

### Caching (Zukunft)
```java
@Cacheable(value = "transactions", key = "#id")
public TransactionResponseDTO getTransactionById(String id)
```

---

## 🔐 Sicherheits-Checkliste

- ✅ JWT Bearer Token erforderlich
- ✅ CORS konfiguriert
- ✅ SQL Injection Protection (via JPA)
- ✅ Role-based Access Control
- ✅ Email-Validation
- ✅ Input Validation (@Valid, @NotNull, etc.)
- ✅ HTTPS in Production empfohlen
- ✅ Sensitive Daten nicht in Logs

---

## 📞 Support & Dokumentation

**Vollständige Dokumentation:**
1. `TRANSACTIONS_IMPLEMENTATION_PLAN.md` - Detaillierter Plan
2. `TRANSACTIONS_COMPLETE_GUIDE.md` - Entwickler-Guide
3. `TRANSACTIONS_CURL_EXAMPLES.md` - API-Beispiele
4. `TRANSACTIONS_IMPLEMENTATION_SUMMARY.md` - Diese Datei

**API-Dokumentation (Swagger UI):**
```
http://localhost:8080/swagger-ui.html
```

**Postman Collection:**
```
POSTMAN_TRANSACTIONS.json
```

---

## ✨ Nächste Schritte nach Deployment

1. **Monitoring einrichten**
   - Application Insights / Datadog
   - Error Tracking (Sentry)
   - Performance Monitoring

2. **Alerting konfigurieren**
   - High error rate alerts
   - Quantity conflicts alerts
   - Database connection alerts

3. **Backup-Strategie**
   - Tägliche DB Backups
   - Transaction Log Backups
   - Disaster Recovery Plan

4. **Skalierung vorbereiten**
   - Load Testing durchführen
   - Database Connection Pool optimieren
   - Caching-Strategie implementieren

---

**Status**: Ready to Deploy
**Version**: 1.0
**Letzte Aktualisierung**: 2025-02-25
