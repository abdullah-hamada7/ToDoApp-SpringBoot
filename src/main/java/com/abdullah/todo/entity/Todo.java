package com.abdullah.todo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a Todo item.
 * 
 * Multi-tenancy: Each todo is owned by a specific user.
 * Users can only access their own todos.
 */
@Entity
@Table(name = "todos", indexes = {
		@Index(name = "idx_todo_owner", columnList = "owner_id"),
		@Index(name = "idx_todo_completed", columnList = "completed")
})
@Getter
@Setter
@NoArgsConstructor
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false)
	private boolean completed = false;

	/**
	 * The user who owns this todo.
	 * Enables multi-tenancy - each user sees only their own todos.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Todo(String title, boolean completed, User owner) {
		this.title = title;
		this.completed = completed;
		this.owner = owner;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}