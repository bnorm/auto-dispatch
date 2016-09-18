package com.bnorm.auto.dispatch.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.bnorm.auto.dispatch.AutoDispatch;

//@AutoService(Processor.class)
public class AutoDispatchProcessor extends AbstractProcessor {

    private static final String CONSTRUCTOR_NAME = "<init>";

    private static final Set<String> SUPPORTED = Collections.singleton(AutoDispatch.class.getName());

    private Messager messager;
    private Elements elements;
    private Types types;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<MethodDescriptor> methodDescriptors = new LinkedHashSet<>();
        final Set<ConstructorDescriptor> constructorDescriptor = new LinkedHashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoDispatch.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;
            if (executableElement.getSimpleName().contentEquals(CONSTRUCTOR_NAME)) {
                methodDescriptors.add(describeMethod(executableElement));
            } else {
                constructorDescriptor.add(describeConstructor(executableElement));
            }
        }

        Set<MethodClassDescriptor> methodClassDescriptors = reduce(methodDescriptors);

        return false;
    }

    private static Set<MethodClassDescriptor> reduce(Set<MethodDescriptor> methodDescriptors) {
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

    private static MethodDescriptor describeMethod(ExecutableElement element) {
        MethodDescriptor.Builder builder = MethodDescriptor.builder();
        builder.method(element);
        describeElement(builder, element);
        return builder.build();
    }

    private static ConstructorDescriptor describeConstructor(ExecutableElement element) {
        ConstructorDescriptor.Builder builder = ConstructorDescriptor.builder();
        builder.constructor(element);
        describeElement(builder, element);
        return builder.build();
    }

    private static void describeElement(ExecutableDescriptorBuilder builder, ExecutableElement element) {
        AutoDispatch annotation = element.getAnnotation(AutoDispatch.class);
        assert annotation != null;

        TypeMirror multi = null;
        try {
            annotation.multi();
        } catch (MirroredTypesException e) {
            List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
            assert typeMirrors.size() == 1;
            multi = typeMirrors.get(0);
        }
        assert multi != null;

        TypeMirror dispatcher = null;
        try {
            annotation.dispatcher();
        } catch (MirroredTypesException e) {
            List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
            assert typeMirrors.size() == 1;
            dispatcher = typeMirrors.get(0);
        }
        assert dispatcher != null;

        builder.multi(multi);
        builder.dispatcher(dispatcher);
    }
}
