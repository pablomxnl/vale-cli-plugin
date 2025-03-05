package org.ideplugins.vale_cli_plugin.exception;

public class ValeCliExecutionException extends Exception {

    public ValeCliExecutionException(Throwable cause) {
        super(cause);
    }

    public ValeCliExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
