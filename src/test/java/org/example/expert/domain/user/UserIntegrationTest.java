package org.example.expert.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String EMAIL = "user1@example.com";
    private static final String PASSWORD = "password";
    private static final String USER_ROLE = "user";
    private static final Long USER_ID = 1L;

    @Test
    void 유저_조회_통합_테스트() throws Exception {
        // 1. given
        String bearerToken = getBearerTokenBySignup();

        // 2. when
        ResultActions getUserResult = mockMvc.perform(
                get("/users/{userId}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
        );
        String userAsString = getUserResult.andReturn().getResponse().getContentAsString();
        UserResponse userResponse = objectMapper.readValue(userAsString, UserResponse.class);

        // 3. then
        getUserResult.andExpect(status().isOk());

        assertThat(userResponse.getId()).isGreaterThan(0L);
        assertEquals(EMAIL, userResponse.getEmail());
    }

    @Test
    void 비밀번호_변경_통합_테스트() throws Exception {
        // 1. given
        String bearerToken = getBearerTokenBySignup();

        String newPassword = "newPassword123!";

        UserChangePasswordRequest request = new UserChangePasswordRequest(PASSWORD, newPassword);

        // 2. when
        ResultActions changePasswordResult = mockMvc.perform(
                put("/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // 3. then
        changePasswordResult.andExpect(status().isOk());
        User updatedUser = userRepository.findByEmail(EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    private String getBearerTokenBySignup() throws Exception {
        SignupRequest signupRequest = new SignupRequest(EMAIL, PASSWORD, USER_ROLE);

        String signupAsString = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(signupAsString)
                .get("bearerToken")
                .asText();
    }

}