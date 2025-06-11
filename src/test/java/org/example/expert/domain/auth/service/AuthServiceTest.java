package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void 이미_존재하는_이메일이면_에외가_발생한다() {
        // 1. given
        SignupRequest signupRequest = new SignupRequest("user1@example.com", "password", "user");

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(signupRequest)
        );

        // 3. then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    void 가입되지않은_유저라면_예외가_발생한다() {
        // 1. given
        SigninRequest signinRequest = new SigninRequest("user1@example.com", "password");

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        // 2. when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signin(signinRequest)
        );
        // 3. then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    void 잘못된_비밀번호라면_예외가_발생한다() {
        // 1. given
        SigninRequest signinRequest = new SigninRequest("user1@example.com", "password");

        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);

        // 2. when
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.signin(signinRequest)
        );
        // 3. then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    void 회원가입을_성공한다() {
        // 1. given
        SignupRequest signupRequest = new SignupRequest("user1@example.com", "password", "user");

        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        String token = "token123";

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn(token);

        // 2. when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // 3. then
        assertEquals(token, signupResponse.getBearerToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 로그인을_성공한다() {
        // 1. given
        SigninRequest signinRequest = new SigninRequest("user1@example.com", "password");

        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        String token = "token123";

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn(token);

        // 2. when
        SigninResponse signinResponse = authService.signin(signinRequest);

        // 3. then
        assertEquals(token, signinResponse.getBearerToken());
    }

}