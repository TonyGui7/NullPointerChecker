package com.npcheck.compiler.processor;

import com.google.auto.service.AutoService;
import com.npcheck.compiler_interface.Consts;
import com.npcheck.compiler_interface.NPClassCheck;
import com.npcheck.compiler_interface.NotNullCheck;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElements = processingEnvironment.getElementUtils();
        mTypes = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
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

    /**
     * 生成如下格式的java文件
     *
     * <p>
     * package com.npcheck.compiler.generated;
     *
     * public class NPCheckInfoManager {
     *     public static List<String> initNPCheckInfo() {
     *        List<String> classes = new ArrayList<>();
     *        classes.add("1");
     *        classes.add("2");
     *        ...
     *       return classes;
     *    }
     * }
     * </>
     */
    private void generateJavaFile(List<String> checkClasses) {
        if (checkClasses == null || checkClasses.isEmpty()) {
            return;
        }

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(List.class, String.class);
        MethodSpec.Builder specBuilder = MethodSpec.methodBuilder(Consts.GN_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(parameterizedTypeName).addStatement(Consts.PARAMETER_VAR_INIT, parameterizedTypeName, ClassName.get(ArrayList.class));

        for (String checkClass : checkClasses) {
            specBuilder.addStatement(Consts.VAR_ADD_STRING, checkClass);
        }
        specBuilder.addStatement(Consts.RETURN_CHECK_CLASS);

        try {
            JavaFile.builder(Consts.PKG, TypeSpec.classBuilder(Consts.GN_CLASS_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(specBuilder.build()).build())
                    .build()
                    .writeTo(mFiler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
