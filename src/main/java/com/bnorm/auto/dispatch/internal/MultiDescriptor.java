package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MultiDescriptor {

  public static Builder builder() {
    return new AutoValue_MultiDescriptor.Builder();
  }

  public abstract AnnotationValue value();

  public abstract ExecutableElement executable();

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder value(AnnotationValue value);

    public abstract Builder executable(ExecutableElement executable);

    public abstract MultiDescriptor build();
  }
}
