package org.example.expert.domain.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentAdminIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ObjectMapper objectMapper;

    private static final String EMAIL = "user1@example.com";
    private static final String PASSWORD = "password";
    private static final Long COMMENT_ID = 1L;

    private static final String TITLE = "title";
    private static final String CONTENTS = "contents";

    @Test
    void user가_접근하면_예외가_발생한다() throws Exception {
        // 1. given
        String bearerToken = getBearerTokenBySignup("user");

        User user = new User(EMAIL, PASSWORD, UserRole.USER);
        Todo todo = new Todo(TITLE, CONTENTS, "sun", user);
        Comment comment = new Comment(CONTENTS, user, todo);
        ReflectionTestUtils.setField(comment, "id", COMMENT_ID);

        // 2. when
        ResultActions deleteComment = mockMvc.perform(
                delete("/admin/comments/{commentId}", COMMENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
        );

        // 3. then
        deleteComment.andExpect(status().isForbidden());
        assertThat(commentRepository.findById(COMMENT_ID)).isEmpty();
    }

    @Test
    void 댓글_삭제_통합_테스트() throws Exception {
        // 1. given
        String bearerToken = getBearerTokenBySignup("admin");

        User user = new User(EMAIL, PASSWORD, UserRole.USER);
        Todo todo = new Todo(TITLE, CONTENTS, "sun", user);
        Comment comment = new Comment(CONTENTS, user, todo);
        ReflectionTestUtils.setField(comment, "id", COMMENT_ID);

        // 2. when
        ResultActions deleteComment = mockMvc.perform(
                delete("/admin/comments/{commentId}", COMMENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
        );

        // 3. then
        deleteComment.andExpect(status().isOk());
        assertThat(commentRepository.findById(COMMENT_ID)).isEmpty();
    }

    private String getBearerTokenBySignup(String userRole) throws Exception {
        SignupRequest signupRequest = new SignupRequest(EMAIL, PASSWORD, userRole);

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