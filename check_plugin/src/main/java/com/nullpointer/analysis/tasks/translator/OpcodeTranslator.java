package com.nullpointer.analysis.tasks.translator;

import com.CommonOpcodeAnalysisItem;
import com.android.annotations.NonNull;
import com.bytecode.parser.ByteCodeParser;
import com.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.AnalysisResultBean;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.TaskBeanContract;
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
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;
    private List<AnalysisResultBean> mResult;
    private List<CommonOpcodeAnalysisItem> mAnalysedList;

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput iSimpleTaskInput, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mAnalysedList = iSimpleTaskInput.getAnalysisList();
        mListener = listener;
        mResult = new ArrayList<>();
        checkToStart();
    }

    private void checkToStart() {
        if (mAnalysedList == null || mAnalysedList.isEmpty()) {
            end();
            return;
        }

        for (CommonOpcodeAnalysisItem analysisItem : mAnalysedList) {
            translate(analysisItem.getOpcodeInfo(), analysisItem.getCheckList());
        }
        end();
    }

    private void translate(ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> analysedCheckList) {
        if (opcodeInfo == null || analysedCheckList == null || analysedCheckList.isEmpty()) {
            return;
        }

        HashMap<Integer, Integer> lineNumberTable = opcodeInfo.getLineNumberTable();
        if (lineNumberTable == null || lineNumberTable.isEmpty()) {
            return;
        }

        TreeSet<Integer> lineNumberOffsetSet = new TreeSet<>(lineNumberTable.keySet());
        for (OpcodeInfoItem infoItem : analysedCheckList) {
            if (lineNumberOffsetSet.floor(infoItem.offset) == null) {
                continue;
            }
            int lineNumber = lineNumberTable.get(lineNumberOffsetSet.floor(infoItem.offset));
            mResult.add(buildResultBean(opcodeInfo.getCurrClzzName(), opcodeInfo.getCurrMethodName(), lineNumber));
        }
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
