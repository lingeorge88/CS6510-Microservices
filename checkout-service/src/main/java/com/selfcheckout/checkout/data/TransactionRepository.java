package com.selfcheckout.checkout.data;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for the transactions table (Checkout's own database). */
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
