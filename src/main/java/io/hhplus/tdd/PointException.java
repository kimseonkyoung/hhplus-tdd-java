package io.hhplus.tdd;

public class PointException{

    public static class InsufficientChargePointsException extends RuntimeException {
        public InsufficientChargePointsException(String message) {
            super(message);
        }
    }

    public static class MaximumChargePointsExceededException extends RuntimeException {
        public MaximumChargePointsExceededException(String message) {
            super(message);
        }
    }

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }
    public static class InsufficientUsePointsException extends RuntimeException {
        public InsufficientUsePointsException(String message) {
            super(message);
        }
    }

}
