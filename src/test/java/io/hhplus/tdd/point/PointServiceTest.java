package io.hhplus.tdd.point;


import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.hhplus.tdd.PointException.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)  // Mockito 확장을 활성화
public class PointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);


    @Mock
    private UserPointTable userPointTable;  // Mock 객체 생성

    @Mock
    private PointHistoryTable pointHistoryTable;  // Mock 객체 생성

    @InjectMocks
    private PointService pointService;  // 의존성 주입 자동 처리

    @Test
    @DisplayName("포인트 조회: 성공케이스 -> 특정 회원의 포인트 잔액이 정확히 반환된다.")
    void test1() {
        // given
        long userId = 1L;
        long initialPoints = 1_000L;
        long initialMillis = 1_000L;
        given(userPointTable.selectById(userId))
                .willReturn(new UserPoint(userId, initialPoints, initialMillis));

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

    @Test
    @DisplayName("특정 사용자 포인트 충전 성공")
    void test5(){
        //given
        long userId = 1L;
        long currentPoints = 100L;
        long chargePoints = 100L;
        given(userPointTable.selectById(userId))
                .willReturn(new UserPoint(userId, currentPoints, 1000L));
        given(userPointTable.insertOrUpdate(userId, currentPoints + chargePoints))
                .willReturn(new UserPoint(userId, currentPoints + chargePoints, 1000L));
        //when
        UserPoint userPoints = pointService.charge(userId, chargePoints);

        assertEquals(200L, userPoints.point());
        log.info("Success message: = 유저 포인트 [{}]", userPoints.point());
    }

    @Test
    @DisplayName("특정 사용자 포인트 연속 두 번 충전 성공")
    void test6(){
        //given
        long userId = 1L;
        long currentPoints = 100L;
        long firstChargePoints = 100L;
        long secondChargePoints = 200L;
        // Mock 설정 - 각각의 충전 결과 반환
        given(userPointTable.selectById(userId))
                .willReturn(
                        new UserPoint(userId, currentPoints, 100L),
                        new UserPoint(userId, currentPoints + firstChargePoints, 100L)
                );

        given(userPointTable.insertOrUpdate(userId, currentPoints + firstChargePoints))
                .willReturn(new UserPoint(userId, currentPoints + firstChargePoints, 100L));

        given(userPointTable.insertOrUpdate(userId, currentPoints + firstChargePoints + secondChargePoints))
                .willReturn(new UserPoint(userId, currentPoints + firstChargePoints + secondChargePoints, 300L));

        //when
        UserPoint firstGetUserPoints = pointService.charge(userId, firstChargePoints);

        //when
        UserPoint secondGetUserPoints = pointService.charge(userId, secondChargePoints);

        assertEquals(200L, firstGetUserPoints.point());
        assertEquals(400L, secondGetUserPoints.point());

        // Verify - 호출 횟수 검증 (BDD Mockito 방식)
        then(userPointTable).should(times(1)).insertOrUpdate(userId, currentPoints + firstChargePoints);
        then(userPointTable).should(times(1)).insertOrUpdate(userId, currentPoints + firstChargePoints + secondChargePoints);

        log.info("Success message: = 유저 포인트 [{}]", firstGetUserPoints.point());
        log.info("Success message: = 유저 포인트 [{}]", secondGetUserPoints.point());
    }

    @Test
    @DisplayName("포인트 충전시 사용자 ID가 '0'일시 예외 발생")
    void test7() {
        //given
        long userId = 0L;
        long chargePoints = 1000L;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 충전시 사용자 ID가 음수일시 예외 발생")
    void test8() {
        //given
        long userId = -1L;
        long chargePoints = 1000L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 충전시 존재하지 않는 사용자 예외 발생")
    void test9(){
        //given
        long userId = 1L;
        long chargePoints = 100L;
        given(userPointTable.selectById(userId)).willReturn(null);

        //when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 유저를 찾을 수 없습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 '0'일시 예외 발생")
    void test10(){
        //given
        long userId = 1L;
        long chargePoints = 0L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, chargePoints, 1000L));

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 충전 포인트가 올바르지 않습니다: " + chargePoints, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 음수일시 예외 발생")
    void test11(){
        //given
        long userId = 1L;
        long chargePoints = -100L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, chargePoints, 1000L));

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 충전 포인트가 올바르지 않습니다: " + chargePoints, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 최소충전포인트(10p) 이하 예외 발생")
    void test12(){
        //given
        long userId = 1L;
        long chargePoints = 5L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, chargePoints, 1000L));

        //when & then
        InsufficientChargePointsException exception = assertThrows(InsufficientChargePointsException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "최소 충전 10이상의 포인트를 충전해야합니다.", exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("충전 포인트를 반영했을 때 최대 보유가능 포인트 이상일시(1000p) 예외 발생")
    void test13(){
        //given
        long userId = 1L;
        long chargePoints = 200L;
        long currentPoints = 900L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, currentPoints, 1000L));

        //when & then
        MaximumChargePointsExceededException exception = assertThrows(MaximumChargePointsExceededException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "최대 보유 가능 " + 1000L + "포인트를 넘었습니다.", exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("특정 사용자 포인트 사용 성공")
    void test14(){
        //given
        long userId = 1L;
        long usePoints = 100L;
        given(userPointTable.selectById(userId))
                .willReturn(new UserPoint(userId, 200L, 1000L));
        given(userPointTable.insertOrUpdate(userId, usePoints))
                .willReturn(new UserPoint(userId, usePoints, 1000L));
        //when
        UserPoint userPoints = pointService.use(userId, usePoints);

        assertEquals(100L, userPoints.point());
        log.info("Success message: = 유저 포인트 [{}]", userPoints.point());
    }

    @Test
    @DisplayName("특정 사용자 포인트 연속 두 번 사용 성공")
    void test15(){
        //given
        long userId = 1L;
        long currentPoints = 500L;
        long firstUsePoints = 100L;
        long secondUsePoints = 100L;

        // Mock 설정 - 각각의 사용 결과 반환
        given(userPointTable.selectById(userId))
                .willReturn(
                        new UserPoint(userId, currentPoints, 300L),
                        new UserPoint(userId, currentPoints- firstUsePoints, 300L)
        );

        given(userPointTable.insertOrUpdate(userId, currentPoints - firstUsePoints))
                .willReturn(new UserPoint(userId, currentPoints - firstUsePoints, 100L));

        given(userPointTable.insertOrUpdate(userId, currentPoints - firstUsePoints - secondUsePoints))
                .willReturn(new UserPoint(userId, currentPoints - firstUsePoints - secondUsePoints, 300L));


        //when
        UserPoint firstGetUserPoints = pointService.use(userId, firstUsePoints);
        UserPoint secondGetUserPoints = pointService.use(userId, secondUsePoints);

        assertEquals(400L, firstGetUserPoints.point());
        assertEquals(300L, secondGetUserPoints.point());

        // Verify - 호출 횟수 검증 (BDD Mockito 방식)
        then(userPointTable).should(times(1)).insertOrUpdate(userId, 400L);  // 첫 번째 사용 후 포인트
        then(userPointTable).should(times(1)).insertOrUpdate(userId, 300L);  // 두 번째 사용 후 포인트

        log.info("Success message: = 유저 포인트 [{}]", firstGetUserPoints.point());
        log.info("Success message: = 유저 포인트 [{}]", secondGetUserPoints.point());
    }

    @Test
    @DisplayName("포인트 사용시 유저 ID가 '0'일시 예외 발생")
    void test17() {
        //given
        long userId = 0L;
        long usePoints = 1000L;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(userId, usePoints));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 사용시 유저 ID가 음수일시 예외 발생")
    void test18() {
        //given
        long userId = -1L;
        long chargePoints = 100L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(userId, chargePoints));

        assertEquals( "해당 유저 아이디가 올바르지 않습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 충전시 존재하지 않는 사용자 예외 발생")
    void test19(){
        //given
        long userId = 1L;
        long chargePoints = 100L;
        given(userPointTable.selectById(userId)).willReturn(null);

        //when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> pointService.charge(userId, chargePoints));

        assertEquals( "해당 유저를 찾을 수 없습니다: " + userId, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 '0'일시 예외 발생")
    void test20(){
        //given
        long userId = 1L;
        long usePoints = 0L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, usePoints, 1000L));

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(userId, usePoints));

        assertEquals( "해당 사용 포인트가 올바르지 않습니다: " + usePoints, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 음수일시 예외 발생")
    void test21(){
        //given
        long userId = 1L;
        long usePoints = -100L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, usePoints, 1000L));

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(userId, usePoints));

        assertEquals( "해당 사용 포인트가 올바르지 않습니다: " + usePoints, exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("포인트가 최소충전포인트(10p) 이하 예외 발생")
    void test22(){
        //given
        long userId = 1L;
        long usePoints = 5L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, usePoints, 1000L));

        //when & then
        InsufficientUsePointsException exception = assertThrows(InsufficientUsePointsException.class,
                () -> pointService.use(userId, usePoints));

        assertEquals( "최소 " + 10 + "이상의 포인트를 사용해야합니다.", exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }

    @Test
    @DisplayName("사용 포인트를 반영했을 때 최소 보유 포인트 이하일시(0P) 예외 발생")
    void test23(){
        //given
        long userId = 1L;
        long usePoints = 200L;
        long currentPoints = 100L;
        given(userPointTable.selectById(userId)).willReturn(new UserPoint(userId, currentPoints, 1000L));

        //when & then
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> pointService.use(userId, usePoints));

        assertEquals( "잔여 포인트가 부족합니다. 현재 잔액 " + currentPoints + "P", exception.getMessage());
        log.info("Exception message: = [{}]", exception.getMessage());
    }
}


