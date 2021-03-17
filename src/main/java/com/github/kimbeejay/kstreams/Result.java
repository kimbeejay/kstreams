package com.github.kimbeejay.kstreams;

public interface Result<T> {
  T get();
  Exception exception();
  boolean hasError();

  class Success<T> implements Result<T> {

    private final T value;

    public Success(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      return this.value;
    }

    @Override
    public Exception exception() {
      return null;
    }

    @Override
    public boolean hasError() {
      return false;
    }
  }

  class Error<T> implements Result<T> {

    private final T value;
    private final Exception error;

    public Error(Exception error, T value) {
      this.error = error;
      this.value = value;
    }

    @Override
    public T get() {
      return this.value;
    }

    @Override
    public Exception exception() {
      return this.error;
    }

    @Override
    public boolean hasError() {
      return true;
    }
  }
}
