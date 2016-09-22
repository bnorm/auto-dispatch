package com.bnorm.auto.dispatch.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public enum Writer {
  ; // no instances

  public static JavaFile write(MethodClassDescriptor methodClassDescriptor) {
    // start building the class
    TypeElement type = methodClassDescriptor.type();
    TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("AutoDispatch_" + type.getSimpleName().toString());
    typeBuilder.addOriginatingElement(type);
    typeBuilder.addModifiers(Modifier.FINAL);

    // add private type
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
    constructorBuilder.addModifiers(Modifier.PRIVATE);
    typeBuilder.addMethod(constructorBuilder.build());

    // add delegate methods
    for (MethodDescriptor methodDescriptor : methodClassDescriptor.methods()) {
      ExecutableElement method = methodDescriptor.method();

      // find the dispatch executable
      DispatcherDescriptor dispatcherDescriptor = methodDescriptor.dispatcherDescriptor();
      TypeMirror dispatchType = dispatcherDescriptor.method().getReturnType();

      // start building the delegate executable
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
      methodBuilder.addModifiers(Modifier.STATIC);

      // add any generics
      for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
        TypeVariable var = (TypeVariable) typeParameterElement.asType();
        methodBuilder.addTypeVariable(TypeVariableName.get(var));
      }

      // add return value
      boolean voidReturn = method.getReturnType().getKind() == TypeKind.VOID;
      if (!voidReturn) {
        methodBuilder.returns(TypeName.get(method.getReturnType()));
      }

      // add self parameter to the executable (if required)
      if (!method.getModifiers().contains(Modifier.STATIC)) {
        ParameterSpec.Builder self = ParameterSpec.builder(TypeName.get(type.asType()), "self");
        methodBuilder.addParameter(self.build());
      }

      // add parameters and build the parameter call string
      StringBuilder paramStr = new StringBuilder();
      List<? extends VariableElement> parameters = method.getParameters();
      for (int i = 0, len = parameters.size(); i < len; i++) {
        VariableElement parameter = parameters.get(i);

        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(TypeName.get(parameter.asType()),
                                                                       parameter.getSimpleName().toString());
        for (Modifier modifier : parameter.getModifiers()) {
          parameterBuilder.addModifiers(modifier);
        }
        for (AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
          parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
        }
        methodBuilder.addParameter(parameterBuilder.build());

        if (i != 0) {
          paramStr.append(", ");
        }
        paramStr.append(parameter);
      }
      methodBuilder.varargs(method.isVarArgs());

      // add thrown exceptions
      for (TypeMirror thrownType : method.getThrownTypes()) {
        methodBuilder.addException(TypeName.get(thrownType));
      }

      // build the body of the executable
      CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
      codeBlockBuilder.addStatement("$T value = self.$L($L)",
                                    dispatchType,
                                    dispatcherDescriptor.method().getSimpleName(),
                                    paramStr);
      boolean first = true;
      if (!dispatchType.getKind().isPrimitive()) {
        // todo(bnorm) best way to tell if an enum?
        codeBlockBuilder.beginControlFlow("if (value == null)");
        codeBlockBuilder.addStatement("throw new NullPointerException($S)", "dispatch value == null");
        first = false;
      }

      // Work with multiMethod dispatch values as a list
      // This allows a multiMethod to accept several dispatch values
      Function<MultiDescriptor, List<?>> toDispatchValues = descriptor ->
          descriptor.value().getValue() instanceof List<?> ? (List<?>) descriptor.value().getValue()
                                                           : Collections.singletonList(descriptor.value().getValue());

      // Dispatch to multiMethods from the most specific to the most accepting
      List<MultiDescriptor> multiMethods = new ArrayList<>(methodDescriptor.multiMethods());
      multiMethods.sort((m1, m2) -> {
        List<?> values1 = toDispatchValues.apply(m1);
        List<?> values2 = toDispatchValues.apply(m2);

        return Integer.compare(values1.size(), values2.size());
      });

      for (MultiDescriptor descriptor : multiMethods) {
        String prefix = first ? "else " : "";

        // will always have at least one value
        List<?> dispatchValues = toDispatchValues.apply(descriptor);
        if (dispatchValues.size() == 1) {
          String value =
              descriptor.value().getValue() instanceof List<?> ? ((List<?>) descriptor.value().getValue()).get(0)
                                                                                                          .toString()
                                                               : descriptor.value().toString();
          codeBlockBuilder.nextControlFlow(prefix + "if (value == $L)", value);
        } else {
          String valuesString = dispatchValues.stream().map(Object::toString).reduce((v1, v2) -> v1 + ", " + v2).get();
          codeBlockBuilder.nextControlFlow(prefix + "if ($T.asList($L).contains(value))", Arrays.class, valuesString);
        }

        if (voidReturn) {
          codeBlockBuilder.addStatement("self.$L($L)", descriptor.executable().getSimpleName(), paramStr);
          codeBlockBuilder.addStatement("return");
        } else {
          codeBlockBuilder.addStatement("return self.$L($L)", descriptor.executable().getSimpleName(), paramStr);
        }
      }
      codeBlockBuilder.nextControlFlow("else");
      codeBlockBuilder.addStatement("throw new UnsupportedOperationException($S + value)", "no method for value == ");
      codeBlockBuilder.endControlFlow();

      methodBuilder.addCode(codeBlockBuilder.build());
      typeBuilder.addMethod(methodBuilder.build());
    }

    PackageElement packageElement = (PackageElement) type.getEnclosingElement();
    return JavaFile.builder(packageElement.getQualifiedName().toString(), typeBuilder.build()).build();
  }
}
