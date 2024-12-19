package io.hhplus.tdd;

import io.hhplus.tdd.PointException.InsufficientBalanceException;

public class PointValidator {

    //포인트 정책
    //1)충전
    private static final long MAX_POINTS = 1000L;
    private static final long MIN_POINTS = 0L;
    //2)사용
    private static final long MIN_CHARGE_POINTS = 10L;
    private static final long MIN_USE_POINTS = 10L;

    //충전 금액 검증
    public static long chargeValidate(long amount, long currentPoints){
        if(amount <= 0L){
            throw new IllegalArgumentException("해당 충전 포인트가 올바르지 않습니다: " + amount);
        }
        if(amount < MIN_CHARGE_POINTS){
            throw new PointException.InsufficientChargePointsException("최소 충전 " + MIN_CHARGE_POINTS + "이상의 포인트를 충전해야합니다.");
        }
        if(currentPoints + amount > MAX_POINTS){
            throw new PointException.MaximumChargePointsExceededException("최대 보유 가능 " + MAX_POINTS + "포인트를 넘었습니다.");
        }
            return amount + currentPoints;
    }
    //사용 금액 검증
    public static long useValidate(long amount, long currentPoints) {
        if(amount <= 0L){
            throw new IllegalArgumentException("해당 사용 포인트가 올바르지 않습니다: " + amount);
        }
        if(amount < MIN_USE_POINTS){
            throw new PointException.InsufficientUsePointsException("최소 " + MIN_USE_POINTS + "이상의 포인트를 사용해야합니다.");
        }
        if(currentPoints - amount < MIN_POINTS){
            throw new InsufficientBalanceException("잔여 포인트가 부족합니다. 현재 잔액 " + currentPoints + "P");
        }
        return currentPoints - amount;
    }
}
