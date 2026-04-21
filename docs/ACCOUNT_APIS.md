# Account APIs - Dokumentation

## Übersicht

Die neuen Account APIs ermöglichen umfassende Verwaltung von Benutzerprofilen einschließlich persönlicher Daten, Adresse, Verfügbarkeit und Profilbild.

---

## APIs

### 1. GET /api/v1/account/details

Ruft die kompletten Account-Details des aktuellen Benutzers ab.

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/account/details" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "id": "user-123",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "age": 30,
  "birthDay": "1995-05-15",
  "role": "USER",
  "address": {
    "street": "Main Street 123",
    "city": "Berlin",
    "zip": "10115",
    "lat": 52.5200,
    "lng": 13.4050
  },
  "profilePicture": "data:image/jpeg;base64,...",
  "availabilityDays": "MON,TUE,WED,THU,FRI",
  "availabilityTimes": "09:00-17:00",
  "needConfirmation": false,
  "profileComplete": true,
  "createdAt": "2025-09-24T10:30:00",
  "updatedAt": "2025-09-24T15:45:00"
}
```

**Fehler:**
- **401 Unauthorized**: Token ungültig oder abgelaufen
- **404 Not Found**: Benutzer nicht gefunden

---

### 2. PUT /api/v1/account/details

Aktualisiert die Account-Details des aktuellen Benutzers (ohne Profilbild).

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/v1/account/details" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jonathan",
    "lastName": "Smith",
    "age": 31,
    "birthDay": "1994-03-20",
    "address": {
      "street": "New Street 456",
      "city": "Munich",
      "zip": "80331",
      "lat": 48.1351,
      "lng": 11.5820
    },
    "availabilityDays": "MON,WED,FRI",
    "availabilityTimes": "10:00-18:00",
    "needConfirmation": true
  }'
```

**Request-Felder:**
- `firstName` (optional): Neuer Vorname
- `lastName` (optional): Neuer Nachname
- `age` (optional): Neues Alter
- `birthDay` (optional): Neues Geburtsdatum (ISO-Format: YYYY-MM-DD)
- `address` (optional): Neue Adresse mit allen Feldern
- `availabilityDays` (optional): Verfügbarkeitstage (komma- oder pipe-separiert)
- `availabilityTimes` (optional): Verfügbarkeitszeiten (z.B. "09:00-17:00")
- `needConfirmation` (optional): Bestätigung erforderlich für Buchungen

**Response (200 OK):**
```json
{
  "id": "user-123",
  "email": "john.doe@example.com",
  "firstName": "Jonathan",
  "lastName": "Smith",
  "age": 31,
  "birthDay": "1994-03-20",
  "role": "USER",
  "address": {
    "street": "New Street 456",
    "city": "Munich",
    "zip": "80331",
    "lat": 48.1351,
    "lng": 11.5820
  },
  "profilePicture": "data:image/jpeg;base64,...",
  "availabilityDays": "MON,WED,FRI",
  "availabilityTimes": "10:00-18:00",
  "needConfirmation": true,
  "profileComplete": true,
  "createdAt": "2025-09-24T10:30:00",
  "updatedAt": "2025-09-24T16:00:00"
}
```

**Fehler:**
- **400 Bad Request**: Ungültige Eingabedaten
- **401 Unauthorized**: Token ungültig
- **404 Not Found**: Benutzer nicht gefunden

---

### 3. POST /api/v1/account/image

Aktualisiert das Profilbild des aktuellen Benutzers.

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/account/image" \
  -H "Authorization: Bearer <token>" \
  -F "image=@/path/to/profile-picture.jpg"
```

**Unterstützte Bildformate:**
- JPEG
- PNG
- GIF
- WebP

**Response (200 OK):**
```json
{
  "id": "user-123",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "age": 30,
  "birthDay": "1995-05-15",
  "role": "USER",
  "address": {
    "street": "Main Street 123",
    "city": "Berlin",
    "zip": "10115",
    "lat": 52.5200,
    "lng": 13.4050
  },
  "profilePicture": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "availabilityDays": "MON,TUE,WED,THU,FRI",
  "availabilityTimes": "09:00-17:00",
  "needConfirmation": false,
  "profileComplete": true,
  "createdAt": "2025-09-24T10:30:00",
  "updatedAt": "2025-09-24T16:05:00"
}
```

**Fehler:**
- **400 Bad Request**: Ungültiges Bildformat oder Verarbeitungsfehler
- **401 Unauthorized**: Token ungültig
- **404 Not Found**: Benutzer nicht gefunden

---

### 4. DELETE /api/v1/account

Löscht das Konto des aktuellen Benutzers.

**WARNUNG:** Diese Operation ist **irreversibel**!

**Request:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/account" \
  -H "Authorization: Bearer <token>"
```

**Response (204 No Content):**
```
(Kein Response Body)
```

