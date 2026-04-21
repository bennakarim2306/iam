# DTO-Refactoring: Trennung von API-Layer und Datenbankschicht

## Problem
Die `Item`-Entity wurde direkt als DTO verwendet, was zu Problemen führte:
- Adressdaten kamen als flache Felder (`street`, `city`, `zip`) vom Client
- Die Entity erwartet ein eingebettetes `Address`-Objekt
- Resultat: Adressdaten wurden nicht in der DB gespeichert

## Lösung
Separate DTOs für API-Requests und -Responses eingeführt:

### 1. Neue Dateien

#### ItemRequestDTO.java
- Flache Struktur für eingehende Requests
- Felder: `street`, `city`, `zip`, `lat`, `lng` (direkt, nicht eingebettet)
- Seller-Felder: `sellerId`, `sellerName`, `sellerContact`
- Datums-Felder als String (Format: "yyyy-MM-dd")

#### ItemResponseDTO.java
- Verschachtelte Struktur für ausgehende Responses
- Enthält `AddressDTO` und `SellerDTO` als innere Klassen
- Bessere API-Klarheit und Struktur

#### ItemMapper.java
- Konvertiert zwischen DTOs und Entity
- Methoden:
  - `toEntity(ItemRequestDTO)` - DTO → Entity
  - `toResponseDTO(Item)` - Entity → DTO
  - `updateEntityFromDTO(ItemRequestDTO, Item)` - Update einer Entity

### 2. Aktualisierte Dateien

#### ItemController.java
- Verwendet jetzt `ItemRequestDTO` für Eingaben
- Verwendet `ItemResponseDTO` für Ausgaben
- Keine direkten Entity-Referenzen mehr

#### ItemService.java
- Alle Methoden arbeiten mit DTOs
- Verwendet `ItemMapper` für Konvertierungen
- Entity-Logik bleibt in der Service-Schicht

#### Item.java (Entity)
- `description` und `imageUrl` als `TEXT` definiert (für Base64-Bilder)
- Ermöglicht das Speichern großer Base64-kodierter Bilder

### 3. Datenbank-Migration

#### migrate_image_column.sql
- Ändert `imageurl` und `description` auf `TEXT`
- Erforderlich für bestehende Datenbanken

## Base64-Bildgröße

Bei Base64-Kodierung werden Bilder ca. **33% größer**:
- **100 KB Bild** → ca. 140.000 Zeichen
- **500 KB Bild** → ca. 700.000 Zeichen
- **1 MB Bild** → ca. 1.400.000 Zeichen

Der Typ `TEXT` in PostgreSQL unterstützt bis zu **1 GB**, was ausreichend ist.

## Vorteile

1. **Klare Trennung**: API-Layer und Datenbankschicht sind getrennt
2. **Flexibilität**: DTOs können sich unabhängig von der Entity ändern
3. **Validierung**: Einfachere Validierung auf DTO-Ebene
4. **Dokumentation**: Klarere API-Struktur für Clients
5. **Wartbarkeit**: Änderungen an der DB-Struktur betreffen nicht die API

## Migration für bestehende Daten

Falls bereits Daten in der DB existieren:

```sql
-- Migration ausführen
psql -h <host> -U <user> -d <database> -f migrate_image_column.sql
```

## Testing

Test mit dem ursprünglichen Request:
```json
{
  "formData": {
    "_parts": [
      [
        "item",
        "{\"name\":\"Hund\",\"type\":\"Other\",\"price\":\"1\",\"quantity\":\"1\",\"unit\":\"kg\",\"street\":\"Rua dos acores\",\"city\":\"Alvor\",\"zip\":\"8550\",\"description\":\"\"}"
      ],
      [
        "image",
        {
          "uri": "...",
          "name": "741d19ea-3a90-4149-be37-5399865178ed.jpeg",
          "type": "image/jpeg"
        }
      ]
    ]
  }
}
```

Jetzt werden `street`, `city` und `zip` korrekt als `address_street`, `address_city` und `address_zip` in der DB gespeichert.
