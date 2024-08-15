package com.memetrader.webserver;

public class Result<R, E> {
    private final R r;
    private final E e;

    private final boolean isError;

    private Result(R r, E e, boolean isError) {
        this.r = r;
        this.e = e;
        this.isError = isError;
    }

    public static <R, E> Result<R, E> ok(R r) {
        return new Result<>(r, null, false);
    }

    public static <R, E> Result<R, E> err(E e) {
        return new Result<>(null, e, false);
    }

    public boolean isOk() {
        return !isError;
    }

    public boolean isErr() {
        return isError;
    }

    public R unwrap() {
        if (isError) {
            throw new IllegalAccessError("Attempt to unwrap error result.");
        }
        return r;
    }

    public E getErr() {
        if (!isError) {
            throw new IllegalAccessError("Attempt to get error from filled result.");
        }
        return e;
    }

}
