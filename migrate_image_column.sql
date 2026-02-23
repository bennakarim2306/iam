-- Migration: Aktualisiere imageurl und description auf TEXT für große Base64-Bilder
-- Dies ermöglicht das Speichern von Base64-kodierten Bildern in der Datenbank

-- Ändere die Spaltentypen auf TEXT
ALTER TABLE business_data.items
    ALTER COLUMN imageurl TYPE TEXT,
    ALTER COLUMN description TYPE TEXT;
