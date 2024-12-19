package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class PointCuncrrentTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

    @Autowired
    private PointService pointService;  // 의존성 주입 자동 처리

    @Autowired
    private UserPointTable userPointTable;

    @Test
    @DisplayName("동시성 제어 충전 테스트")
    void test1() throws InterruptedException{
        long userId = 1L;
        long initialPoints = 0L;
        long chargeAmount = 10;
        int threadCount = 99;

        userPointTable.insertOrUpdate(userId, initialPoints);

        // 동시 실행 테스트 설정
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티스레드 포인트 충전 시뮬레이션
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.charge(userId, chargeAmount);
                } finally {
                    latch.countDown();  // 완료 시 감소
                }
            });
        }

        latch.await();  // 모든 작업 종료 대기
        executor.shutdown();  // 스레드 풀 종료
        //충전 후 유저 포인트 조회
        UserPoint userPoint = pointService.getUserPoint(userId);
        // 예상 값 계산
        long expectedPoints = initialPoints + (threadCount * chargeAmount);

        assertEquals(expectedPoints, userPoint.point());

        log.info("테스트 예상: 예상 포인트: " + expectedPoints + ", 실제 포인트: " + userPoint.point());
    }

    @Test
    @DisplayName("동시성 충전 테스트")
    void test2() throws InterruptedException{
        long userId = 1L;
        long initialPoints = 999;
        long useAmount = 10;
        int threadCount = 99;

        userPointTable.insertOrUpdate(userId, initialPoints);

        // 동시 실행 테스트 설정
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티스레드 포인트 충전 시뮬레이션
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.use(userId, useAmount);
                } finally {
                    latch.countDown();  // 완료 시 감소
                }
            });
        }

        latch.await();  // 모든 작업 종료 대기
        executor.shutdown();  // 스레드 풀 종료
        //사용 후 유저 포인트 조회
        UserPoint userPoint = pointService.getUserPoint(userId);
        // 예상 값 계산
        long expectedPoints = initialPoints - (threadCount * useAmount);

        assertEquals(expectedPoints, userPoint.point());

        log.info("테스트 예상: 예상 포인트: " + expectedPoints + ", 실제 포인트: " + userPoint.point());
    }
}
