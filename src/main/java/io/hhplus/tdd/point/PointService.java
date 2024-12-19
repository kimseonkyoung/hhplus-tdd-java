package io.hhplus.tdd.point;

import io.hhplus.tdd.PointValidator;
import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 의존성 주입을 위한 생성자 정의
    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // 포인트 조회 메서드
    public UserPoint getUserPoint(long userId) {
        validateUserId(userId);
        return verifyUserExits(userId);
    }

    //포인트 충전 메서드
    public UserPoint charge(long userId, long amount) {
        //아이디 검증
        validateUserId(userId);
        //포인트 검증 -> 현재 포인트 조회
        long currentPoints = verifyUserExits(userId).point();
        //현재 포인트와 충전 포인트를 이용해 검증
        long chargePoints = PointValidator.chargeValidate(amount, currentPoints);
        //포인트 충전
        UserPoint pointChargeInfo = userPointTable.insertOrUpdate(userId, chargePoints);
        //포인트를 충전한 유저 히스토리 등록
        pointHistoryTable.insert(pointChargeInfo.id(), pointChargeInfo.point(), TransactionType.CHARGE, pointChargeInfo.updateMillis());
        return pointChargeInfo;
    }

    //포인트 사용 메서드
    public UserPoint use(long userId, long amount) {
        //아이디 검증
        validateUserId(userId);
        //포인트 검증 -> 현재 포인트 조회
        long currentPoints = verifyUserExits(userId).point();
        //현재 포인트와 사용 포인트를 이용해 검증
        long usePoints = PointValidator.useValidate(amount, currentPoints);
        //포인트 사용
        UserPoint pointUseInfo = userPointTable.insertOrUpdate(userId, usePoints);
        //포인트를 사용한 유저 히스토리 등록
        pointHistoryTable.insert(pointUseInfo.id(), pointUseInfo.point(), TransactionType.USE, pointUseInfo.updateMillis());
        return pointUseInfo;
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
