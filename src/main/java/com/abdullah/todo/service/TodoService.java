package com.abdullah.todo.service;

import com.abdullah.todo.dto.TodoRequestDTO;
import com.abdullah.todo.dto.TodoResponseDTO;
import com.abdullah.todo.entity.Todo;
import com.abdullah.todo.entity.User;
import com.abdullah.todo.exception.TodoNotFoundException;
import com.abdullah.todo.mapper.TodoMapper;
import com.abdullah.todo.repository.TodoRepository;
import com.abdullah.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Todo business logic.
 * 
 * Multi-tenancy: All operations are scoped to the current authenticated user.
 * Users can only access their own todos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;
	private final UserRepository userRepository;
	private final TodoMapper todoMapper;

	/**
	 * Get the currently authenticated user.
	 */
	private User getCurrentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("User not found: " + username));
	}

	/**
	 * Retrieves all todos for the current user.
	 */
	@Transactional(readOnly = true)
	public List<TodoResponseDTO> findAll() {
		User owner = getCurrentUser();
		log.info("Fetching all todos for user: {}", owner.getUsername());
		List<Todo> todos = todoRepository.findByOwner(owner);
		log.debug("Found {} todos for user: {}", todos.size(), owner.getUsername());
		return todoMapper.toResponseDTOList(todos);
	}

	/**
	 * Retrieves a single todo by ID for the current user.
	 */
	@Transactional(readOnly = true)
	public TodoResponseDTO findById(Long id) {
		User owner = getCurrentUser();
		log.info("Fetching todo {} for user: {}", id, owner.getUsername());
		Todo todo = todoRepository.findByIdAndOwner(id, owner)
				.orElseThrow(() -> {
					log.warn("Todo {} not found for user: {}", id, owner.getUsername());
					return new TodoNotFoundException(id);
				});
		return todoMapper.toResponseDTO(todo);
	}

	/**
	 * Creates a new todo for the current user.
	 */
	@Transactional
	public TodoResponseDTO create(TodoRequestDTO request) {
		User owner = getCurrentUser();
		log.info("Creating todo for user: {} - title: {}", owner.getUsername(), request.getTitle());

		Todo todo = new Todo(
				request.getTitle(),
				request.getCompleted() != null ? request.getCompleted() : false,
				owner);
		Todo savedTodo = todoRepository.save(todo);

		log.info("Created todo {} for user: {}", savedTodo.getId(), owner.getUsername());
		return todoMapper.toResponseDTO(savedTodo);
	}

	/**
	 * Updates an existing todo for the current user.
	 */
	@Transactional
	public TodoResponseDTO update(Long id, TodoRequestDTO request) {
		User owner = getCurrentUser();
		log.info("Updating todo {} for user: {}", id, owner.getUsername());

		Todo todo = todoRepository.findByIdAndOwner(id, owner)
				.orElseThrow(() -> {
					log.warn("Cannot update - Todo {} not found for user: {}", id, owner.getUsername());
					return new TodoNotFoundException(id);
				});

		if (request.getTitle() != null) {
			todo.setTitle(request.getTitle());
		}
		if (request.getCompleted() != null) {
			todo.setCompleted(request.getCompleted());
		}

		Todo updatedTodo = todoRepository.save(todo);
		log.info("Updated todo {} for user: {}", updatedTodo.getId(), owner.getUsername());
		return todoMapper.toResponseDTO(updatedTodo);
	}

	/**
	 * Deletes a todo for the current user.
	 */
	@Transactional
	public void delete(Long id) {
		User owner = getCurrentUser();
		log.info("Deleting todo {} for user: {}", id, owner.getUsername());

		if (!todoRepository.existsByIdAndOwner(id, owner)) {
			log.warn("Cannot delete - Todo {} not found for user: {}", id, owner.getUsername());
			throw new TodoNotFoundException(id);
		}

		todoRepository.deleteById(id);
		log.info("Deleted todo {} for user: {}", id, owner.getUsername());
	}

	/**
	 * Finds todos by completion status for the current user.
	 */
	@Transactional(readOnly = true)
	public List<TodoResponseDTO> findByCompleted(boolean completed) {
		User owner = getCurrentUser();
		log.info("Fetching todos with completed={} for user: {}", completed, owner.getUsername());
		List<Todo> todos = todoRepository.findByOwnerAndCompleted(owner, completed);
		log.debug("Found {} todos with completed={} for user: {}", todos.size(), completed, owner.getUsername());
		return todoMapper.toResponseDTOList(todos);
	}
}