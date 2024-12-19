package io.hhplus.tdd;

public class ChargePointException{

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
}
