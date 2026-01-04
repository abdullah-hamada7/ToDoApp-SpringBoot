package com.abdullah.todo.mapper;

import com.abdullah.todo.dto.TodoRequestDTO;
import com.abdullah.todo.dto.TodoResponseDTO;
import com.abdullah.todo.entity.Todo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between Todo entity and DTOs.
 * 
 * In larger projects, consider using MapStruct for automatic mapping.
 * For this project, manual mapping provides clarity and simplicity.
 */
@Component
public class TodoMapper {

    /**
     * Converts a Todo entity to a TodoResponseDTO.
     * 
     * @param todo The entity to convert
     * @return The corresponding response DTO
     */
    public TodoResponseDTO toResponseDTO(Todo todo) {
        if (todo == null) {
            return null;
        }

        return TodoResponseDTO.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .completed(todo.isCompleted())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }

    /**
     * Converts a list of Todo entities to a list of TodoResponseDTOs.
     * 
     * @param todos The list of entities to convert
     * @return The corresponding list of response DTOs
     */
    public List<TodoResponseDTO> toResponseDTOList(List<Todo> todos) {
        return todos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new Todo entity from a TodoRequestDTO.
     * 
     * @param dto The request DTO
     * @return A new Todo entity (not yet persisted)
     */
    public Todo toEntity(TodoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);
        return todo;
    }

    /**
     * Updates an existing Todo entity with data from a TodoRequestDTO.
     * 
     * @param todo The existing entity to update
     * @param dto  The request DTO with new values
     */
    public void updateEntityFromDTO(Todo todo, TodoRequestDTO dto) {
        if (dto.getTitle() != null) {
            todo.setTitle(dto.getTitle());
        }
        if (dto.getCompleted() != null) {
            todo.setCompleted(dto.getCompleted());
        }
    }
}
