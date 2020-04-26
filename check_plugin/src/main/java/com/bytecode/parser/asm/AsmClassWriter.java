package com.bytecode.parser;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class AsmClassWriter extends ClassWriter {
    public AsmClassWriter(int flags) {
        super(flags);
    }
}
