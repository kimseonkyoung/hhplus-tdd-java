package io.hhplus.tdd.point;

import io.hhplus.tdd.ChargePointValidator;
import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;

    // 의존성 주입을 위한 생성자 정의
    public PointService(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    // 포인트 조회 메서드
    public UserPoint getUserPoint(long userId) {
        validateUserId(userId);
        return verifyUserExits(userId);
    }

    //포인트 충전 메서드
    public UserPoint charge(long userId, long chargePoints) {
        validateUserId(userId);
        UserPoint userPoint = verifyUserExits(userId);
        ChargePointValidator.validate(chargePoints, userPoint.point());
        return userPointTable.insertOrUpdate(userId, chargePoints);
    }

    private void validateUserId(long userId){
        if(userId  <= 0L){
            throw new IllegalArgumentException("해당 유저 아이디가 올바르지 않습니다: " + userId);
        }
    }

    private UserPoint verifyUserExits(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint == null){
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다: " + userId);
        }
        return userPoint;
    }
}
