# hhplus-tdd-java
항해플러스 1주차 TDD로 개발하기(2024.12.14 ~ 2024.12.20)
## 1. 동시성 제어 실패 케이스 테스트
### 코드


```Java
    @Test
    @DisplayName("동시성 제어 테스트 실패 케이스")
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

        log.info("테스트 실패 예상: 예상 포인트: " + expectedPoints + ", 실제 포인트: " + userPoint.point());
    }
```

### 결과
![image](https://github.com/user-attachments/assets/39a27046-4c63-4d76-8993-c167530f7549)

## 2. 동시성 제어 성공 케이스 테스트
### synchronized
### service method에 추가
![image](https://github.com/user-attachments/assets/cf314338-88f9-4d16-94ec-cd2f4bf18532)

혹은
블록식으로도 가능하나, 동기화 경계를 잘못 설정하면 데이터 일관성 문제가 발생할 수 있으니 주의하자.
동기화가 필요한 코드만 잠금 처리하여 성능이 향상될 수는 있다.
### 테스트코드(위와 동일하다.)
### 결과
![image](https://github.com/user-attachments/assets/f36ed4e3-864e-44dd-a7d3-9c6309225e65)

### 테스트는 통과했지만, 엄청 오래걸린다.

#### 이유

Synchronized 키워드는 자바에서 스레드 안전성을 보장하지만 성능 저하의 주요 원인으로 작용한다. 첫 번째 원인은 **스레드 경합(Thread Contention)**이다. 여러 스레드가 동시에 동일한 동기화 블록이나 메서드에 접근하려고 하면, 자원을 얻기 위해 대기해야 하는 스레드가 늘어나면서 대기 시간이 증가시키기 때문이다.

두 번째 원인은 **컨텍스트 스위칭 비용(Context Switching)**이다. 스레드가 실행 상태에서 대기 상태로 전환되거나, 대기 중인 스레드가 다시 실행될 때 발생하는 전환 비용은 시스템 리소스를 소모하고 성능을 저하시킨다.

세 번째로, 잠금(Lock) 유지 비용도 성능 저하의 주된 원인이다. 동기화된 블록이나 메서드가 오래 실행되면, 해당 자원을 사용하는 동안 다른 스레드가 계속 대기해야 하므로 전체적인 응답 속도가 느려진다.

마지막으로, 불필요한 동기화는 시스템 자원의 낭비를 초래한다. 동기화가 필요하지 않은 코드에도 synchronized 키워드를 남용하면, 자원 접근이 제한되어 병렬 처리가 불가능해지고 성능이 크게 저하될 수 있다. 이러한 원인은 멀티스레드 환경에서 성능 문제를 발생시키므로, 동기화 사용 시 신중한 설계가 필요하다.

## 3. 다른 해결책 (Reentrantlock)
![image](https://github.com/user-attachments/assets/ddbddb20-f475-4962-9eef-061b3a66bf71)

ReentrantLock은 동기화 제어 범위를 명확히 관리할 수 있어 불필요한 대기 시간을 줄이는 데 유리하고 한다. 락이 필요한 코드만 잠금을 적용하므로 성능이 향상되며, lock.unlock()을 반드시 finally 블록에서 호출해 데드락 발생을 방지할 수 있다. 또한, ReentrantLock은 tryLock()을 통해 락 획득을 시도하고 실패 시 대기하지 않도록 하거나, lockInterruptibly()로 인터럽트 가능한 대기를 지원한다. 필요 시 공정성(Fairness) 옵션을 설정해 대기 중인 스레드가 공정하게 락을 획득할 수 있도록 설정할 수도 있다. 이러한 기능을 통해 ReentrantLock은 성능과 유연성을 동시에 확보할 수 있으며, 예외 발생 시에도 finally 블록을 활용해 안정적인 락 해제를 보장한다고..

![image](https://github.com/user-attachments/assets/d13667d5-494a-47af-9d55-cd53ebb49f17)

아니 근데 왜 reentrantlock이 시간이 더 많이 나오지?

### 마지막으로
해당 충전에 적용한 reentrantlock 다른 곳에도 적용

### etc
난 동시성 제어를 테스트할 때 따로 제어테스트 클래스를 파서
@springbootTest에 @AutoConfigureMockMvc로 필요한 pointService, userPointTable를 @AutoWired를 주입받아 썼는데
mokito를 이용해서 synchronized나 reentrantlock을 이용하는 방법도 있긴 있다.
명확히 이해하고 쓰는 게 맞는 것 같아서 이건 나중에 다시 테스트 시도해보는 걸로.


```

   @Test
    @DisplayName("동시성 제어 테스트: synchronized 키워드")
    void test30() throws InterruptedException {
        long userId = 1L;
        long initialPoints = 0L;
        long chargeAmount = 10;
        int threadCount = 99;

        // 목 객체의 상태 관리
        AtomicLong currentPoints = new AtomicLong(initialPoints);

        // Mockito 설정: selectById 호출 시 현재 포인트 반환
        given(mockUserPointTable.selectById(userId))
                .willAnswer(invocation -> new UserPoint(userId, currentPoints.get(), 1000L));

        // Mockito 설정: insertOrUpdate 호출 시 값 업데이트
        given(mockUserPointTable.insertOrUpdate(eq(userId), anyLong()))
                .willAnswer(invocation -> {
                    long newPoints = invocation.getArgument(1, Long.class);
                    currentPoints.set(newPoints);  // 상태 업데이트
                    return new UserPoint(userId, newPoints, System.currentTimeMillis());
                });

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

        UserPoint userPoint = pointService.getUserPoint(userId);

        // 예상 값 계산
        long expectedPoints = initialPoints + (threadCount * chargeAmount);

        assertNotEquals(expectedPoints, userPoint.point());

        log.info("테스트 실패 예상: 예상 포인트: " + expectedPoints + ", 실제 포인트: " + userPoint.point());
}
``
