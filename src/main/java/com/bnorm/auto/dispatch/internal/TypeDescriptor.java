package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class TypeDescriptor {

    public static TypeDescriptor.Builder builder() {
        return new AutoValue_TypeDescriptor.Builder();
    }

    public abstract TypeElement type();

    public abstract TypeMirror multi();

    public abstract ImmutableSet<MultiDescriptor> multiMethods();

    public abstract TypeMirror dispatcher();

    public abstract DispatcherDescriptor dispatcherDescriptor();

    @AutoValue.Builder
    public abstract static class Builder implements DispatchDescriptorBuilder {

        public abstract Builder type(TypeElement method);

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

        public abstract TypeDescriptor build();
    }
}
