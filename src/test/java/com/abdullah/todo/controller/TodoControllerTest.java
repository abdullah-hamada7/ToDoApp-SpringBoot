package com.abdullah.todo.controller;

import com.abdullah.todo.dto.TodoRequestDTO;
import com.abdullah.todo.dto.TodoResponseDTO;
import com.abdullah.todo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests with MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TodoController Tests")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private TodoResponseDTO testResponse;
    private TodoRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testResponse = TodoResponseDTO.builder()
                .id(1L)
                .title("Test Todo")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = new TodoRequestDTO();
        testRequest.setTitle("Test Todo");
        testRequest.setCompleted(false);
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("Health check should be accessible without auth")
        void healthCheckShouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/todos/hi"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hey there! Todo API is running."));
        }
    }

    @Nested
    @DisplayName("Protected Endpoints - Without Auth")
    class ProtectedEndpointsWithoutAuth {

        @Test
        @DisplayName("GET /api/todos should return 403 without auth")
        void shouldReturn403WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Protected Endpoints - With Auth")
    @WithMockUser(username = "testuser", roles = { "USER" })
    class ProtectedEndpointsWithAuth {

        @Test
        @DisplayName("GET /api/todos should return todos")
        void shouldReturnTodos() throws Exception {
            when(todoService.findAll()).thenReturn(Arrays.asList(testResponse));

            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Test Todo"));
        }

        @Test
        @DisplayName("GET /api/todos should return empty list")
        void shouldReturnEmptyList() throws Exception {
            when(todoService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("GET /api/todos/{id} should return todo")
        void shouldReturnTodoById() throws Exception {
            when(todoService.findById(1L)).thenReturn(testResponse);

            mockMvc.perform(get("/api/todos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Todo"));
        }

        @Test
        @DisplayName("POST /api/todos should create todo")
        void shouldCreateTodo() throws Exception {
            when(todoService.create(any(TodoRequestDTO.class))).thenReturn(testResponse);

            mockMvc.perform(post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Todo"));
        }

        @Test
        @DisplayName("POST /api/todos with empty title should return 400")
        void shouldReturn400ForEmptyTitle() throws Exception {
            testRequest.setTitle("");

            mockMvc.perform(post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /api/todos/{id} should update todo")
        void shouldUpdateTodo() throws Exception {
            when(todoService.update(anyLong(), any(TodoRequestDTO.class))).thenReturn(testResponse);

            mockMvc.perform(put("/api/todos/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Nested
    @DisplayName("Admin Endpoints")
    class AdminEndpoints {

        @Test
        @DisplayName("DELETE /api/todos/{id} should require ADMIN role")
        @WithMockUser(username = "testuser", roles = { "USER" })
        void shouldRequireAdminRoleForDelete() throws Exception {
            mockMvc.perform(delete("/api/todos/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/todos/{id} should work for ADMIN")
        @WithMockUser(username = "admin", roles = { "USER", "ADMIN" })
        void shouldAllowDeleteForAdmin() throws Exception {
            mockMvc.perform(delete("/api/todos/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
