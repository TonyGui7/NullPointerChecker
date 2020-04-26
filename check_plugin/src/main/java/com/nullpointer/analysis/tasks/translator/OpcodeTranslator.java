package com.nullpointer.analysis.tasks.translator;

import com.android.annotations.NonNull;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.AnalysisResultBean;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.bean.TaskBeanContract;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * 将检测过的字节码指令输出结果翻译成java源码级别对应的信息，方便阅读
 *
 * @author guizhihong
 */
public class OpcodeTranslator implements ITaskFlowInstruction.IOpcodeTranslator<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput> {
    private ByteCodeParser.OpcodeInfo mOpcodeInfo;
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;
    private List<OpcodeInfoItem> mProcessedInputList;
    private List<AnalysisResultBean> mResult;

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput iSimpleTaskInput, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mOpcodeInfo = iSimpleTaskInput.getOpcodeInfo();
        mProcessedInputList = iSimpleTaskInput.getCheckList();
        mListener = listener;
        mResult = new ArrayList<>();
        translate();
    }

    private void translate() {
        if (mOpcodeInfo == null || mProcessedInputList == null || mProcessedInputList.isEmpty()) {
            end();
            return;
        }

        HashMap<Integer, Integer> lineNumberTable = mOpcodeInfo.getLineNumberTable();
        if (lineNumberTable == null || lineNumberTable.isEmpty()) {
            end();
            return;
        }

        TreeSet<Integer> lineNumberOffsetSet = new TreeSet<>(lineNumberTable.keySet());
        for (OpcodeInfoItem infoItem : mProcessedInputList) {
            if (lineNumberOffsetSet.floor(infoItem.offset) == null) {
                continue;
            }
            int lineNumber = lineNumberTable.get(lineNumberOffsetSet.floor(infoItem.offset));
            mResult.add(buildResultBean(mOpcodeInfo.getCurrClzzName(), mOpcodeInfo.getCurrMethodName(), lineNumber));
        }
        end();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    private SimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .analysisResult(mResult)
                .build();
    }

    private AnalysisResultBean buildResultBean(String clzzName, String methodName, int lineNumber) {
        return new AnalysisResultBean(clzzName, methodName, lineNumber);
    }
}