**Fehler:**
- **401 Unauthorized**: Token ungültig
- **404 Not Found**: Benutzer nicht gefunden

---

## DTOs

### AccountDetailsDTO

Vollständige Kontodetails des Benutzers:

```java
{
  "id": "user-123",                           // Benutzer-ID
  "email": "john.doe@example.com",           // E-Mail
  "firstName": "John",                        // Vorname
  "lastName": "Doe",                          // Nachname
  "age": 30,                                  // Alter
  "birthDay": "1995-05-15",                   // Geburtsdatum
  "role": "USER",                             // Benutzerrolle
  "address": { ... },                         // Adresse
  "profilePicture": "data:image/jpeg;...",   // Profilbild (Base64)
  "availabilityDays": "MON,TUE,WED,THU,FRI", // Verfügbarkeitstage
  "availabilityTimes": "09:00-17:00",        // Verfügbarkeitszeiten
  "needConfirmation": false,                  // Bestätigung erforderlich
  "profileComplete": true,                    // Profil vollständig
  "createdAt": "2025-09-24T10:30:00",        // Erstellungszeit
  "updatedAt": "2025-09-24T15:45:00"         // Letzte Aktualisierung
}
```

### UpdateAccountDetailsDTO

Felder für Account-Update (alle optional):

```java
{
  "firstName": "Jonathan",                    // Neuer Vorname
  "lastName": "Smith",                        // Neuer Nachname
  "age": 31,                                  // Neues Alter
  "birthDay": "1994-03-20",                   // Neues Geburtsdatum
  "address": { ... },                         // Neue Adresse
  "availabilityDays": "MON,WED,FRI",          // Neue Verfügbarkeitstage
  "availabilityTimes": "10:00-18:00",         // Neue Verfügbarkeitszeiten
  "needConfirmation": true                    // Neue Einstellung
}
```

### AddressDto

```java
{
  "street": "Main Street 123",                // Straße
  "city": "Berlin",                           // Stadt
  "zip": "10115",                             // Postleitzahl
  "lat": 52.5200,                             // Breitengrad
  "lng": 13.4050                              // Längengrad
}
```

---

## Verfügbarkeitskonfiguration

### Verfügbarkeitstage (availability_days)

Format: Komma- oder pipe-separierte Tage

**Beispiele:**
```
"MON,TUE,WED,THU,FRI"     // Werktage
"MON|WED|FRI"              // Montag, Mittwoch, Freitag
"MON,TUE,WED,THU,FRI,SAT" // Montag bis Samstag
```

**Unterstützte Werte:**
- `MON` - Montag
- `TUE` - Dienstag
- `WED` - Mittwoch
- `THU` - Donnerstag
- `FRI` - Freitag
- `SAT` - Samstag
- `SUN` - Sonntag

### Verfügbarkeitszeiten (availability_times)

Format: Zeitspannen im Format HH:MM-HH:MM, mehrere Spannen pipe-separiert

**Beispiele:**
```
"09:00-17:00"              // 9 Uhr bis 17 Uhr
"09:00-12:00|14:00-18:00"  // Zwei Zeitfenster mit Mittagspause
```

---

## Profilbild (profilePicture)

Das Profilbild wird als Base64-kodiertes Bild gespeichert und zurückgegeben:

**Format:** `data:image/jpeg;base64,<base64-encoded-image>`

**Beispiel:**
```
data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8VAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k
```

---

## Error Handling

Alle APIs verwenden standardisierte HTTP Status-Codes:

| Status | Beschreibung |
|--------|-------------|
| 200 | Erfolgreiche GET/PUT Operation |
| 204 | Erfolgreiche DELETE Operation (kein Content) |
| 400 | Bad Request - Ungültige Eingabedaten |
| 401 | Unauthorized - Token ungültig oder fehlt |
| 404 | Not Found - Benutzer nicht gefunden |
| 500 | Internal Server Error |

---

## Sicherheit

- Alle APIs erfordern einen gültigen JWT Bearer Token im `Authorization` Header
- Benutzer können nur ihre eigenen Daten abrufen und ändern
- Die DELETE Operation löscht das gesamte Benutzerkonto unwiederbringlich

---

## Beispiel-Workflow

### 1. Benutzer ruft Profildaten ab
```bash
curl -X GET "http://localhost:8080/api/v1/account/details" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 2. Benutzer aktualisiert Profilinformationen
```bash
curl -X PUT "http://localhost:8080/api/v1/account/details" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "availabilityDays": "MON,WED,FRI",
    "availabilityTimes": "10:00-18:00"
  }'
```

### 3. Benutzer lädt Profilbild hoch
```bash
curl -X POST "http://localhost:8080/api/v1/account/image" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -F "image=@profile.jpg"
```

### 4. Benutzer löscht Konto (Vorsicht!)
```bash
curl -X DELETE "http://localhost:8080/api/v1/account" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

**Version**: 1.0  
**Datum**: 2026-02-28

