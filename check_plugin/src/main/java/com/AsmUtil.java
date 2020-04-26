package com;

import com.android.SdkConstants;
import com.android.ddmlib.Log;
import com.bytecode.parser.ASMConfig;
import com.bytecode.parser.AsmClassReader;
import com.bytecode.parser.AsmClassVisitor;
import com.bytecode.parser.AsmClassWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

public class AsmUtil {
    private AsmUtil() {
    }

    private static AsmUtil mInstance;

    public static synchronized AsmUtil getInstance() {
        if (mInstance == null) {
            mInstance = new AsmUtil();
        }
        return mInstance;
    }

    public void handleClassFile(File src, File dest) {
        if (src == null || !src.exists()) {
            return;
        }

        Collection<File> files = FileUtils.listFiles(src, new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
        Collection<File> targetFiles = FileUtils.listFiles(src, new NameFileFilter(ASMConfig.TARGET_CLASS), TrueFileFilter.INSTANCE);

        if (targetFiles != null && !targetFiles.isEmpty()) {
            for (File file : targetFiles) {
                processTargetClassFile(file);
                System.out.println(" target file name: " + file.getAbsolutePath());
            }
        }
    }

    private void processTargetClassFile(File targetFile) {
        if (targetFile == null || !targetFile.exists()) {
            return;
        }

        String targetFileName = targetFile.getName();
        int endIndex = targetFileName.length() - SdkConstants.DOT_CLASS.length();
        String targetClassName = targetFileName.substring(0, endIndex);
        if (!ASMConfig.CLASS_NAME.equals(targetClassName)) {
            return;
        }

        try {
            AsmClassReader classReader = new AsmClassReader(FileUtils.readFileToByteArray(targetFile));
            AsmClassWriter classWriter = new AsmClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            AsmClassVisitor classVisitor = new AsmClassVisitor(classWriter, new OpcodeParserReceiver());
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(classWriter.toByteArray());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.d("AsmUtil", "exception");
            e.printStackTrace();
        }
    }
}
