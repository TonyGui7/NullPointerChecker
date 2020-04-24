package com.example.gui;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class AsmClassVisitor extends ClassVisitor {
    private String mClassName;
    private IOpcodesParser.Listener mListener;

    public AsmClassVisitor(ClassVisitor classVisitor, IOpcodesParser.Listener listener) {
        super(Opcodes.ASM5, classVisitor);
        mListener = listener;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        mClassName = name;
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        ModuleVisitor moduleVisitor = cv.visitModule(name, access, version);
        moduleVisitor = new AsmModuleVisitor(Opcodes.ASM5);
        return moduleVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldVisitor fv = cv.visitField(access, name, descriptor, signature, value);
        fv = new AsmFieldVisitor(Opcodes.ASM5);
        return fv;

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
                exceptions);
        //Base类中有两个方法：无参构造以及process方法，这里不增强构造方法
        if (ASMConfig.TARGET_METHOD_NAME.equals(name) && ASMConfig.TARGET_METHOD_DESC1.equals(desc) && mv != null) {
            mv = new AsmMethodVisitor(mv, mClassName, name, mListener);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    private class AsmModuleVisitor extends ModuleVisitor {

        public AsmModuleVisitor(int api) {
            super(api);
        }

        @Override
        public void visitMainClass(String mainClass) {
            super.visitMainClass(mainClass);
        }

        @Override
        public void visitPackage(String packaze) {
            super.visitPackage(packaze);
        }

        @Override
        public void visitExport(String packaze, int access, String... modules) {
            super.visitExport(packaze, access, modules);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }

    private class AsmFieldVisitor extends FieldVisitor {

        public AsmFieldVisitor(int api) {
            super(api);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }
    }

    private class AsmMethodVisitor extends MethodVisitor implements Opcodes {
        private String mClzzName;
        private String mMethodName;

        private ByteCodeParser mByteCodeParser;

        private int mCurrentBycodeOffset = 0;

        public AsmMethodVisitor(MethodVisitor methodVisitor, String clzzName, String methodName, IOpcodesParser.Listener listener) {
            super(Opcodes.ASM5, methodVisitor);
            this.mClzzName = clzzName;
            this.mMethodName = methodName;
            mByteCodeParser = new ByteCodeParser(listener);
        }

        @Override
        public void visitCode() {
            if (mByteCodeParser != null) {
                mByteCodeParser.start();
            }
            mCurrentBycodeOffset = 0;
            super.visitCode();
        }

        @Override
        public void visitFrame(final int type, final int numLocal, final Object[] local, final int numStack, final Object[] stack) {
            super.visitFrame(type, numLocal, local, numStack, stack);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitInsn(int opcode) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(
                final int opcode,
                final String owner,
                final String name,
                final String descriptor,
                final boolean isInterface) {

            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mByteCodeParser.cacheInvokeOpcodeInfo(mCurrentBycodeOffset, opcode, owner, name, descriptor, isInterface);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mByteCodeParser.cacheInstanceOfOpcodeInfo(mCurrentBycodeOffset, opcode, type);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitLabel(final Label label) {
            super.visitLabel(label);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }
            super.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (mByteCodeParser != null) {
                int actualOpcode = mByteCodeParser.parseVarOpcode(opcode, var);

                mByteCodeParser.cacheGeneralByteCodeInfo(actualOpcode, mCurrentBycodeOffset);
                mByteCodeParser.cacheVarOpcodeInfo(mCurrentBycodeOffset, opcode, var);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(actualOpcode, mCurrentBycodeOffset);
            }

            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mByteCodeParser.cacheFieldOpcodeInfo(mCurrentBycodeOffset, opcode, owner, name, descriptor);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            if (mByteCodeParser != null && start != null) {
                mByteCodeParser.cacheLineNumberTableInfo(start.getOffset(), line);
            }
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitInvokeDynamicInsn(
                final String name,
                final String descriptor,
                final Handle bootstrapMethodHandle,
                final Object... bootstrapMethodArguments) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(Opcodes.INVOKEDYNAMIC, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(Opcodes.INVOKEDYNAMIC, mCurrentBycodeOffset);
            }
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mByteCodeParser.cacheJumpOpcodeInfo(mCurrentBycodeOffset, opcode, label);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }

            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLocalVariable(
                final String name,
                final String descriptor,
                final String signature,
                final Label start,
                final Label end,
                final int index) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index);

        }


        @Override
        public void visitLdcInsn(final Object value) {
            if (mByteCodeParser != null) {
                int opcode = mByteCodeParser.parseLdcOpcode(value);
                mByteCodeParser.cacheGeneralByteCodeInfo(opcode, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(opcode, mCurrentBycodeOffset);
            }
            super.visitLdcInsn(value);
        }

        @Override
        public void visitIincInsn(final int var, final int increment) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(Opcodes.IINC, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(Opcodes.IINC, mCurrentBycodeOffset);
            }
            super.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(
                final int min, final int max, final Label dflt, final Label... labels) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(Opcodes.TABLESWITCH, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(Opcodes.TABLESWITCH, mCurrentBycodeOffset);
            }
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(Opcodes.LOOKUPSWITCH, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(Opcodes.LOOKUPSWITCH, mCurrentBycodeOffset);
            }
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
            if (mByteCodeParser != null) {
                mByteCodeParser.cacheGeneralByteCodeInfo(Opcodes.MULTIANEWARRAY, mCurrentBycodeOffset);
                mCurrentBycodeOffset = mByteCodeParser.parseOpcodeOffset(Opcodes.MULTIANEWARRAY, mCurrentBycodeOffset);
            }
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitTryCatchBlock(
                final Label start, final Label end, final Label handler, final String type) {
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack, maxLocals);
            int maxSta = maxStack;
            int locals = maxLocals;
        }

        @Override
        public void visitEnd() {
            if (mByteCodeParser != null) {
                mByteCodeParser.finish(mClzzName, mMethodName);
            }
            super.visitEnd();
        }
    }
}
