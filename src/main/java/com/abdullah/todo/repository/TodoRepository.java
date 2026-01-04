package com.abdullah.todo.repository;

import com.abdullah.todo.entity.Todo;
import com.abdullah.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Todo entities.
 * 
 * All methods are scoped by owner for multi-tenancy.
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * Find all todos owned by a specific user.
     */
    List<Todo> findByOwner(User owner);

    /**
     * Find all todos by owner and completion status.
     */
    List<Todo> findByOwnerAndCompleted(User owner, boolean completed);

    /**
     * Find a specific todo by ID and owner.
     * Returns empty if todo doesn't exist or belongs to another user.
     */
    Optional<Todo> findByIdAndOwner(Long id, User owner);

    /**
     * Check if a todo exists and belongs to the user.
     */
    boolean existsByIdAndOwner(Long id, User owner);

    /**
     * Delete a todo by ID and owner.
     */
    void deleteByIdAndOwner(Long id, User owner);

    /**
     * Count todos by owner.
     */
    long countByOwner(User owner);

    /**
     * Count completed todos by owner.
     */
    long countByOwnerAndCompleted(User owner, boolean completed);
}
