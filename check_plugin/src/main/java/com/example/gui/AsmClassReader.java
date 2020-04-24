package com.example.gui;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;

import java.io.IOException;

public class AsmClassReader extends ClassReader {
    public AsmClassReader(byte[] classFile) {
        super(classFile);
    }

    public AsmClassReader(String className) throws IOException {
        super(className);
    }


    @Override
    protected Label readLabel(final int bytecodeOffset, final Label[] labels) {
        return super.readLabel(bytecodeOffset, labels);
    }

}
