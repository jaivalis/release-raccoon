package com.raccoon.scraper;

public class ReleaseScrapeException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    public ReleaseScrapeException(String message, Throwable cause) {
        super(message, cause);
    }

//    public ErrorCode getCode() {
//        return this.code;
//    }
}