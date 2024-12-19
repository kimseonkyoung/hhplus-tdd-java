package io.hhplus.tdd.point;

import io.hhplus.tdd.PointValidator;
import io.hhplus.tdd.UserNotFoundException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
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

    //유저 포인트 충전 메서드
    public UserPoint charge(long userId, long amount) {
        //아이디 검증
        validateUserId(userId);
        //해당 아이디의 유저 존재 유무 검증 -> 검증 후 존재하는 사용자의 포인트 조회
        long currentPoints = verifyUserExits(userId).point();
        //현재 포인트와 충전 포인트를 사용해 포인트 정책 검증
        long chargePoints = PointValidator.chargeValidate(amount, currentPoints);
        //포인트 충전
        UserPoint pointChargeInfo = userPointTable.insertOrUpdate(userId, chargePoints);
        //포인트를 충전한 유저 히스토리 등록
        pointHistoryTable.insert(pointChargeInfo.id(), amount, TransactionType.CHARGE, pointChargeInfo.updateMillis());
        return pointChargeInfo;
    }

    //유저 포인트 사용
    public UserPoint use(long userId, long amount) {
        //아이디 검증
        validateUserId(userId);
        //해당 아이디의 유저 존재 유무 검증 -> 검증 후 존재하는 사용자의 포인트 조회
        long currentPoints = verifyUserExits(userId).point();
        //현재 포인트와 충전 포인트를 사용해 포인트 정책 검증
        long usePoints = PointValidator.useValidate(amount, currentPoints);
        //포인트 사용
        UserPoint pointUseInfo = userPointTable.insertOrUpdate(userId, usePoints);
        //포인트를 사용한 유저 히스토리 등록
        pointHistoryTable.insert(pointUseInfo.id(), amount, TransactionType.USE, pointUseInfo.updateMillis());
        return pointUseInfo;
    }

    //유저 포인트 히스토리 조회
    public List getUserPointHistory(long userId) {
        //아이디 검증
        validateUserId(userId);
        //해당 아이디의 유저 존재 유무 검증
        verifyUserExits(userId);
        List<PointHistory> userHistoryList = pointHistoryTable.selectAllByUserId(userId);
        if(userHistoryList.isEmpty()){
            log.info("유저 ID " + userId + "의 포인트 히스토리가 없습니다.");
        }
        return userHistoryList;
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
