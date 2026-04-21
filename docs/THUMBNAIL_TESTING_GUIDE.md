# Test-Anleitung: Thumbnail-Feature

## Voraussetzungen
- Foodopia Backend läuft
- PostgreSQL Datenbank ist verfügbar
- `thumbnail_url` Spalte wurde zur `items` Tabelle hinzugefügt

## Test 1: Item mit Bild hochladen

### Request:
```bash
curl -X POST "http://localhost:8080/api/v1/items/with-image" \
  -H "Authorization: Bearer <your_token>" \
  -F "item={\"name\":\"Test Tomatoes\",\"type\":\"Vegetables\",\"price\":5.99,\"quantity\":10,\"unit\":\"kg\",\"street\":\"Test St\",\"city\":\"Test City\",\"zip\":\"12345\",\"description\":\"Test item\"};type=application/json" \
  -F "image=@/path/to/test-image.jpg"
```

### Erwartetes Verhalten:
1. Item wird erstellt
2. `imageUrl` enthält Vollbild (Base64)
3. `thumbnailUrl` enthält 128x128 Thumbnail (Base64)
4. Log: `"Thumbnail generated for item: name=Test Tomatoes"`

### Prüfen in DB:
```sql
SELECT id, name, 
       LENGTH(imageurl) as full_image_size, 
       LENGTH(thumbnail_url) as thumbnail_size 
FROM business_data.items 
WHERE name = 'Test Tomatoes';
```

**Erwartung**: `thumbnail_size` sollte deutlich kleiner sein als `full_image_size`

---

## Test 2: Transaction erstellen und abrufen

### 2.1 Transaction erstellen:
```bash
curl -X POST "http://localhost:8080/api/v1/transactions" \
  -H "Authorization: Bearer <customer_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "<item_id_from_test1>",
    "quantityOrdered": 2.5,
    "notes": "Test order"
  }'
```

### 2.2 Transactions als Kunde abrufen:
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders" \
  -H "Authorization: Bearer <customer_token>"
```

### Erwartete Response:
```json
[
  {
    "id": "trans-xxx",
    "itemId": "item-xxx",
    "item": {
      "id": "item-xxx",
      "name": "Test Tomatoes",
      "type": "Vegetables",
      "price": 5.99,
      "unit": "kg",
      "thumbnailUrl": "data:image/jpeg;base64,/9j/4AAQ..."  // 128x128
    },
    "customerEmail": "customer@example.com",
    "sellerEmail": "seller@example.com",
    "quantityOrdered": 2.5,
    "status": "PENDING",
    "pricePerUnit": 5.99,
    "totalPrice": 14.975,
    ...
  }
]
```

### Prüfpunkte:
- ✅ `item` Objekt ist vorhanden
- ✅ `item.thumbnailUrl` ist vorhanden
- ✅ `thumbnailUrl` ist kürzer als Original-`imageUrl` (nicht in Response)
- ✅ Response-Größe ist deutlich kleiner (< 100 KB für 10 Transactions)

---

## Test 3: Item mit Bild aktualisieren

### Request:
```bash
curl -X PUT "http://localhost:8080/api/v1/items/<item_id>/with-image" \
  -H "Authorization: Bearer <your_token>" \
  -F "item={\"name\":\"Updated Tomatoes\",\"price\":6.99};type=application/json" \
  -F "image=@/path/to/new-image.jpg"
```

### Erwartetes Verhalten:
1. Item wird aktualisiert
2. Neues Thumbnail wird generiert
3. Log: `"Thumbnail generated for item update: id=<item_id>"`

---

## Test 4: Bestehende Items (ohne Thumbnail)

### Items ohne Thumbnail erstellen (Migration-Szenario):
```sql
-- Füge ein Item direkt in DB ohne Thumbnail ein
INSERT INTO business_data.items 
(id, name, type, price, quantity, unit, imageurl, thumbnail_url) 
VALUES 
('old-item-1', 'Old Item', 'Other', 9.99, 5, 'kg', 
 'data:image/jpeg;base64,/9j/4AAQ...', NULL);
```

### Transaction für dieses Item erstellen und abrufen:
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders" \
  -H "Authorization: Bearer <customer_token>"
```

