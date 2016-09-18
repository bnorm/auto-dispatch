package com.bnorm.auto.dispatch.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
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

    public static JavaFile write(RoundEnvironment roundEnv, MethodClassDescriptor descriptor) {
        // start building the class
        TypeElement type = descriptor.type();
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("AutoDispatch_" + type.getSimpleName().toString());
        typeBuilder.addModifiers(Modifier.FINAL);

        // add private constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        constructorBuilder.addModifiers(Modifier.PRIVATE);
        typeBuilder.addMethod(constructorBuilder.build());

        // add delegate methods
        for (MethodDescriptor methodDescriptor : descriptor.methods()) {
            ExecutableElement method = methodDescriptor.method();

            // todo(bnorm) move this to the description building process
            // find the dispatch method
            TypeElement dispatcherAnnotation = (TypeElement) methodDescriptor.dispatcher();
            Set<? extends Element> dispatchElements = roundEnv.getElementsAnnotatedWith(dispatcherAnnotation);
            ExecutableElement dispatchMethod = (ExecutableElement) dispatchElements.iterator().next();
            TypeMirror dispatchType = dispatchMethod.getReturnType();


            // find the multi methods
            TypeElement multiAnnotation = (TypeElement) methodDescriptor.multi();
            // todo(bnorm) make sure annotation has the right properties - targets only method or constructors

            Set<? extends Element> multiElements = roundEnv.getElementsAnnotatedWith(multiAnnotation);

            Map<ExecutableElement, AnnotationValue> multiElementValues = new LinkedHashMap<>();
            for (Element multiElement : multiElements) {
                ExecutableElement multiMethod = (ExecutableElement) multiElement;
                AnnotationMirror annotation = null;
                for (AnnotationMirror annotationMirror : multiMethod.getAnnotationMirrors()) {
                    if (annotationMirror.getAnnotationType().asElement().equals(multiAnnotation)) {
                        assert annotation == null : "Annotation already assigned " + annotation;
                        annotation = annotationMirror;
                    }
                }
                assert annotation != null : "Element was retrieved using annotation!";

                Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> values = annotation.getElementValues()
                                                                                                                    .entrySet();
                if (values.size() != 1) {
                    throw new IllegalArgumentException("Multi-method annotations must only have the value() method");
                }
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> only = values.iterator().next();
                if (!only.getKey().getSimpleName().contentEquals("value")) {
                    throw new IllegalArgumentException("Multi-method annotations must only have the value method");
                }

                multiElementValues.put(multiMethod, only.getValue());
            }


            // start building the delegate method
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
            methodBuilder.addModifiers(Modifier.STATIC);

            // add any generics
            for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
                TypeVariable var = (TypeVariable) typeParameterElement.asType();
                methodBuilder.addTypeVariable(TypeVariableName.get(var));
            }

            // add self parameter to the method (if required)
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

            // build the body of the method
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            codeBlockBuilder.addStatement("$T value = self.$L($L)", dispatchType, dispatchMethod, paramStr);
            boolean first = true;
            if (!dispatchType.getKind().isPrimitive()) {
                // todo(bnorm) best way to tell if an enum?
                codeBlockBuilder.beginControlFlow("if (value == null)");
                codeBlockBuilder.addStatement("throw new $T($S)", NullPointerException.class, "dispatch value == null");
                first = false;
            }
            for (Map.Entry<ExecutableElement, AnnotationValue> entry : multiElementValues.entrySet()) {
                String prefix = first ? "else " : "";
                codeBlockBuilder.nextControlFlow(prefix + "if (value == $L)", dispatchType, entry.getValue());
                codeBlockBuilder.addStatement("return self.$L($L)", entry.getKey().getSimpleName(), paramStr);
            }
            codeBlockBuilder.nextControlFlow("else");
            codeBlockBuilder.addStatement("throw new $T($S + value)",
                                          UnsupportedOperationException.class,
                                          "no method fors value == ");
            codeBlockBuilder.endControlFlow();
        }

        PackageElement packageElement = (PackageElement) type.getEnclosingElement();
        return JavaFile.builder(packageElement.getQualifiedName().toString(), typeBuilder.build()).build();
    }
}
