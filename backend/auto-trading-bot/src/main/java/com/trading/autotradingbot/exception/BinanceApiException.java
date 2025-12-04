package com.trading.autotradingbot.exception;

public class BinanceApiException extends RuntimeException {
    public BinanceApiException(String message) {
        super(message);
    }
    public BinanceApiException(String message, Throwable cause) { super(message, cause); }
}