### Erwartetes Verhalten:
- `item.thumbnailUrl` enthält Fallback auf Vollbild (`imageUrl`)
- Keine Fehler, auch wenn `thumbnail_url` NULL ist

---

## Test 5: Performance-Test (10 Transactions)

### 5.1 Erstelle 10 Transactions

### 5.2 Messe Response-Größe:
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/customer/my-orders" \
  -H "Authorization: Bearer <token>" \
  -w "\nResponse Size: %{size_download} bytes\nTime: %{time_total}s\n" \
  -o /dev/null -s
```

### Erwartete Metriken:
- **Mit Thumbnails**: ~50 KB, ~100ms
- **Ohne Thumbnails** (alte Implementierung): ~5 MB+, ~500ms+

### Vergleich:
| Metrik | Vorher | Nachher | Verbesserung |
|--------|--------|---------|--------------|
| Größe | ~5 MB | ~50 KB | **99% kleiner** |
| Zeit | ~500ms | ~100ms | **80% schneller** |

---

## Test 6: Fehlerbehandlung

### 6.1 Upload mit ungültigem Bild:
```bash
curl -X POST "http://localhost:8080/api/v1/items/with-image" \
  -H "Authorization: Bearer <token>" \
  -F "item={...};type=application/json" \
  -F "image=@/path/to/invalid-file.txt"
```

### Erwartetes Verhalten:
- Fehler beim Bild-Verarbeiten
- Item wird NICHT erstellt
- Log: `"errorCode=IMAGE_PROCESSING_ERROR"`

### 6.2 Thumbnail-Generierung schlägt fehl (simuliert):
- Item wird trotzdem gespeichert
- `thumbnailUrl` ist NULL
- Log: `"Failed to generate thumbnail for item: name=..., using fallback"`

---

## Logs überwachen

### Erfolgreiche Thumbnail-Generierung:
```
DEBUG c.f.b.service.ThumbnailService - Generating thumbnail for image, contentType=image/jpeg
DEBUG c.f.b.service.ThumbnailService - Thumbnail generated successfully, size=128x128
DEBUG c.f.b.service.ItemService - Thumbnail generated for item: name=Test Tomatoes
INFO  c.f.b.service.ItemService - Item added successfully: id=item-xxx, seller=seller@example.com
```

### Thumbnail-Fehler (nicht kritisch):
```
WARN  c.f.b.service.ItemService - Failed to generate thumbnail for item: name=Test Item, using fallback
INFO  c.f.b.service.ItemService - Item added successfully: id=item-xxx, seller=seller@example.com
```

---

## Verifizierung

### ✅ Checkliste:
- [ ] Thumbnails werden automatisch beim Upload generiert
- [ ] Thumbnails werden automatisch beim Update generiert
- [ ] Transaction-Responses enthalten `ItemSummaryDTO`
- [ ] `ItemSummaryDTO` enthält Thumbnail
- [ ] Fallback auf Vollbild funktioniert bei NULL-Thumbnails
- [ ] Response-Größe ist deutlich kleiner (< 100 KB für 10 Transactions)
- [ ] Keine Fehler bei bestehenden Items ohne Thumbnail
- [ ] Item-APIs (GET /items) funktionieren weiterhin normal

---

## Troubleshooting

### Problem: Thumbnail wird nicht generiert
**Lösung**: 
- Prüfe Logs auf Fehler
- Stelle sicher, dass `thumbnailator` Dependency geladen wurde
- Prüfe Bildformat (JPEG, PNG, GIF, WEBP werden unterstützt)

### Problem: Transaction-Response enthält kein `item` Objekt
**Lösung**:
- Prüfe, ob ItemMapper injiziert wurde
- Stelle sicher, dass Item in DB existiert
- Prüfe Logs auf `mapToResponseDTO` Fehler

### Problem: Response ist immer noch zu groß
**Lösung**:
- Prüfe, ob `thumbnailUrl` verwendet wird (nicht `imageUrl`)
- Verifiziere Thumbnail-Größe in DB (sollte ~10-20 KB sein)
- Prüfe, ob alter Code noch Vollbilder lädt

---

**Erstellt**: 2026-02-28  
**Version**: 1.0

