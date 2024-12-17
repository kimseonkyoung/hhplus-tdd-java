package io.hhplus.tdd.point;

import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)  // Mockito 확장을 활성화
public class PointServiceTest {

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
    }

    @Test
    @DisplayName("포인트 조회: 실페 케이스 -> 특정 회원의 사용자 ID가 유효하지 않을 때(존재X) 사용 요청이 실패하고 예외처리를 반환한다.")
    void test2() {
        //given
        long userId = 1L;
        given(userPointTable.selectById(userId)).willReturn(null);

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> pointService.getUserPoint(userId));
    }
}


