package com.foodopia.backend.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO für Anfragen zum Aktualisieren von Account-Details (ohne Bild)
 * Enthält alle Felder, die der Benutzer aktualisieren kann, ausgenommen das Profilbild
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountDetailsDTO {

    /**
     * Vorname des Benutzers
     */
    @Schema(description = "Vorname", example = "John")
    private String firstName;

    /**
     * Nachname des Benutzers
     */
    @Schema(description = "Nachname", example = "Doe")
    private String lastName;

    /**
     * Alter des Benutzers
     */
    @Schema(description = "Alter", example = "30")
    private Integer age;

    /**
     * Geburtsdatum des Benutzers
     */
    @Schema(description = "Geburtsdatum (ISO-Format: YYYY-MM-DD)", example = "1995-05-15")
    private String birthDay;

    /**
     * Adresse des Benutzers
     */
    @Schema(description = "Adresse des Benutzers")
    private AddressDto address;

    /**
     * Verfügbarkeitstage (komma-separiert oder pipe-separiert)
     * Beispiel: "MON,TUE,WED,THU,FRI" oder "MON|WED|FRI"
     */
    @Schema(description = "Verfügbarkeitstage", example = "MON,TUE,WED,THU,FRI")
    private String availabilityDays;

    /**
     * Verfügbarkeitszeiten (mehrere Zeitspannen, pipe-separiert)
     * Beispiel: "09:00-17:00" oder "09:00-12:00|14:00-18:00"
     */
    @Schema(description = "Verfügbarkeitszeiten", example = "09:00-17:00")
    private String availabilityTimes;

    /**
     * Bestätigung erforderlich für Buchungen
     */
    @Schema(description = "Bestätigung für Buchungen erforderlich", example = "false")
    private Boolean needConfirmation;
}

