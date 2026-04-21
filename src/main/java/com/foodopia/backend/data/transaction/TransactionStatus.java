package com.foodopia.backend.data.transaction;

/**
 * Enum für die verschiedenen Transaktionsstatus
 *
 * Gültige Übergänge:
 * PENDING   → CONFIRMED (Verkäufer akzeptiert)
 * PENDING   → REJECTED (Verkäufer lehnt ab)
 * PENDING   → CANCELLED (Kunde oder System storniert)
 * CONFIRMED → COMPLETED (Bestellung erfüllt)
 * CONFIRMED → CANCELLED (Kunde oder Verkäufer storniert mit Restitution)
 */
public enum TransactionStatus {
    /**
     * Neue Bestellung, wartet auf Bestätigung durch Verkäufer
     */
    PENDING("Ausstehend"),

    /**
     * Bestellung vom Verkäufer bestätigt.
     * Die Menge wird im entsprechenden Item reduziert.
     */
    CONFIRMED("Bestätigt"),

    /**
     * Bestellung erfüllt/abgeschlossen.
     * Keine weiteren Änderungen möglich.
     */
    COMPLETED("Abgeschlossen"),

    /**
     * Bestellung storniert (von Kunde oder System).
     * Bei Stornierung von CONFIRMED wird die Menge zurückgebucht.
     */
    CANCELLED("Storniert"),

    /**
     * Bestellung vom Verkäufer abgelehnt.
     * Keine Menge-Reduktion hat stattgefunden.
     */
    REJECTED("Abgelehnt");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Prüft, ob ein Status-Übergang von currentStatus zu newStatus gültig ist
     *
     * @param currentStatus Der aktuelle Status
     * @param newStatus Der neue Status
     * @return true wenn der Übergang gültig ist, false sonst
     */
    public static boolean isValidTransition(TransactionStatus currentStatus, TransactionStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == REJECTED || newStatus == CANCELLED;
            case CONFIRMED:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
            case REJECTED:
                // Kein Übergang möglich von diesen Endzuständen
                return false;
            default:
                return false;
        }
    }

    /**
     * Prüft, ob der Status Quantity-Reduktion bedeutet
     * (Item-Menge wird reduziert)
     *
     * @return true wenn Status CONFIRMED ist
     */
    public boolean doesReduceQuantity() {
        return this == CONFIRMED;
    }

    /**
     * Prüft, ob der Status eine Endsituation darstellt
     *
     * @return true wenn Status COMPLETED, CANCELLED oder REJECTED ist
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED;
    }
}
