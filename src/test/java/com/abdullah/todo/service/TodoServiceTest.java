package com.abdullah.todo.service;

import com.abdullah.todo.dto.TodoRequestDTO;
import com.abdullah.todo.dto.TodoResponseDTO;
import com.abdullah.todo.entity.Todo;
import com.abdullah.todo.entity.User;
import com.abdullah.todo.exception.TodoNotFoundException;
import com.abdullah.todo.mapper.TodoMapper;
import com.abdullah.todo.repository.TodoRepository;
import com.abdullah.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoService with multi-tenancy.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Unit Tests")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private TodoMapper todoMapper = new TodoMapper();

    @InjectMocks
    private TodoService todoService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User testUser;
    private Todo testTodo;
    private TodoRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        // Create test user
        testUser = new User("testuser", "password", Set.of("ROLE_USER"));
        testUser.setId(1L);

        // Create test todo
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setCompleted(false);
        testTodo.setOwner(testUser);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());

        // Create test request
        testRequest = new TodoRequestDTO();
        testRequest.setTitle("Test Todo");
        testRequest.setCompleted(false);

        // Mock user repository
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all todos for current user")
        void shouldReturnAllTodosForCurrentUser() {
            Todo todo2 = new Todo();
            todo2.setId(2L);
            todo2.setTitle("Second Todo");
            todo2.setCompleted(true);
            todo2.setOwner(testUser);
            todo2.setCreatedAt(LocalDateTime.now());

            when(todoRepository.findByOwner(testUser)).thenReturn(Arrays.asList(testTodo, todo2));

            List<TodoResponseDTO> result = todoService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Todo");
            assertThat(result.get(1).getTitle()).isEqualTo("Second Todo");
            verify(todoRepository, times(1)).findByOwner(testUser);
        }

        @Test
        @DisplayName("Should return empty list when user has no todos")
        void shouldReturnEmptyListWhenNoTodosExist() {
            when(todoRepository.findByOwner(testUser)).thenReturn(Collections.emptyList());

            List<TodoResponseDTO> result = todoService.findAll();

            assertThat(result).isEmpty();
            verify(todoRepository, times(1)).findByOwner(testUser);
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return todo when found for current user")
        void shouldReturnTodoWhenFound() {
            when(todoRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.of(testTodo));

            TodoResponseDTO result = todoService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Todo");
            verify(todoRepository, times(1)).findByIdAndOwner(1L, testUser);
        }

        @Test
        @DisplayName("Should throw TodoNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(todoRepository.findByIdAndOwner(anyLong(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.findById(999L))
                    .isInstanceOf(TodoNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create todo for current user")
        void shouldCreateTodoForCurrentUser() {
            when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

            TodoResponseDTO result = todoService.create(testRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Todo");
            verify(todoRepository, times(1)).save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update todo for current user")
        void shouldUpdateTodoForCurrentUser() {
            TodoRequestDTO updateRequest = new TodoRequestDTO();
            updateRequest.setTitle("Updated Title");
            updateRequest.setCompleted(true);

            Todo updatedTodo = new Todo();
            updatedTodo.setId(1L);
            updatedTodo.setTitle("Updated Title");
            updatedTodo.setCompleted(true);
            updatedTodo.setOwner(testUser);
            updatedTodo.setCreatedAt(testTodo.getCreatedAt());
            updatedTodo.setUpdatedAt(LocalDateTime.now());

            when(todoRepository.findByIdAndOwner(1L, testUser)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

            TodoResponseDTO result = todoService.update(1L, updateRequest);

            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.isCompleted()).isTrue();
            verify(todoRepository, times(1)).findByIdAndOwner(1L, testUser);
            verify(todoRepository, times(1)).save(any(Todo.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent todo")
        void shouldThrowExceptionWhenUpdatingNonExistentTodo() {
            when(todoRepository.findByIdAndOwner(anyLong(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.update(999L, testRequest))
                    .isInstanceOf(TodoNotFoundException.class);
            verify(todoRepository, never()).save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete todo for current user")
        void shouldDeleteTodoForCurrentUser() {
            when(todoRepository.existsByIdAndOwner(1L, testUser)).thenReturn(true);
            doNothing().when(todoRepository).deleteById(1L);

            todoService.delete(1L);

            verify(todoRepository, times(1)).existsByIdAndOwner(1L, testUser);
            verify(todoRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent todo")
        void shouldThrowExceptionWhenDeletingNonExistentTodo() {
            when(todoRepository.existsByIdAndOwner(anyLong(), any())).thenReturn(false);

            assertThatThrownBy(() -> todoService.delete(999L))
                    .isInstanceOf(TodoNotFoundException.class);
            verify(todoRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("findByCompleted() Tests")
    class FindByCompletedTests {

        @Test
        @DisplayName("Should return completed todos for current user")
        void shouldReturnCompletedTodos() {
            Todo completedTodo = new Todo();
            completedTodo.setId(2L);
            completedTodo.setTitle("Completed Todo");
            completedTodo.setCompleted(true);
            completedTodo.setOwner(testUser);
            completedTodo.setCreatedAt(LocalDateTime.now());

            when(todoRepository.findByOwnerAndCompleted(testUser, true))
                    .thenReturn(Collections.singletonList(completedTodo));

            List<TodoResponseDTO> result = todoService.findByCompleted(true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isCompleted()).isTrue();
            verify(todoRepository, times(1)).findByOwnerAndCompleted(testUser, true);
        }
    }
}
