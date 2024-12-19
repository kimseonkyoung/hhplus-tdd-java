package io.hhplus.tdd.point;

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
        UserPoint userPoint = userPointTable.selectById(userId);
        verifyUserExists(userPoint, userId);
        return userPoint;
    }
    private void validateUserId(long userId){
        if(userId == 0L || userId < 0L){
            throw new IllegalArgumentException("해당 유저 아이디가 올바르지 않습니다: " + userId);
        }
    }
    private void verifyUserExists(UserPoint userPoint, long userId){
        if(userPoint == null){
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다: " + userId);
        }
    }
}
