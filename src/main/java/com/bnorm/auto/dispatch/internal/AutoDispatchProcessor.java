package com.bnorm.auto.dispatch.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.bnorm.auto.dispatch.AutoDispatch;
import com.squareup.javapoet.JavaFile;

//@AutoService(Processor.class)
public class AutoDispatchProcessor extends AbstractProcessor {

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
        final Set<TypeDescriptor> typeDescriptor = new LinkedHashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoDispatch.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                methodDescriptors.add(Descriptors.describeMethod((ExecutableElement) element, roundEnv, types));
            } else if (element.getKind() == ElementKind.CLASS) {
                typeDescriptor.add(Descriptors.describeType((TypeElement) element, roundEnv, types));
            }
        }

        Set<MethodClassDescriptor> methodClassDescriptors = Descriptors.reduce(methodDescriptors);
        for (MethodClassDescriptor methodClassDescriptor : methodClassDescriptors) {
            JavaFile javaFile = Writer.write(methodClassDescriptor);
            writeSourceFile(javaFile, methodClassDescriptor.type());
        }
        return false;
    }


    private void writeSourceFile(JavaFile javaFile, TypeElement originatingType) {
        try {
            JavaFileObject sourceFile = processingEnv.getFiler()
                                                     .createSourceFile(fqClassNameOf(originatingType), originatingType);
            java.io.Writer writer = sourceFile.openWriter();
            try {
                javaFile.writeTo(writer);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            processingEnv.getMessager()
                         .printMessage(Diagnostic.Kind.ERROR,
                                       "Could not write generated class " + javaFile.typeSpec.name + ": " + e);
        }
    }

    private String fqClassNameOf(TypeElement type) {
        String pkg = packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return pkg + dot + classNameOf(type);
    }

    private String classNameOf(TypeElement type) {
        return "AutoDispatch_" + type.getSimpleName().toString();
    }

    static String packageNameOf(TypeElement type) {
        while (true) {
            Element enclosing = type.getEnclosingElement();
            if (enclosing instanceof PackageElement) {
                return ((PackageElement) enclosing).getQualifiedName().toString();
            }
            type = (TypeElement) enclosing;
        }
    }
}
