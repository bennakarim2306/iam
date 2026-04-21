package com.foodopia.backend.data.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository für Transaction-Entitäten
 * Bietet CRUD-Operationen und Custom Queries für Transaktionen
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Findet alle Transaktionen für einen Kunden anhand seiner E-Mail
     *
     * @param customerEmail Die E-Mail des Kunden
     * @return Liste aller Transaktionen des Kunden
     */
    List<Transaction> findByCustomerEmail(String customerEmail);

    /**
     * Findet alle Transaktionen für einen Verkäufer anhand seiner E-Mail
     *
     * @param sellerEmail Die E-Mail des Verkäufers
     * @return Liste aller Verkäufe des Verkäufers
     */
    List<Transaction> findBySellerEmail(String sellerEmail);

    /**
     * Findet alle Transaktionen für ein spezifisches Item
     *
     * @param itemId Die Item-ID
     * @return Liste aller Transaktionen für das Item
     */
    List<Transaction> findByItemId(String itemId);

    /**
     * Findet alle Transaktionen mit einem spezifischen Status
     *
     * @param status Der Transaktionsstatus
     * @return Liste aller Transaktionen mit diesem Status
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Findet eine Transaktion anhand ihrer ID
     *
     * @param id Die Transaktions-ID
     * @return Optional mit der Transaktion oder leer
     */
    Optional<Transaction> findById(String id);

    /**
     * Findet alle Transaktionen für ein Item mit einem spezifischen Status
     *
     * @param itemId Die Item-ID
     * @param status Der Transaktionsstatus
     * @return Liste der Transaktionen
     */
    @Query("SELECT t FROM transactions t WHERE t.itemId = :itemId AND t.status = :status")
    List<Transaction> findByItemIdAndStatus(
            @Param("itemId") String itemId,
            @Param("status") TransactionStatus status
    );

    /**
     * Findet alle Transaktionen für einen Kunden mit einem spezifischen Status
     *
     * @param customerEmail Die E-Mail des Kunden
     * @param status        Der Transaktionsstatus
     * @return Liste der Transaktionen
     */
    @Query("SELECT t FROM transactions t WHERE t.customerEmail = :customerEmail AND t.status = :status")
    List<Transaction> findByCustomerEmailAndStatus(
            @Param("customerEmail") String customerEmail,
            @Param("status") TransactionStatus status
    );

    /**
     * Findet alle Transaktionen für einen Verkäufer mit einem spezifischen Status
     *
     * @param sellerEmail Die E-Mail des Verkäufers
     * @param status      Der Transaktionsstatus
     * @return Liste der Transaktionen
     */
    @Query("SELECT t FROM transactions t WHERE t.sellerEmail = :sellerEmail AND t.status = :status")
    List<Transaction> findBySellerEmailAndStatus(
            @Param("sellerEmail") String sellerEmail,
            @Param("status") TransactionStatus status
    );

    /**
     * Findet alle Transaktionen zwischen zwei spezifischen Users
     *
     * @param email1 Erste Email-Adresse
     * @param email2 Zweite Email-Adresse
     * @return Liste der Transaktionen zwischen den beiden Users
     */
    @Query("SELECT t FROM transactions t WHERE " +
            "(t.customerEmail = :email1 AND t.sellerEmail = :email2) OR " +
            "(t.customerEmail = :email2 AND t.sellerEmail = :email1)")
    List<Transaction> findByCustomerEmailAndSellerEmail(
            @Param("email1") String email1,
            @Param("email2") String email2
    );
}