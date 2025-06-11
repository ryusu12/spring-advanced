package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private  UserRepository userRepository;
    @Mock
    private  PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void 유저를_찾지_못하면_예외가_발생한다() {
        // 1. given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.getUser(userId)
        );

        // 3. then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void 새_비밀번호와_기존_비밀번호가_같으면_예외가_발생한다() {
        // 1. given
        long userId = 1L;
        String password = "password123!";
        UserChangePasswordRequest request = new UserChangePasswordRequest(password, password);

        User user = new User("user1@example.com", password, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(userId, request)
        );

        // 3. then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 비밀번호가_틀리면_예외가_발생한다() {
        // 1. given
        long userId = 1L;
        String password = "password123!";
        UserChangePasswordRequest request = new UserChangePasswordRequest("password", "newPassword123!");

        User user = new User("user1@example.com", password, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(userId, request)
        );

        // 3. then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    void id로_유저_조회를_성공한다() {
        // 1. given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 2. when
        UserResponse userResponse = userService.getUser(userId);

        // 3. then
        assertEquals(user.getId(), userResponse.getId());
        assertEquals(user.getEmail(), userResponse.getEmail());
    }

    @Test
    void 비밀번호_변경을_성공한다() {
        // 1. given
        long userId = 1L;
        String password = "password123!";
        UserChangePasswordRequest request = new UserChangePasswordRequest(password, "newPassword123!");

        User user = new User("user1@example.com", password, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(request.getNewPassword())).willReturn("encodedPassword");

        // 2. when
        userService.changePassword(userId, request);

        // 3. then
        assertEquals("encodedPassword", user.getPassword());
    }

}