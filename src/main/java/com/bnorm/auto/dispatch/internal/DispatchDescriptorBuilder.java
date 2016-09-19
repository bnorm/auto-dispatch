package com.bnorm.auto.dispatch.internal;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public interface DispatchDescriptorBuilder {

    DispatchDescriptorBuilder multi(TypeMirror multi);

    DispatchDescriptorBuilder addMultiDescriptor(MultiDescriptor multiDescriptor);

    DispatchDescriptorBuilder dispatcher(TypeMirror dispatcher);

    DispatchDescriptorBuilder dispatcherDescriptor(DispatcherDescriptor dispatcherDescriptor);
}
