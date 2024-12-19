package io.hhplus.tdd;

public class ChargePointValidator {

    //포인트 정책
    private static final long MAX_POINTS = 1000L;
    private static final long MIN_CHARGE_POINTS = 10L;

    public static void validate(long chargePoints, long currentPoints){
        if(currentPoints + chargePoints > MAX_POINTS){
            throw new ChargePointException.MaximumChargePointsExceededException("최대 보유 가능 " + MAX_POINTS + "포인트를 넘었습니다.");
        }
            if(chargePoints <= 0L){
            throw new IllegalArgumentException("해당 충전 포인트가 올바르지 않습니다: " + chargePoints);
        }
            if(chargePoints < MIN_CHARGE_POINTS){
            throw new ChargePointException.InsufficientChargePointsException("최소 충전 포인트 " + MIN_CHARGE_POINTS + "이상의 금액을 충전해야합니다.");
        }
    }

}
