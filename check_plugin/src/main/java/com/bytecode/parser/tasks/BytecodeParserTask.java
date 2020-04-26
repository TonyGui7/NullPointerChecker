package com.bytecode.parser.tasks;

import com.ITaskFlowInstruction;
import com.TaskBeanContract;
import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.ddmlib.Log;
import com.bytecode.parser.ASMConfig;
import com.bytecode.parser.AsmClassReader;
import com.bytecode.parser.AsmClassVisitor;
import com.bytecode.parser.AsmClassWriter;
import com.bytecode.parser.ByteCodeParser;
import com.bytecode.parser.IOpcodesParser;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BytecodeParserTask implements ITaskFlowInstruction.IBytecodeParser<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput>, IOpcodesParser.Listener {
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;
    private List<ByteCodeParser.OpcodeInfo> mOpcodeInfoList;
    private Collection<File> mClassFiles;

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput iSimpleTaskInput, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mListener = listener;
        mClassFiles = iSimpleTaskInput.getClassFiles();
        mOpcodeInfoList = new ArrayList<>();
        startParseBytecode();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    private void startParseBytecode() {
        if (mClassFiles == null || mClassFiles.isEmpty()) {
            end();
            return;
        }
        for (File file : mClassFiles) {
            processTargetClassFile(file);
            System.out.println(" target file name: " + file.getAbsolutePath());
        }
        end();
    }

    private void processTargetClassFile(File targetFile) {
        if (targetFile == null || !targetFile.exists()) {
            return;
        }

        try {
            AsmClassReader classReader = new AsmClassReader(FileUtils.readFileToByteArray(targetFile));
            AsmClassWriter classWriter = new AsmClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            AsmClassVisitor classVisitor = new AsmClassVisitor(classWriter, this);
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(classWriter.toByteArray());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.d("AsmUtil", "exception");
            e.printStackTrace();
        }
    }

    private @NonNull
    TaskBeanContract.ISimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .opcodeInfoList(mOpcodeInfoList)
                .build();
    }

    @Override
    public void onParseStart() {
        if (mOpcodeInfoList == null) {
            mOpcodeInfoList = new ArrayList<>();
        }
    }

    @Override
    public void onParseEnd(ByteCodeParser.OpcodeInfo opcodeInfo) {
        mOpcodeInfoList.add(opcodeInfo);
    }
}
