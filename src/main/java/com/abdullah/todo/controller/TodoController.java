package com.abdullah.todo.controller;

import com.abdullah.todo.dto.TodoRequestDTO;
import com.abdullah.todo.dto.TodoResponseDTO;
import com.abdullah.todo.exception.ErrorResponse;
import com.abdullah.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Todo API endpoints.
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Todo", description = "Todo management API")
public class TodoController {

	private final TodoService todoService;

	@Operation(summary = "Health check", description = "Simple endpoint to verify the API is running")
	@ApiResponse(responseCode = "200", description = "API is running")
	@GetMapping("/hi")
	public String sayHi() {
		log.info("Health check endpoint called");
		return "Hey there! Todo API is running.";
	}

	@Operation(summary = "Get all todos", description = "Retrieves all todos, optionally filtered by completion status")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved todos", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TodoResponseDTO.class))))
	@GetMapping
	public ResponseEntity<List<TodoResponseDTO>> getAllTodos(
			@Parameter(description = "Filter by completion status") @RequestParam(required = false) Boolean completed) {

		log.info("GET /api/todos - completed filter: {}", completed);

		List<TodoResponseDTO> todos;
		if (completed != null) {
			todos = todoService.findByCompleted(completed);
		} else {
			todos = todoService.findAll();
		}

		return ResponseEntity.ok(todos);
	}

	@Operation(summary = "Get todo by ID", description = "Retrieves a specific todo by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Todo found", content = @Content(schema = @Schema(implementation = TodoResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Todo not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/{id}")
	public ResponseEntity<TodoResponseDTO> getTodoById(
			@Parameter(description = "ID of the todo to retrieve") @PathVariable Long id) {
		log.info("GET /api/todos/{}", id);
		TodoResponseDTO todo = todoService.findById(id);
		return ResponseEntity.ok(todo);
	}

	@Operation(summary = "Create a new todo", description = "Creates a new todo item")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Todo created successfully", content = @Content(schema = @Schema(implementation = TodoResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping
	public ResponseEntity<TodoResponseDTO> createTodo(
			@Valid @RequestBody TodoRequestDTO request) {

		log.info("POST /api/todos - title: {}", request.getTitle());
		TodoResponseDTO created = todoService.create(request);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	@Operation(summary = "Update a todo", description = "Fully updates an existing todo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Todo updated successfully", content = @Content(schema = @Schema(implementation = TodoResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Todo not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PutMapping("/{id}")
	public ResponseEntity<TodoResponseDTO> updateTodo(
			@Parameter(description = "ID of the todo to update") @PathVariable Long id,
			@Valid @RequestBody TodoRequestDTO request) {

		log.info("PUT /api/todos/{} - title: {}", id, request.getTitle());
		TodoResponseDTO updated = todoService.update(id, request);
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "Partially update a todo", description = "Updates specific fields of a todo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Todo updated successfully", content = @Content(schema = @Schema(implementation = TodoResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Todo not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PatchMapping("/{id}")
	public ResponseEntity<TodoResponseDTO> patchTodo(
			@Parameter(description = "ID of the todo to patch") @PathVariable Long id,
			@RequestBody TodoRequestDTO request) {

		log.info("PATCH /api/todos/{}", id);
		TodoResponseDTO updated = todoService.update(id, request);
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "Delete a todo", description = "Deletes a todo by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Todo not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTodo(
			@Parameter(description = "ID of the todo to delete") @PathVariable Long id) {
		log.info("DELETE /api/todos/{}", id);
		todoService.delete(id);
		return ResponseEntity.noContent().build();
	}
}