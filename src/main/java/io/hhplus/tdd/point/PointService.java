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
        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint == null){
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다: " + userId);
        }
        return userPoint;
    }
}
