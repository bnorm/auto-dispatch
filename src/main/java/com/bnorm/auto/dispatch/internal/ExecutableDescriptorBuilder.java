package com.bnorm.auto.dispatch.internal;

import javax.lang.model.type.TypeMirror;

public interface ExecutableDescriptorBuilder {

    ExecutableDescriptorBuilder multi(TypeMirror multi);

    ExecutableDescriptorBuilder dispatcher(TypeMirror dispatcher);
}
