package br.com.danielsantana.weathershow.task.exception;


public class GoogleLocationTaskException extends Exception {
    public GoogleLocationTaskException(String detailMessage) {
        super(detailMessage);
    }
}
