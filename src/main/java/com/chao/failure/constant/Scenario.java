package com.chao.failure.constant;

/**
 * Business scene enum for validation classification.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public enum Scenario {
    // --- 1. Basic single operations (4) ---
    DEFAULT,      // Default/generic scenario (fallback)
    CREATE,       // Create (strong validation, generate new ID)
    UPDATE,       // Update (incremental validation, optimistic lock check)
    DELETE,       // Delete (soft delete flag or cascade check)

    // --- 2. Workflow and status transitions (5) ---
    SUBMIT,       // Submit (from draft/edit state to approval state, trigger process)
    APPROVE,      // Approve (admin action, status change)
    REJECT,       // Reject (return for modification, record reason)
    DRAFT,        // Draft (weak validation, no process trigger, allow incomplete data)
    PUBLISH,      // Publish (make data visible or officially effective)

    // --- 3. Data import/export and synchronization (4) ---
    IMPORT,       // Import (batch write, fault tolerance, format cleaning)
    EXPORT,       // Export (data query + formatting, read-only but time-consuming)
    SYNC,         // Sync (external system integration, ignore some local business rules)
    MIGRATE,      // Migrate (old data relocation, usually disable all validation, direct database write)

    // --- 4. Batch collection operations (3) ---
    BATCH_CREATE, // Batch create (large transaction boundary, allow partial failure)
    BATCH_UPDATE, // Batch update (direct edit from list page or batch status change)
    BATCH_DELETE, // Batch delete (complex cascade logic, requires high permission)

    // --- 5. Data derivation and mutation (3) ---
    COPY,         // Copy/clone (create new data based on old data, reset primary key)
    MERGE,        // Merge (combine multiple data into one, deduplication logic)
    SPLIT,        // Split (split one data into multiple, amount/quantity allocation)

    // --- 6. Special maintenance and recovery (1) ---
    RESTORE,      // Restore (recover from recycle bin or archive, reverse delete logic)
}
