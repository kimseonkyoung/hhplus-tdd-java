package io.hhplus.tdd.point;

import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)  // Mockito 확장을 활성화
public class PointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);


    @Mock
    private UserPointTable userPointTable;  // Mock 객체 생성

    @InjectMocks
    private PointService pointService;  // 의존성 주입 자동 처리

    @Test
    @DisplayName("포인트 조회: 성공케이스 -> 특정 회원의 포인트 잔액이 정확히 반환된다.")
    void test1() {
        // given
        long userId = 1L;
        long initialPoints = 1_000L;
        long initialMillis = 1_000L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, initialPoints, initialMillis));

        // when
        UserPoint userPoints = pointService.getUserPoint(userId);

        // then
        assertEquals(initialPoints, userPoints.point());
        log.info("Success message: = 유저 포인트 [{}]", userPoints.point());

    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void test2() {
        //given
        long userId = 1L;
        given(userPointTable.selectById(userId)).willReturn(null);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> pointService.getUserPoint(userId));

        assertEquals( "해당 유저를 찾을 수 없습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 ID가 '0'일시 예외 발생")
    void test3() {
        //given
        long userId = 0L;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.getUserPoint(userId));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 ID가 음수일시 예외 발생")
    void test4() {
        //given
        long userId = -1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.getUserPoint(userId));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }
}


