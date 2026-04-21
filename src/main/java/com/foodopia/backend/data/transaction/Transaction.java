package com.foodopia.backend.data.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction Entity - Repräsentiert eine Bestellung (Order) zwischen Kunde und Verkäufer
 * für ein spezifisches Angebot (Item).
 *
 * Geschäftsregeln:
 * - Jede Transaktion ist gekoppelt an ein Item (Angebot)
 * - Transaktionen haben einen Status (PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED)
 * - Bei Status-Wechsel zu CONFIRMED wird die Menge im Item reduziert
 * - Bei Status-Wechsel von CONFIRMED zu anderen Status wird die Menge zurückgebucht
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "transactions")
@Table(name = "transactions", schema = "business_data")
public class Transaction {

    /**
     * Eindeutige Transaktions-ID (UUID)
     */
    @Id
    private String id;

    /**
     * Referenz zum Angebot (Item)
     */
    @Column(name = "item_id", nullable = false)
    private String itemId;

    /**
     * E-Mail des Kunden (eindeutige Kennung)
     */
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /**
     * E-Mail des Verkäufers (eindeutige Kennung)
     */
    @Column(name = "seller_email", nullable = false)
    private String sellerEmail;

    /**
     * Bestellmenge
     */
    @Column(name = "quantity_ordered", nullable = false)
    private double quantityOrdered;

    /**
     * Preis pro Einheit zum Zeitpunkt der Bestellung (Snapshot)
     */
    @Column(name = "price_per_unit", nullable = false)
    private double pricePerUnit;

    /**
     * Gesamtpreis (quantityOrdered * pricePerUnit)
     */
    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    /**
     * Transaktionsstatus
     * PENDING    → Neue Bestellung, wartet auf Bestätigung
     * CONFIRMED  → Bestellung bestätigt, Menge in Item reduziert
     * COMPLETED  → Bestellung erfüllt/abgeschlossen
     * CANCELLED  → Storniert mit Restitution der Menge
     * REJECTED   → Von Verkäufer abgelehnt
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    /**
     * Optionale Notizen/Kommentare zur Transaktion
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Zeitstempel: Erstellung der Transaktion
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Zeitstempel: Letzte Aktualisierung
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Pre-Persist Hook - Wird aufgerufen, bevor die Entity in die DB eingefügt wird
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    /**
     * Pre-Update Hook - Wird aufgerufen, bevor die Entity aktualisiert wird
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
