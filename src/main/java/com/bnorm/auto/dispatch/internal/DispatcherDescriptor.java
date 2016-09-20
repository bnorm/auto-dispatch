package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.ExecutableElement;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DispatcherDescriptor {

  public static Builder builder() {
    return new AutoValue_DispatcherDescriptor.Builder();
  }

  public abstract ExecutableElement method();

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder method(ExecutableElement method);

    public abstract DispatcherDescriptor build();
  }
}
