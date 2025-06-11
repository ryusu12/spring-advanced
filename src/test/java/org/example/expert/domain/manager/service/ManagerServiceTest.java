package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // 1. given
        // saveManager
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

        long todoId = 1L;
        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        long managerUserId = 2L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        // deleteManager
        User user = User.fromAuthUser(authUser);
        long managerId = 1L;


        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 2. when
        InvalidRequestException exception1 = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        InvalidRequestException exception2 = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        // 3. then
        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception1.getMessage());
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception2.getMessage());
    }

    @Test
    void todo의_user와_authUser가_다른_경우_예외가_발생한다() {
        // 1. given
        // saveManager
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

        long todoId = 1L;
        User other = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(other, "id", 2L);
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", other);

        long managerUserId = 2L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        // deleteManager
        User user = User.fromAuthUser(authUser);
        long managerId = 1L;


        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 2. when
        InvalidRequestException exception1 = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        InvalidRequestException exception2 = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        // 3. then
        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception1.getMessage());
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception2.getMessage());
    }

    @Test
    void todo의_user와_managerUser가_같은_경우_예외가_발생한다() {
        // 1. given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(userId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        // 3. then
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    void todo의_user와_manager의_user가_다를_경우_예외가_발생한다() {
        // 1. given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long otherTodoId = 2L;
        Todo otherTodo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(otherTodo, "id", otherTodoId);

        long managerId = 1L;
        Manager manager = new Manager(user, otherTodo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );
        // 3. then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }


    @Test
    void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void manager가_정상적으로_삭제된다() {
        // 1. given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);

        long todoId = 1L;
        Todo todo = new Todo("Title", "Contents", "Sunny", user);

        long managerId = 1L;
        Manager manager = new Manager(user, todo);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // 2. when
        managerService.deleteManager(userId, todoId, managerId);

        // 3. then
        verify(managerRepository, times(1)).delete(manager);
    }
}