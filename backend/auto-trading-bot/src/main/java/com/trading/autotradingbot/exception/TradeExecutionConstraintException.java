package com.trading.autotradingbot.exception;

public class TradeExecutionConstraintException extends RuntimeException {
    public TradeExecutionConstraintException(String message) {
        super(message);
    }
    public TradeExecutionConstraintException(String message, Throwable cause) { super(message, cause); }
}
