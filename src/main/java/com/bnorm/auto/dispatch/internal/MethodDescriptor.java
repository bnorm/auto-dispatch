package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class MethodDescriptor {

  public static MethodDescriptor.Builder builder() {
    return new AutoValue_MethodDescriptor.Builder();
  }

  public abstract ExecutableElement method();

  public abstract TypeMirror multi();

  public abstract ImmutableSet<MultiDescriptor> multiMethods();

  public abstract TypeMirror dispatcher();

  public abstract DispatcherDescriptor dispatcherDescriptor();

  @AutoValue.Builder
  public abstract static class Builder implements DispatchDescriptorBuilder {

    public abstract Builder method(ExecutableElement method);

    @Override
    public abstract Builder multi(TypeMirror multi);

    abstract ImmutableSet.Builder<MultiDescriptor> multiMethodsBuilder();

    @Override
    public Builder addMultiDescriptor(MultiDescriptor multiDescriptor) {
      multiMethodsBuilder().add(multiDescriptor);
      return this;
    }

    @Override
    public abstract Builder dispatcher(TypeMirror dispatcher);

    @Override
    public abstract Builder dispatcherDescriptor(DispatcherDescriptor dispatcherDescriptor);

    public abstract MethodDescriptor build();
  }
}
