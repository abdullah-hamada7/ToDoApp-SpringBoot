package com.abdullah.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Todo responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing Todo details")
public class TodoResponseDTO {

    @Schema(description = "Unique identifier of the todo", example = "1")
    private Long id;

    @Schema(description = "The title of the todo item", example = "Buy groceries")
    private String title;

    @Schema(description = "Whether the todo is completed", example = "false")
    private boolean completed;

    @Schema(description = "Timestamp when the todo was created", example = "2026-01-04T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the todo was last updated", example = "2026-01-04T12:30:00")
    private LocalDateTime updatedAt;
}
