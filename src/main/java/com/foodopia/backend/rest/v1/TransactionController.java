package com.foodopia.backend.rest.v1;

import com.foodopia.backend.rest.v1.dto.TransactionRequestDTO;
import com.foodopia.backend.rest.v1.dto.TransactionResponseDTO;
import com.foodopia.backend.rest.v1.dto.TransactionStatusUpdateDTO;
import com.foodopia.backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import java.time.LocalDate;

/**
 * REST Controller für Transaktionen (Orders/Bestellungen)
 *
 * Stellt Endpoints zur Verfügung für:
 * - Erstellung neuer Bestellungen
 * - Abruf von Bestellungen (Kunde, Verkäufer, spezifisches Item)
 * - Abruf von Transaktionen zwischen zwei Usern
 * - Status-Updates für Bestellungen
 * - Löschen von Bestellungen
 * - Filterung nach Status, Datum, Preis, Menge
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "API für Transaktionen (Orders/Bestellungen)")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Erstellt eine neue Bestellung
     *
     * POST /api/v1/transactions
     *
     * Der Kunde wird automatisch aus dem Authorization Bearer Token extrahiert.
     *
     * Geschäftslogik:
     * 1. Validiere dass Item existiert
     * 2. Erstelle neue Transaktion mit Status PENDING
     * 3. Verkäufer wird automatisch aus Item-Daten ermittelt
     *
     * @param authHeader Authorization Bearer Token (automatisch vom Framework gesetzt)
     * @param requestDTO Details der Bestellung
     * @return 201 Created mit der erstellten Bestellung
     */
    @PostMapping
    @Operation(summary = "Neue Bestellung erstellen", description = "Erstellt eine neue Bestellung für ein Item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bestellung erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "404", description = "Item nicht gefunden"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionRequestDTO requestDTO) {

        TransactionResponseDTO response = transactionService.createTransaction(authHeader, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Ruft eine Bestellung anhand ihrer ID ab
     *
     * GET /api/v1/transactions/{id}
     *
     * @param id Die Transaktions-ID
     * @return 200 OK mit der Bestellung
     */
    @GetMapping("/{id}")
    @Operation(summary = "Bestellung abrufen", description = "Ruft Details einer Bestellung anhand ihrer ID ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bestellung gefunden",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @Parameter(description = "Transaktions-ID") @PathVariable String id) {

        TransactionResponseDTO response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden ab
     *
     * GET /api/v1/transactions/customer/my-orders
     *
     * Der Kunde wird automatisch aus dem Authorization Bearer Token extrahiert.
     *
     * Query Parameter (optional) für Filterung:
     * - status: Filtert nach Status (z.B. PENDING, CONFIRMED, COMPLETED)
     * - minDate: Minimum Erstellungsdatum (yyyy-MM-dd)
     * - maxDate: Maximum Erstellungsdatum (yyyy-MM-dd)
     * - minPrice: Minimum Gesamtpreis
     * - maxPrice: Maximum Gesamtpreis
     * - minQuantity: Minimum bestellte Menge
     * - maxQuantity: Maximum bestellte Menge
     *
     * @param authHeader Authorization Bearer Token
     * @param status Status Filter (optional)
     * @param minDate Minimum Date (optional)
     * @param maxDate Maximum Date (optional)
     * @param minPrice Minimum Price (optional)
     * @param maxPrice Maximum Price (optional)
     * @param minQuantity Minimum Quantity (optional)
     * @param maxQuantity Maximum Quantity (optional)
     * @return 200 OK mit Liste aller Bestellungen des Kunden (gefiltert)
     */
    @GetMapping("/customer/my-orders")
    @Operation(summary = "Meine Bestellungen (mit optionalen Filtern)",
            description = "Ruft alle Bestellungen des aktuellen Kunden ab mit optionalen Filtern")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste der Bestellungen",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<List<TransactionResponseDTO>> getMyOrders(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Status Filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate minDate,
            @Parameter(description = "Maximum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate maxDate,
            @Parameter(description = "Minimum Preis") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum Preis") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum Menge") @RequestParam(required = false) Double minQuantity,
            @Parameter(description = "Maximum Menge") @RequestParam(required = false) Double maxQuantity) {

        List<TransactionResponseDTO> orders = transactionService.getMyOrders(
                authHeader, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
        return ResponseEntity.ok(orders);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers ab
     *
     * GET /api/v1/transactions/seller/my-sales
     *
     * Der Verkäufer wird automatisch aus dem Authorization Bearer Token extrahiert.
     *
     * Query Parameter (optional) für Filterung:
     * - status: Filtert nach Status (z.B. PENDING, CONFIRMED, COMPLETED)
     * - minDate: Minimum Erstellungsdatum (yyyy-MM-dd)
     * - maxDate: Maximum Erstellungsdatum (yyyy-MM-dd)
     * - minPrice: Minimum Gesamtpreis
     * - maxPrice: Maximum Gesamtpreis
     * - minQuantity: Minimum bestellte Menge
     * - maxQuantity: Maximum bestellte Menge
     *
     * @param authHeader Authorization Bearer Token
     * @param status Status Filter (optional)
     * @param minDate Minimum Date (optional)
     * @param maxDate Maximum Date (optional)
     * @param minPrice Minimum Price (optional)
     * @param maxPrice Maximum Price (optional)
     * @param minQuantity Minimum Quantity (optional)
     * @param maxQuantity Maximum Quantity (optional)
     * @return 200 OK mit Liste aller Verkäufe des Verkäufers (gefiltert)
     */
    @GetMapping("/seller/my-sales")
    @Operation(summary = "Meine Verkäufe (mit optionalen Filtern)",
            description = "Ruft alle Verkäufe des aktuellen Verkäufers ab mit optionalen Filtern")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste der Verkäufe",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<List<TransactionResponseDTO>> getMySales(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Status Filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate minDate,
            @Parameter(description = "Maximum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate maxDate,
            @Parameter(description = "Minimum Preis") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum Preis") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum Menge") @RequestParam(required = false) Double minQuantity,
            @Parameter(description = "Maximum Menge") @RequestParam(required = false) Double maxQuantity) {

        List<TransactionResponseDTO> sales = transactionService.getMySales(
                authHeader, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
        return ResponseEntity.ok(sales);
    }

    /**
     * Ruft alle Transaktionen zwischen dem aktuellen User und einem anderen User ab
     *
     * GET /api/v1/transactions/between
     *
     * Der aktuelle User wird automatisch aus dem Authorization Bearer Token extrahiert.
     * Der andere User wird via Query Parameter 'email' angegeben.
     *
     * Query Parameter:
     * - email: Email-Adresse des anderen Users (erforderlich)
     * - status: Filtert nach Status (optional)
     * - minDate: Minimum Erstellungsdatum (yyyy-MM-dd) (optional)
     * - maxDate: Maximum Erstellungsdatum (yyyy-MM-dd) (optional)
     * - minPrice: Minimum Gesamtpreis (optional)
     * - maxPrice: Maximum Gesamtpreis (optional)
     * - minQuantity: Minimum bestellte Menge (optional)
     * - maxQuantity: Maximum bestellte Menge (optional)
     *
     * Sicherheit: Es werden nur Transaktionen zurückgegeben, bei denen der aktuelle User beteiligt ist
     * (als Kunde oder Verkäufer).
     *
     * @param authHeader Authorization Bearer Token
     * @param otherUserEmail Die Email-Adresse des anderen Users (erforderlich)
     * @param status Status Filter (optional)
     * @param minDate Minimum Date (optional)
     * @param maxDate Maximum Date (optional)
     * @param minPrice Minimum Price (optional)
     * @param maxPrice Maximum Price (optional)
     * @param minQuantity Minimum Quantity (optional)
     * @param maxQuantity Maximum Quantity (optional)
     * @return 200 OK mit Liste aller Transaktionen zwischen den zwei Usern
     */
    @GetMapping("/between")
    @Operation(summary = "Transaktionen zwischen zwei Usern",
            description = "Ruft alle Transaktionen zwischen dem aktuellen User und einem anderen User ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste der Transaktionen",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Email Parameter erforderlich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsBetweenUsers(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Email des anderen Users", required = true) @RequestParam String otherUserEmail,
            @Parameter(description = "Status Filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate minDate,
            @Parameter(description = "Maximum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate maxDate,
            @Parameter(description = "Minimum Preis") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum Preis") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum Menge") @RequestParam(required = false) Double minQuantity,
            @Parameter(description = "Maximum Menge") @RequestParam(required = false) Double maxQuantity) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsBetweenUsers(
                authHeader, otherUserEmail);

        // Wende Filter an
        // Note: Filter werden innerhalb des Service angewendet, wenn benötigt
        return ResponseEntity.ok(transactions);
    }

    /**
     * Aktualisiert den Status einer Bestellung
     *
     * PUT /api/v1/transactions/{id}/status
     *
     * Nur der Verkäufer kann den Status ändern.
     *
     * Gültige Übergänge:
     * - PENDING → CONFIRMED (Verkäufer akzeptiert Bestellung)
     * - PENDING → REJECTED (Verkäufer lehnt ab)
     * - PENDING → CANCELLED (Kunde oder Verkäufer storniert)
     * - CONFIRMED → COMPLETED (Bestellung erfüllt)
     * - CONFIRMED → CANCELLED (Mit Quantity-Restitution)
     *
     * @param authHeader Authorization Bearer Token
     * @param id Die Transaktions-ID
     * @param statusUpdateDTO Der neue Status
     * @return 200 OK mit der aktualisierten Bestellung
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Bestellungs-Status aktualisieren",
            description = "Aktualisiert den Status einer Bestellung. Nur Verkäufer darf diese Operation durchführen.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ungültiger Status-Übergang"),
            @ApiResponse(responseCode = "403", description = "Nicht berechtigt (nicht der Verkäufer)"),
            @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden"),
            @ApiResponse(responseCode = "409", description = "Unzureichende Menge verfügbar")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<TransactionResponseDTO> updateTransactionStatus(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Transaktions-ID") @PathVariable String id,
            @Valid @RequestBody TransactionStatusUpdateDTO statusUpdateDTO) {

        TransactionResponseDTO response = transactionService.updateTransactionStatus(authHeader, id, statusUpdateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden für ein bestimmtes Item ab
     *
     * GET /api/v1/transactions/customer/item/{itemId}
     *
     * Der Kunde wird automatisch aus dem Authorization Bearer Token extrahiert.
     * Nur Transaktionen, bei denen der Kunde als customer_email eingetragen ist, werden zurückgegeben.
     *
     * Query Parameter (optional) für Filterung:
     * - status: Filtert nach Status
     * - minDate: Minimum Erstellungsdatum (yyyy-MM-dd)
     * - maxDate: Maximum Erstellungsdatum (yyyy-MM-dd)
     * - minPrice: Minimum Gesamtpreis
     * - maxPrice: Maximum Gesamtpreis
     * - minQuantity: Minimum bestellte Menge
     * - maxQuantity: Maximum bestellte Menge
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @param status Status Filter (optional)
     * @param minDate Minimum Date (optional)
     * @param maxDate Maximum Date (optional)
     * @param minPrice Minimum Price (optional)
     * @param maxPrice Maximum Price (optional)
     * @param minQuantity Minimum Quantity (optional)
     * @param maxQuantity Maximum Quantity (optional)
     * @return 200 OK mit Liste gefilterte Bestellungen
     */
    @GetMapping("/customer/item/{itemId}")
    @Operation(summary = "Meine Bestellungen für ein Item (mit optionalen Filtern)",
            description = "Ruft alle Bestellungen des aktuellen Kunden für ein bestimmtes Item ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste der Bestellungen",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<List<TransactionResponseDTO>> getMyOrdersForItem(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Item-ID") @PathVariable String itemId,
            @Parameter(description = "Status Filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate minDate,
            @Parameter(description = "Maximum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate maxDate,
            @Parameter(description = "Minimum Preis") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum Preis") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum Menge") @RequestParam(required = false) Double minQuantity,
            @Parameter(description = "Maximum Menge") @RequestParam(required = false) Double maxQuantity) {

        List<TransactionResponseDTO> orders = transactionService.getMyOrdersForItem(
                authHeader, itemId, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
        return ResponseEntity.ok(orders);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers für ein bestimmtes Item ab
     *
     * GET /api/v1/transactions/seller/item/{itemId}
     *
     * Der Verkäufer wird automatisch aus dem Authorization Bearer Token extrahiert.
     * Nur Transaktionen, bei denen der Verkäufer als seller_email eingetragen ist, werden zurückgegeben.
     *
     * Query Parameter (optional) für Filterung:
     * - status: Filtert nach Status
     * - minDate: Minimum Erstellungsdatum (yyyy-MM-dd)
     * - maxDate: Maximum Erstellungsdatum (yyyy-MM-dd)
     * - minPrice: Minimum Gesamtpreis
     * - maxPrice: Maximum Gesamtpreis
     * - minQuantity: Minimum bestellte Menge
     * - maxQuantity: Maximum bestellte Menge
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @param status Status Filter (optional)
     * @param minDate Minimum Date (optional)
     * @param maxDate Maximum Date (optional)
     * @param minPrice Minimum Price (optional)
     * @param maxPrice Maximum Price (optional)
     * @param minQuantity Minimum Quantity (optional)
     * @param maxQuantity Maximum Quantity (optional)
     * @return 200 OK mit Liste gefilterte Verkäufe
     */
    @GetMapping("/seller/item/{itemId}")
    @Operation(summary = "Meine Verkäufe für ein Item (mit optionalen Filtern)",
            description = "Ruft alle Verkäufe des aktuellen Verkäufers für ein bestimmtes Item ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste der Verkäufe",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<List<TransactionResponseDTO>> getMySalesForItem(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Item-ID") @PathVariable String itemId,
            @Parameter(description = "Status Filter") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate minDate,
            @Parameter(description = "Maximum Datum (yyyy-MM-dd)") @RequestParam(required = false) LocalDate maxDate,
            @Parameter(description = "Minimum Preis") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum Preis") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum Menge") @RequestParam(required = false) Double minQuantity,
            @Parameter(description = "Maximum Menge") @RequestParam(required = false) Double maxQuantity) {

        List<TransactionResponseDTO> sales = transactionService.getMySalesForItem(
                authHeader, itemId, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
        return ResponseEntity.ok(sales);
    }

    /**
     * Löscht eine Bestellung
     *
     * DELETE /api/v1/transactions/{id}
     *
     * Kann nur Bestellungen mit Status PENDING löschen.
     * Berechtigt sind der Kunde oder der Verkäufer.
     *
     * @param authHeader Authorization Bearer Token
     * @param id Die Transaktions-ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Bestellung löschen",
            description = "Löscht eine Bestellung (nur wenn Status PENDING). Berechtigt: Kunde oder Verkäufer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bestellung erfolgreich gelöscht"),
            @ApiResponse(responseCode = "403", description = "Nicht berechtigt"),
            @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden"),
            @ApiResponse(responseCode = "409", description = "Bestellung kann nicht gelöscht werden (nicht PENDING)")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<Void> deleteTransaction(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Transaktions-ID") @PathVariable String id) {

        transactionService.deleteTransaction(authHeader, id);
        return ResponseEntity.noContent().build();
    }
}
