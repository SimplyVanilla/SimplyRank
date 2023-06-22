package net.simplyvanilla.simplyrank.data;

import java.util.function.Consumer;

public class WrappedCallback {
  private WrappedCallback() {}

  public static <T, E> IOCallback<T, E> wrap(
      Consumer<T> successConsumer, Consumer<E> errorConsumer) {
    return new IOCallback<T, E>() {
      @Override
      public void success(T data) {
        successConsumer.accept(data);
      }

      @Override
      public void error(E error) {
        errorConsumer.accept(error);
      }
    };
  }
}
