package com.annotation.parser.asm;

import com.annotation.parser.IAnnotationParser;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作注解生成的class文件，并获取其中注解作用的class文件信息
 *
 * @author guizhihong
 */
public class AptClassVisitor extends ClassVisitor {

    private IAnnotationParser mParser;

    public AptClassVisitor(ClassVisitor classVisitor, IAnnotationParser parser) {
        super(Opcodes.ASM5, classVisitor);
        mParser = parser;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
                exceptions);
        if (IAnnotationParser.GN_METHOD_NAME.equals(name)) {
            return new AptMethodVisitor(mv, mParser);
        }
        return mv;
    }

    private class AptMethodVisitor extends MethodVisitor {

        private IAnnotationParser parser;
        private List<String> mCheckClasses;

        public AptMethodVisitor(MethodVisitor methodVisitor, IAnnotationParser parser) {
            super(Opcodes.ASM5, methodVisitor);
            this.parser = parser;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mCheckClasses = new ArrayList<>();
        }

        @Override
        public void visitLdcInsn(final Object value) {
            super.visitLdcInsn(value);
            if (value instanceof String) {
                mCheckClasses.add((String) value);
            }
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (parser != null) {
                parser.parseNPCheckClasses(mCheckClasses);
            }
        }
    }

}
