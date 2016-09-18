package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConstructorDescriptor {

    public static ConstructorDescriptor.Builder builder() {
        return new AutoValue_ConstructorDescriptor.Builder();
    }

    public abstract ExecutableElement constructor();

    public abstract TypeMirror multi();

    public abstract TypeMirror dispatcher();

    @AutoValue.Builder
    public abstract static class Builder implements ExecutableDescriptorBuilder {

        public abstract Builder constructor(ExecutableElement method);

        @Override
        public abstract Builder multi(TypeMirror multi);

        @Override
        public abstract Builder dispatcher(TypeMirror dispatcher);

        public abstract ConstructorDescriptor build();
    }
}
