package org.example.expert.domain.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.repository.TodoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TodoIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    TodoRepository todoRepository;
    @Autowired
    ObjectMapper objectMapper;

    private static final String EMAIL = "user1@example.com";
    private static final String PASSWORD = "password";
    private static final String USER_ROLE = "user";

    private static final String TITLE = "title";
    private static final String CONTENTS = "contents";

    @Test
    void 일정_생성_통합_테스트() throws Exception {
        // 1. given
        String bearerToken = getBearerTokenBySignup();

        TodoSaveRequest todoSaveRequest = new TodoSaveRequest(TITLE, CONTENTS);

        // 2. when
        ResultActions saveTodoResult = mockMvc.perform(
                post("/todos")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveRequest))
        );
        String todoAsString = saveTodoResult.andReturn().getResponse().getContentAsString();
        TodoSaveResponse todoResponse = objectMapper.readValue(todoAsString, TodoSaveResponse.class);

        // 3. then
        saveTodoResult.andExpect(status().isOk());
        assertEquals(TITLE, todoResponse.getTitle());
        assertEquals(CONTENTS, todoResponse.getContents());
        assertThat(todoRepository.findById(todoResponse.getId())).isPresent();
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