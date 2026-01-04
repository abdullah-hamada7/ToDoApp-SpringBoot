package com.abdullah.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Todo creation and update requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a Todo")
public class TodoRequestDTO {

    @Schema(description = "The title of the todo item", example = "Buy groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required and cannot be blank")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Schema(description = "Completion status of the todo", example = "false", defaultValue = "false")
    private Boolean completed;
}
