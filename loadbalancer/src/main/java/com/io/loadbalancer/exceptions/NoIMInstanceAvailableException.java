package com.io.loadbalancer.exceptions;

public class NoIMInstanceAvailableException extends Exception{

    public NoIMInstanceAvailableException() {
        super();
    }

    public NoIMInstanceAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoIMInstanceAvailableException(String message) {
        super(message);
    }

    public NoIMInstanceAvailableException(Throwable cause) {
        super(cause);
    }
}
