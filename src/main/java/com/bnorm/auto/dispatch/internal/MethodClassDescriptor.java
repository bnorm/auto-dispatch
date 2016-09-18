package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.TypeElement;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class MethodClassDescriptor {

    public static MethodClassDescriptor.Builder builder() {
        return new AutoValue_MethodClassDescriptor.Builder();
    }

    public abstract TypeElement type();

    public abstract ImmutableSet<MethodDescriptor> methods();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder type(TypeElement type);

        abstract ImmutableSet.Builder<MethodDescriptor> methodsBuilder();

        public Builder method(MethodDescriptor method) {
            methodsBuilder().add(method);
            return this;
        }

        public abstract MethodClassDescriptor build();
    }
}
