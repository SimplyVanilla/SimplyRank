package net.simplyvanilla.simplyrank.callback;

import net.simplyvanilla.simplyrank.data.callback.IOCallback;

public class CallbackMock<T, E> implements IOCallback<T, E> {
    public static <T, E> CallbackMock<T, E> create() {
        return new CallbackMock<>();
    }

    private boolean successCalled = false;
    private boolean errorCalled = false;
    private boolean executed = false;
    private T data;
    private E error;

    @Override
    public void success(T data) {
        this.successCalled = true;
        this.data = data;
        this.executed = true;
    }

    @Override
    public void error(E error) {
        this.errorCalled = true;
        this.error = error;
        this.executed = true;
    }

    public boolean isSuccessCalled() {
        return this.successCalled;
    }

    public boolean isErrorCalled() {
        return this.errorCalled;
    }

    public T getData() {
        return this.data;
    }

    public E getError() {
        return this.error;
    }

    public boolean isExecuted() {
        return this.executed;
    }
}
