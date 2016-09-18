package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MultiMethodDescriptor {

    public abstract ExecutableElement method();

    public abstract AnnotationValue value();
}
