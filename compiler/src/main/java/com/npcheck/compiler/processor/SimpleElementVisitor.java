package com.npcheck.compiler.processor;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class SimpleElementVisitor implements ElementVisitor<Object, ElementVisitorListener> {
    @Override
    public Object visit(Element element, ElementVisitorListener o) {
        String name = element.getSimpleName().toString();
        return o;
    }

    @Override
    public Object visit(Element element) {
        return null;
    }

    @Override
    public Object visitPackage(PackageElement packageElement, ElementVisitorListener o) {
        String na1 = packageElement.getQualifiedName().toString();
        String na2 = packageElement.getSimpleName().toString();
        return o;
    }

    @Override
    public Object visitType(TypeElement typeElement, ElementVisitorListener o) {
        String na1 = typeElement.getQualifiedName().toString();
        String na2 = typeElement.getSimpleName().toString();
        TypeMirror mirror = typeElement.getSuperclass();
        List<? extends TypeParameterElement> elements = typeElement.getTypeParameters();
        return o;
    }

    @Override
    public Object visitVariable(VariableElement variableElement, ElementVisitorListener o) {
        String varName = variableElement.getSimpleName().toString();
        String typeName = variableElement.asType().toString();
        return o;
    }

    @Override
    public Object visitExecutable(ExecutableElement executableElement, ElementVisitorListener o) {
        List<? extends TypeParameterElement> parameterElements = executableElement.getTypeParameters();
        List<? extends VariableElement> pa = executableElement.getParameters();
        String simpleName = executableElement.getSimpleName().toString();
        TypeMirror mirror = executableElement.getReturnType();
        return o;
    }

    @Override
    public Object visitTypeParameter(TypeParameterElement typeParameterElement, ElementVisitorListener o) {
        Element element = typeParameterElement.getGenericElement();
        List<? extends TypeMirror> typeMirrors = typeParameterElement.getBounds();
        return o;
    }

    @Override
    public Object visitUnknown(Element element, ElementVisitorListener o) {
        return o;
    }
}
