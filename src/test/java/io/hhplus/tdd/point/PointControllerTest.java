package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointService pointService;


    @Test
    @DisplayName("/point 요청시 특정 유저의 포인트를 조회한다.")
    void test() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, 1000L);
        given(pointService.getUserPoint(1L)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1000L))
                .andExpect(jsonPath("$.updateMillis").value(1000L))
                .andDo(print());
    }

    @Test
    @DisplayName("/point/{id}/histories 요청시 특정 유저의 포인트를 충전한다.")
    void test2() throws Exception {
        //given
        long userId = 1L;

        List<PointHistory> userPointHistory = Arrays.asList(
                new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, 100L),
                new PointHistory(2L, 1L, 100L, TransactionType.CHARGE, 100L),
                new PointHistory(3L, 1L, 100L, TransactionType.CHARGE, 100L),
                new PointHistory(4L, 1L, 100L, TransactionType.USE, 100L),
                new PointHistory(5L, 1L, 100L, TransactionType.USE, 100L)
        );

        given(pointService.getUserPointHistory(userId)).willReturn(userPointHistory);

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(100L))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[0].updateMillis").value(100L))
                .andDo(print());
    }

    @Test
    @DisplayName("/point/{id}/charge 요청시 특정 유저의 포인트를 충전한다.")
    void test3() throws Exception {
        //given
        long userId = 1L;
        long amount = 100L;

        UserPoint userPoint = new UserPoint(userId, amount, 1000L);
        given(pointService.charge(userId, amount)).willReturn(userPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(100L))
                .andExpect(jsonPath("$.updateMillis").value(1000L))
                .andDo(print());
    }

    @Test
    @DisplayName("/point/{id}/use 요청시 특정 유저의 포인트를 사용한다.")
    void test4() throws Exception {
        //given
        long userId = 1L;
        long amount = 100L;

        UserPoint userPoint = new UserPoint(userId, amount, 1000L);
        given(pointService.use(userId, amount)).willReturn(userPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(100L))
                .andExpect(jsonPath("$.updateMillis").value(1000L))
                .andDo(print());
    }
}