package com.foodopia.backend.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO für Account-Details Response
 * Enthält alle relevanten Informationen des Benutzerkontos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDetailsDTO {

    /**
     * Eindeutige Benutzer-ID
     */
    @Schema(description = "Benutzer-ID", example = "user-123")
    private String id;

    /**
     * E-Mail-Adresse (eindeutig)
     */
    @Schema(description = "E-Mail-Adresse", example = "john.doe@example.com")
    private String email;

    /**
     * Vorname
     */
    @Schema(description = "Vorname", example = "John")
    private String firstName;

    /**
     * Nachname
     */
    @Schema(description = "Nachname", example = "Doe")
    private String lastName;

    /**
     * Alter
     */
    @Schema(description = "Alter", example = "30")
    private Integer age;

    /**
     * Geburtsdatum
     */
    @Schema(description = "Geburtsdatum (ISO-Format: YYYY-MM-DD)", example = "1995-05-15")
    private LocalDate birthDay;

    /**
     * Benutzerrolle
     */
    @Schema(description = "Benutzerrolle", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    /**
     * Adresse des Benutzers
     */
    @Schema(description = "Adresse des Benutzers")
    private AddressDto address;

    /**
     * Profilbild (Base64 kodiert)
     * Kann null sein wenn kein Bild gespeichert ist
     */
    @Schema(description = "Profilbild (Base64 kodiert)")
    private String profilePicture;

    /**
     * Verfügbarkeitstage
     */
    @Schema(description = "Verfügbarkeitstage", example = "MON,TUE,WED,THU,FRI")
    private String availabilityDays;

    /**
     * Verfügbarkeitszeiten
     */
    @Schema(description = "Verfügbarkeitszeiten", example = "09:00-17:00")
    private String availabilityTimes;

    /**
     * Bestätigung erforderlich für Buchungen
     */
    @Schema(description = "Bestätigung für Buchungen erforderlich", example = "false")
    private Boolean needConfirmation;

    /**
     * Profilstatus (vollständig/unvollständig)
     */
    @Schema(description = "Profil ist vollständig", example = "true")
    private Boolean profileComplete;

    /**
     * Zeitstempel der Erstellung
     */
    @Schema(description = "Erstellungszeitpunkt", example = "2025-09-24T10:30:00")
    private String createdAt;

    /**
     * Zeitstempel der letzten Aktualisierung
     */
    @Schema(description = "Letzte Aktualisierung", example = "2025-09-24T15:45:00")
    private String updatedAt;
}

