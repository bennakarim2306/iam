package com.foodopia.backend.service;

import com.foodopia.backend.data.item.Item;
import com.foodopia.backend.data.item.ItemRepository;
import com.foodopia.backend.data.transaction.Transaction;
import com.foodopia.backend.data.transaction.TransactionRepository;
import com.foodopia.backend.data.transaction.TransactionStatus;
import com.foodopia.backend.data.user.UserRepository;
import com.foodopia.backend.exception.*;
import com.foodopia.backend.rest.v1.dto.TransactionRequestDTO;
import com.foodopia.backend.rest.v1.dto.TransactionResponseDTO;
import com.foodopia.backend.rest.v1.dto.TransactionStatusUpdateDTO;
import com.foodopia.backend.rest.v1.mapper.ItemMapper;
import com.foodopia.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service-Klasse für Transaktionen (Orders/Bestellungen)
 *
 * Verantwortlichkeiten:
 * - Erstellung neuer Transaktionen mit Validierung
 * - Status-Verwaltung und -Übergänge
 * - Quantity-Management (Reduktion/Restitution im Item)
 * - Abruf von Transaktionen (nach Kunde, Verkäufer, Item, zwischen zwei Usern)
 * - Filter-Funktionalität (Status, Daten, Preis, Menge)
 * - Enforcement von Geschäftsregeln
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ItemMapper itemMapper;

    // ...existing code...

    /**
     * Ruft alle Transaktionen zwischen zwei Usern ab
     *
     * Eine Transaktion zwischen zwei Usern existiert, wenn:
     * - Der erste User der Kunde UND der zweite User der Verkäufer ist, ODER
     * - Der erste User der Verkäufer UND der zweite User der Kunde ist
     *
     * Sicherheit: Der anfordernde User wird aus dem Token extrahiert.
     * Es werden nur Transaktionen zurückgegeben, bei denen der User beteiligt ist.
     *
     * @param authHeader Authorization Bearer Token
     * @param otherUserEmail Die Email-Adresse des anderen Users
     * @return Liste aller Transaktionen zwischen den beiden Usern
     */
    public List<TransactionResponseDTO> getTransactionsBetweenUsers(String authHeader, String otherUserEmail) {
        String currentUserEmail = extractEmailFromToken(authHeader);
        log.debug("Fetching transactions between: {} and {}", currentUserEmail, otherUserEmail);

        if (currentUserEmail.equals(otherUserEmail)) {
            log.warn("User {} tried to get transactions with themselves", currentUserEmail);
            throw new IllegalArgumentException("Cannot get transactions between the same user");
        }

        // Hole alle Transaktionen wo currentUser Kunde und otherUser Verkäufer ist
        List<Transaction> asCustomer = transactionRepository.findByCustomerEmailAndSellerEmail(
                currentUserEmail, otherUserEmail);

        // Hole alle Transaktionen wo currentUser Verkäufer und otherUser Kunde ist
        List<Transaction> asSeller = transactionRepository.findByCustomerEmailAndSellerEmail(
                otherUserEmail, currentUserEmail);

        // Kombiniere beide Listen
        List<Transaction> allTransactions = new java.util.ArrayList<>(asCustomer);
        allTransactions.addAll(asSeller);

        log.debug("Found {} transactions between {} and {}",
                allTransactions.size(), currentUserEmail, otherUserEmail);

        return allTransactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden ab (mit optionalen Filtern)
     *
     * @param authHeader Authorization Bearer Token
     * @param status Filter nach Status (optional)
     * @param minDate Filter nach Minimum-Datum (optional)
     * @param maxDate Filter nach Maximum-Datum (optional)
     * @param minPrice Filter nach Minimum-Preis (optional)
     * @param maxPrice Filter nach Maximum-Preis (optional)
     * @param minQuantity Filter nach Minimum-Menge (optional)
     * @param maxQuantity Filter nach Maximum-Menge (optional)
     * @return Liste gefilterte Bestellungen des Kunden
     */
    public List<TransactionResponseDTO> getMyOrders(
            String authHeader,
            String status,
            LocalDate minDate,
            LocalDate maxDate,
            Double minPrice,
            Double maxPrice,
            Double minQuantity,
            Double maxQuantity) {

        String customerEmail = extractEmailFromToken(authHeader);
        log.debug("Fetching orders for customer: {} with filters", customerEmail);

        List<Transaction> transactions = transactionRepository.findByCustomerEmail(customerEmail);

        return applyFilters(transactions, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers ab (mit optionalen Filtern)
     *
     * @param authHeader Authorization Bearer Token
     * @param status Filter nach Status (optional)
     * @param minDate Filter nach Minimum-Datum (optional)
     * @param maxDate Filter nach Maximum-Datum (optional)
     * @param minPrice Filter nach Minimum-Preis (optional)
     * @param maxPrice Filter nach Maximum-Preis (optional)
     * @param minQuantity Filter nach Minimum-Menge (optional)
     * @param maxQuantity Filter nach Maximum-Menge (optional)
     * @return Liste gefilterte Verkäufe des Verkäufers
     */
    public List<TransactionResponseDTO> getMySales(
            String authHeader,
            String status,
            LocalDate minDate,
            LocalDate maxDate,
            Double minPrice,
            Double maxPrice,
            Double minQuantity,
            Double maxQuantity) {

        String sellerEmail = extractEmailFromToken(authHeader);
        log.debug("Fetching sales for seller: {} with filters", sellerEmail);

        List<Transaction> transactions = transactionRepository.findBySellerEmail(sellerEmail);

        return applyFilters(transactions, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden für ein bestimmtes Item ab (mit optionalen Filtern)
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @param status Filter nach Status (optional)
     * @param minDate Filter nach Minimum-Datum (optional)
     * @param maxDate Filter nach Maximum-Datum (optional)
     * @param minPrice Filter nach Minimum-Preis (optional)
     * @param maxPrice Filter nach Maximum-Preis (optional)
     * @param minQuantity Filter nach Minimum-Menge (optional)
     * @param maxQuantity Filter nach Maximum-Menge (optional)
     * @return Liste gefilterte Bestellungen des Kunden für das Item
     */
    public List<TransactionResponseDTO> getMyOrdersForItem(
            String authHeader,
            String itemId,
            String status,
            LocalDate minDate,
            LocalDate maxDate,
            Double minPrice,
            Double maxPrice,
            Double minQuantity,
            Double maxQuantity) {

        String customerEmail = extractEmailFromToken(authHeader);
        log.debug("Fetching orders for customer: {} and item: {} with filters", customerEmail, itemId);

        List<Transaction> transactions = transactionRepository.findByItemId(itemId);

        List<Transaction> filtered = transactions.stream()
                .filter(transaction -> customerEmail.equals(transaction.getCustomerEmail()))
                .collect(Collectors.toList());

        return applyFilters(filtered, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers für ein bestimmtes Item ab (mit optionalen Filtern)
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @param status Filter nach Status (optional)
     * @param minDate Filter nach Minimum-Datum (optional)
     * @param maxDate Filter nach Maximum-Datum (optional)
     * @param minPrice Filter nach Minimum-Preis (optional)
     * @param maxPrice Filter nach Maximum-Preis (optional)
     * @param minQuantity Filter nach Minimum-Menge (optional)
     * @param maxQuantity Filter nach Maximum-Menge (optional)
     * @return Liste gefilterte Verkäufe des Verkäufers für das Item
     */
    public List<TransactionResponseDTO> getMySalesForItem(
            String authHeader,
            String itemId,
            String status,
            LocalDate minDate,
            LocalDate maxDate,
            Double minPrice,
            Double maxPrice,
            Double minQuantity,
            Double maxQuantity) {

        String sellerEmail = extractEmailFromToken(authHeader);
        log.debug("Fetching sales for seller: {} and item: {} with filters", sellerEmail, itemId);

        List<Transaction> transactions = transactionRepository.findByItemId(itemId);

        List<Transaction> filtered = transactions.stream()
                .filter(transaction -> sellerEmail.equals(transaction.getSellerEmail()))
                .collect(Collectors.toList());

        return applyFilters(filtered, status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);
    }

    // ...existing code...

    /**
     * Private Hilfsmethode: Wendet Filter auf eine Liste von Transaktionen an
     *
     * Unterstützte Filter:
     * - status: Filtert nach Transaktionsstatus (z.B. "PENDING", "CONFIRMED")
     * - minDate/maxDate: Filtert nach Erstellungsdatum
     * - minPrice/maxPrice: Filtert nach Gesamtpreis
     * - minQuantity/maxQuantity: Filtert nach bestellter Menge
     *
     * @param transactions Die zu filternden Transaktionen
     * @param status Status-Filter (optional)
     * @param minDate Minimum-Datum (optional)
     * @param maxDate Maximum-Datum (optional)
     * @param minPrice Minimum-Preis (optional)
     * @param maxPrice Maximum-Preis (optional)
     * @param minQuantity Minimum-Menge (optional)
     * @param maxQuantity Maximum-Menge (optional)
     * @return Gefilterte Liste als DTOs
     */
    private List<TransactionResponseDTO> applyFilters(
            List<Transaction> transactions,
            String status,
            LocalDate minDate,
            LocalDate maxDate,
            Double minPrice,
            Double maxPrice,
            Double minQuantity,
            Double maxQuantity) {

        log.debug("Applying filters: status={}, minDate={}, maxDate={}, minPrice={}, maxPrice={}, minQty={}, maxQty={}",
                status, minDate, maxDate, minPrice, maxPrice, minQuantity, maxQuantity);

        return transactions.stream()
                // Filter nach Status
                .filter(t -> status == null || t.getStatus().toString().equalsIgnoreCase(status))
                // Filter nach Datum (minimum)
                .filter(t -> minDate == null || t.getCreatedAt().toLocalDate().isAfter(minDate) ||
                       t.getCreatedAt().toLocalDate().isEqual(minDate))
                // Filter nach Datum (maximum)
                .filter(t -> maxDate == null || t.getCreatedAt().toLocalDate().isBefore(maxDate) ||
                       t.getCreatedAt().toLocalDate().isEqual(maxDate))
                // Filter nach Preis (minimum)
                .filter(t -> minPrice == null || t.getTotalPrice() >= minPrice)
                // Filter nach Preis (maximum)
                .filter(t -> maxPrice == null || t.getTotalPrice() <= maxPrice)
                // Filter nach Menge (minimum)
                .filter(t -> minQuantity == null || t.getQuantityOrdered() >= minQuantity)
                // Filter nach Menge (maximum)
                .filter(t -> maxQuantity == null || t.getQuantityOrdered() <= maxQuantity)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ...existing code...

    /**
     * Erstellt eine neue Transaktion
     *
     * Geschäftslogik:
     * 1. Extrahiere Kunden-Email aus Authorization Header
     * 2. Validiere dass das Item existiert
     * 3. Validiere dass die Menge verfügbar ist (wenn nicht PENDING)
     * 4. Erstelle neue Transaktion mit Status PENDING
     * 5. Speichere in DB
     *
     * @param authHeader Authorization Bearer Token
     * @param requestDTO Die Transaktions-Details
     * @return Die erstellte Transaktion als Response
     * @throws ItemNotFoundException wenn Item nicht existiert
     * @throws InsufficientQuantityException wenn Menge nicht ausreichend ist
     */
    @Transactional
    public TransactionResponseDTO createTransaction(String authHeader, TransactionRequestDTO requestDTO) {
        log.info("Creating new transaction for item: {}", requestDTO.getItemId());

        // Extrahiere Kunden-Email aus Token
        String customerEmail = extractEmailFromToken(authHeader);
        log.debug("Customer email extracted: {}", customerEmail);

        // Validiere dass das Item existiert
        Item item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + requestDTO.getItemId()));

        // Hole Verkäufer-Email aus Item
        String sellerEmail = item.getSeller().getContact();
        log.debug("Seller email: {}", sellerEmail);

        // Validiere dass Kunde und Verkäufer unterschiedlich sind
        if (customerEmail.equals(sellerEmail)) {
            log.warn("Customer cannot buy from themselves: {}", customerEmail);
            throw new IllegalArgumentException("You cannot buy from yourself");
        }

        // Berechne Gesamtpreis
        double totalPrice = requestDTO.getQuantityOrdered() * item.getPrice();

        // Erstelle neue Transaktion
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .itemId(item.getId())
                .customerEmail(customerEmail)
                .sellerEmail(sellerEmail)
                .quantityOrdered(requestDTO.getQuantityOrdered())
                .pricePerUnit(item.getPrice())
                .totalPrice(totalPrice)
                .status(TransactionStatus.PENDING)
                .notes(requestDTO.getNotes())
                .build();

        // Speichere Transaktion
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully: {} with status PENDING", savedTransaction.getId());

        return mapToResponseDTO(savedTransaction);
    }

    /**
     * Aktualisiert den Status einer Transaktion
     *
     * Geschäftslogik:
     * 1. Finde Transaktion
     * 2. Validiere Status-Übergang
     * 3. Wenn Übergang zu CONFIRMED: Reduziere Item-Quantity
     * 4. Wenn Übergang von CONFIRMED zu anderen: Restitution der Quantity
     * 5. Speichere aktualisierte Transaktion
     *
     * @param authHeader Authorization Bearer Token
     * @param transactionId Die Transaktions-ID
     * @param statusUpdateDTO Der neue Status
     * @return Die aktualisierte Transaktion
     * @throws TransactionNotFoundException wenn Transaktion nicht existiert
     * @throws InvalidStatusTransitionException wenn Status-Übergang ungültig ist
     * @throws InsufficientQuantityException wenn Quantity nicht ausreichend ist
     */
    @Transactional
    public TransactionResponseDTO updateTransactionStatus(
            String authHeader,
            String transactionId,
            TransactionStatusUpdateDTO statusUpdateDTO) {

        log.info("Updating transaction status: {} to {}", transactionId, statusUpdateDTO.getStatus());

        // Finde Transaktion
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        String userEmail = extractEmailFromToken(authHeader);

        // Validiere Berechtigung (nur Verkäufer kann Status ändern)
        if (!userEmail.equals(transaction.getSellerEmail())) {
            log.warn("Unauthorized status update attempt by: {} for transaction: {}", userEmail, transactionId);
            throw new UnauthorizedException("Only seller can update transaction status");
        }

        TransactionStatus currentStatus = transaction.getStatus();
        TransactionStatus newStatus = statusUpdateDTO.getStatus();

        // Validiere Status-Übergang
        if (!TransactionStatus.isValidTransition(currentStatus, newStatus)) {
            log.warn("Invalid status transition from {} to {}", currentStatus, newStatus);
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }

        // Handle Quantity bei Status-Änderungen
        handleStatusChange(transaction, currentStatus, newStatus);

        // Aktualisiere Status
        transaction.setStatus(newStatus);

        // Speichere
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction status updated successfully: {} -> {}", transactionId, newStatus);

        return mapToResponseDTO(updatedTransaction);
    }

    /**
     * Ruft eine einzelne Transaktion anhand ihrer ID ab
     *
     * @param transactionId Die Transaktions-ID
     * @return Die Transaktion als Response
     * @throws TransactionNotFoundException wenn nicht gefunden
     */
    public TransactionResponseDTO getTransactionById(String transactionId) {
        log.debug("Fetching transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        return mapToResponseDTO(transaction);
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden ab
     * Überladene Methode ohne Filter
     *
     * @param authHeader Authorization Bearer Token
     * @return Liste aller Bestellungen des Kunden
     */
    public List<TransactionResponseDTO> getMyOrders(String authHeader) {
        return getMyOrders(authHeader, null, null, null, null, null, null, null);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers ab
     * Überladene Methode ohne Filter
     *
     * @param authHeader Authorization Bearer Token
     * @return Liste aller Verkäufe des Verkäufers
     */
    public List<TransactionResponseDTO> getMySales(String authHeader) {
        return getMySales(authHeader, null, null, null, null, null, null, null);
    }

    /**
     * Ruft alle Bestellungen des aktuellen Kunden für ein bestimmtes Item ab
     * Überladene Methode ohne Filter
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @return Liste aller Bestellungen des Kunden für dieses Item
     */
    public List<TransactionResponseDTO> getMyOrdersForItem(String authHeader, String itemId) {
        return getMyOrdersForItem(authHeader, itemId, null, null, null, null, null, null, null);
    }

    /**
     * Ruft alle Verkäufe des aktuellen Verkäufers für ein bestimmtes Item ab
     * Überladene Methode ohne Filter
     *
     * @param authHeader Authorization Bearer Token
     * @param itemId Die Item-ID
     * @return Liste aller Verkäufe des Verkäufers für dieses Item
     */
    public List<TransactionResponseDTO> getMySalesForItem(String authHeader, String itemId) {
        return getMySalesForItem(authHeader, itemId, null, null, null, null, null, null, null);
    }

    /**
     * Ruft alle Transaktionen für ein spezifisches Item ab
     *
     * @param itemId Die Item-ID
     * @return Liste der Transaktionen für das Item
     */
    public List<TransactionResponseDTO> getTransactionsByItem(String itemId) {
        log.debug("Fetching transactions for item: {}", itemId);

        List<Transaction> transactions = transactionRepository.findByItemId(itemId);
        return transactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Löscht eine Transaktion (nur wenn sie noch PENDING ist)
     *
     * @param authHeader Authorization Bearer Token
     * @param transactionId Die Transaktions-ID
     * @throws TransactionNotFoundException wenn nicht gefunden
     * @throws IllegalStateException wenn Transaktion nicht PENDING ist
     */
    @Transactional
    public void deleteTransaction(String authHeader, String transactionId) {
        log.info("Deleting transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        String userEmail = extractEmailFromToken(authHeader);

        // Validiere Berechtigung (Kunde oder Verkäufer)
        if (!userEmail.equals(transaction.getCustomerEmail()) && !userEmail.equals(transaction.getSellerEmail())) {
            log.warn("Unauthorized deletion attempt by: {} for transaction: {}", userEmail, transactionId);
            throw new UnauthorizedException("You cannot delete this transaction");
        }

        // Kann nur PENDING Transaktionen gelöscht werden
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Cannot delete non-PENDING transaction: {} with status: {}", transactionId, transaction.getStatus());
            throw new IllegalStateException(
                    String.format("Cannot delete transaction with status %s", transaction.getStatus())
            );
        }

        transactionRepository.deleteById(transactionId);
        log.info("Transaction deleted successfully: {}", transactionId);
    }

    /**
     * Private Hilfsmethode: Verarbeitet Quantity-Änderungen bei Status-Übergängen
     *
     * Regeln:
     * - PENDING → CONFIRMED: Reduziere Item-Quantity
     * - CONFIRMED → CANCELLED: Restitution der Quantity
     * - Andere Übergänge: Keine Quantity-Änderung
     *
     * @param transaction Die Transaktion
     * @param currentStatus Der aktuelle Status
     * @param newStatus Der neue Status
     * @throws InsufficientQuantityException wenn Quantity nicht ausreichend
     */
    private void handleStatusChange(Transaction transaction, TransactionStatus currentStatus, TransactionStatus newStatus) {
        log.debug("Handling status change from {} to {} for transaction {}", currentStatus, newStatus, transaction.getId());

        Item item = itemRepository.findById(transaction.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + transaction.getItemId()));

        // PENDING → CONFIRMED: Quantity reduzieren
        if (currentStatus == TransactionStatus.PENDING && newStatus == TransactionStatus.CONFIRMED) {
            updateItemQuantity(item, -transaction.getQuantityOrdered());
            log.info("Item quantity reduced by {} for transaction {}", transaction.getQuantityOrdered(), transaction.getId());
        }

        // CONFIRMED → CANCELLED: Quantity zurückbuchen
        else if (currentStatus == TransactionStatus.CONFIRMED && newStatus == TransactionStatus.CANCELLED) {
            updateItemQuantity(item, transaction.getQuantityOrdered());
            log.info("Item quantity restored by {} for transaction {}", transaction.getQuantityOrdered(), transaction.getId());
        }

        // CONFIRMED → REJECTED: Quantity zurückbuchen (falls aus CONFIRMED kommt)
        else if (currentStatus == TransactionStatus.CONFIRMED && newStatus == TransactionStatus.REJECTED) {
            updateItemQuantity(item, transaction.getQuantityOrdered());
            log.info("Item quantity restored by {} due to rejection for transaction {}", transaction.getQuantityOrdered(), transaction.getId());
        }

        // Alle anderen Übergänge: Keine Quantity-Änderung nötig
    }

    /**
     * Private Hilfsmethode: Aktualisiert die Quantity eines Items
     *
     * @param item Das Item
     * @param quantityDelta Der Betrag um den die Quantity geändert wird (negativ = Reduktion)
     * @throws InsufficientQuantityException wenn resultierende Quantity negativ wäre
     */
    private void updateItemQuantity(Item item, double quantityDelta) {
        double newQuantity = item.getQuantity() + quantityDelta;

        // Validiere dass Quantity nicht negativ wird (nur bei Reduktion)
        if (quantityDelta < 0 && newQuantity < 0) {
            log.error("Insufficient quantity for item {}: available={}, required={}",
                    item.getId(), item.getQuantity(), Math.abs(quantityDelta));
            throw new InsufficientQuantityException(
                    String.format("Insufficient quantity for item %s: available=%.2f, required=%.2f",
                            item.getId(), item.getQuantity(), Math.abs(quantityDelta))
            );
        }

        item.setQuantity(newQuantity);
        itemRepository.save(item);
        log.debug("Item {} quantity updated: {} -> {}", item.getId(), item.getQuantity() - quantityDelta, newQuantity);
    }

    /**
     * Private Hilfsmethode: Extrahiert E-Mail-Adresse aus Authorization Header
     *
     * @param authHeader Der Authorization Header (z.B. "Bearer <token>")
     * @return Die E-Mail-Adresse aus dem Token
     */
    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7); // Entferne "Bearer "
        String email = jwtService.extractUserEmail(token);

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in token");
        }

        return email;
    }

    /**
     * Private Hilfsmethode: Mappt eine Transaction Entity zu einem Response DTO
     *
     * @param transaction Die Transaction Entity
     * @return Der TransactionResponseDTO
     */
    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        // Lade Item-Informationen für ItemSummaryDTO
        Item item = itemRepository.findById(transaction.getItemId()).orElse(null);

        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .itemId(transaction.getItemId())
                .item(item != null ? itemMapper.toSummaryDTO(item) : null)
                .customerEmail(transaction.getCustomerEmail())
                .sellerEmail(transaction.getSellerEmail())
                .quantityOrdered(transaction.getQuantityOrdered())
                .status(transaction.getStatus())
                .pricePerUnit(transaction.getPricePerUnit())
                .totalPrice(transaction.getTotalPrice())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
