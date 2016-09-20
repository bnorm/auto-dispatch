package com.bnorm.auto.dispatch.internal;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.bnorm.auto.dispatch.AutoDispatch;

public final class Descriptors {

  private Descriptors() {
  }

  static TypeDescriptor describeType(TypeElement element, RoundEnvironment roundEnv, Types types) {
    TypeDescriptor.Builder builder = TypeDescriptor.builder();
    builder.type(element);
    describeElement(builder, element, roundEnv, types);
    return builder.build();
  }

  static MethodDescriptor describeMethod(ExecutableElement element, RoundEnvironment roundEnv, Types types) {
    MethodDescriptor.Builder builder = MethodDescriptor.builder();
    builder.method(element);
    describeElement(builder, element, roundEnv, types);
    return builder.build();
  }

  static void describeElement(DispatchDescriptorBuilder builder,
                              Element element,
                              RoundEnvironment roundEnv,
                              Types types) {
    AutoDispatch annotation = element.getAnnotation(AutoDispatch.class);
    assert annotation != null;

    // find the multi annotation
    TypeMirror multi = null;
    try {
      annotation.multi();
    } catch (MirroredTypesException e) {
      List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
      assert typeMirrors.size() == 1;
      multi = typeMirrors.get(0);
    }
    assert multi != null;
    // todo(bnorm) make sure annotation has the right properties - targets only executable or constructors
    builder.multi(multi);

    // find the dispatcher annotation
    TypeMirror dispatcher = null;
    try {
      annotation.dispatcher();
    } catch (MirroredTypesException e) {
      List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
      assert typeMirrors.size() == 1;
      dispatcher = typeMirrors.get(0);
    }
    assert dispatcher != null;
    builder.dispatcher(dispatcher);

    // find all methods/constructors annotated with the multi annotation
    for (Element multiElement : roundEnv.getElementsAnnotatedWith((TypeElement) types.asElement(multi))) {
      // todo(bnorm) make sure this is the only type possible
      ExecutableElement multiMethod = (ExecutableElement) multiElement;
      AnnotationMirror multiAnnotation = null;
      for (AnnotationMirror annotationMirror : multiMethod.getAnnotationMirrors()) {
        if (annotationMirror.getAnnotationType().equals(multi)) {
          assert multiAnnotation == null : "Annotation already assigned " + multiAnnotation;
          multiAnnotation = annotationMirror;
        }
      }
      assert multiAnnotation != null : "Element was retrieved using annotation!";

      Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> values = multiAnnotation.getElementValues()
                                                                                                               .entrySet();
      if (values.size() != 1) {
        throw new IllegalArgumentException("Multi-executable annotations must only have the value() executable");
      }
      Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> only = values.iterator().next();
      if (!only.getKey().getSimpleName().contentEquals("value")) {
        throw new IllegalArgumentException("Multi-executable annotations must only have the value executable");
      }

      builder.addMultiDescriptor(MultiDescriptor.builder().value(only.getValue()).executable(multiMethod).build());
    }
    // todo(bnorm) make sure all the multi descriptors have the same signature

    // find the correct dispatch method
    DispatcherDescriptor dispatcherDescriptor = null;
    for (Element dispatcherElement : roundEnv.getElementsAnnotatedWith((TypeElement) types.asElement(dispatcher))) {
      ExecutableElement dispatcherMethod = (ExecutableElement) dispatcherElement;
      if (dispatcherDescriptor != null) {
        // todo(bnorm) allow for multi dispatch methods - look for the one with the right signature
        throw new AssertionError("Why are there multi dispatch methods?");
      }
      dispatcherDescriptor = DispatcherDescriptor.builder().method(dispatcherMethod).build();
    }
    if (dispatcherDescriptor == null) {
      throw new AssertionError("There are no dispatch methods?");
    }
    // todo(bnorm) make sure the dispatch descriptor has the same signature

    builder.dispatcherDescriptor(dispatcherDescriptor);
  }

  static Set<MethodClassDescriptor> reduce(Set<MethodDescriptor> methodDescriptors) {
    Map<TypeElement, MethodClassDescriptor.Builder> methodClassDescriptorBuilders = new LinkedHashMap<>();
    for (MethodDescriptor methodDescriptor : methodDescriptors) {
      TypeElement enclosingElement = (TypeElement) methodDescriptor.method().getEnclosingElement();

      MethodClassDescriptor.Builder builder = methodClassDescriptorBuilders.get(enclosingElement);
      if (builder == null) {
        builder = MethodClassDescriptor.builder();
        builder.type(enclosingElement);
        methodClassDescriptorBuilders.put(enclosingElement, builder);
      }

      builder.method(methodDescriptor);
    }

    Set<MethodClassDescriptor> methodClassDescriptors = new LinkedHashSet<>();
    for (MethodClassDescriptor.Builder builder : methodClassDescriptorBuilders.values()) {
      methodClassDescriptors.add(builder.build());
    }
    return methodClassDescriptors;
  }
}
