package com.npcheck.compiler.processor;

import com.google.auto.service.AutoService;
import com.npcheck.compiler_interface.Consts;
import com.npcheck.compiler_interface.NPClassCheck;
import com.npcheck.compiler_interface.NotNullCheck;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class NPCheckProcessor extends AbstractProcessor {

    private Elements mElements;
    private Types mTypes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElements = processingEnvironment.getElementUtils();
        mTypes = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(NPClassCheck.class);
        List<String> checkClassNames = new ArrayList<>();
        for (Element element : elements) {
            checkClassNames.add(element.asType().toString());
        }

        generateJavaFile(checkClassNames);
        // TODO: 2020-05-15  @guizhihong 针对注解NotNullCheck的信息处理
//        Set<? extends Element> notNullElement = roundEnvironment.getElementsAnnotatedWith(NotNullCheck.class);
//        for (Element element : notNullElement) {
//
//        }
        return true;
    }

    @Override
    public java.util.Set<java.lang.String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(NPClassCheck.class.getCanonicalName());
        types.add(NotNullCheck.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    private void generateJavaFile(List<String> checkClasses) {
        if (checkClasses == null || checkClasses.isEmpty()) {
            return;
        }

        MethodSpec.Builder specBuilder = MethodSpec.methodBuilder(Consts.GN_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
    }
}
