package com.nullpointer.analysis.tasks.analyser.atomicAnalyser;

import com.CommonOpcodeAnalysisItem;
import com.android.annotations.NonNull;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.TaskBeanContract;
import com.nullpointer.analysis.tools.AnalyserUtil;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 调用方法字节码指令判断是否判空分析器
 *
 * @author guizhihong
 */

public class BaseOpcodeAnalyser implements ITaskFlowInstruction.IOpcodeAnalyser.IAtomicOpcodeAnalyser<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput> {
    protected List<CommonOpcodeAnalysisItem> mAnalysisList;
    private Listener<TaskBeanContract.IAtomicTaskOutput> mListener;

    @Override
    public void start(@NonNull TaskBeanContract.IAtomicTaskInput iAtomicTaskInput, @NonNull Listener<TaskBeanContract.IAtomicTaskOutput> listener) {
        mAnalysisList = iAtomicTaskInput.getAnalysisList();
        mListener = listener;
        checkToStart();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    private void checkToStart() {
        if (mAnalysisList == null || mAnalysisList.isEmpty()) {
            end();
            return;
        }

        for (CommonOpcodeAnalysisItem analysisItem : mAnalysisList) {
            startAnalyse(analysisItem.getOpcodeInfo(), analysisItem.getCheckList());
        }
        end();
    }

    public void startAnalyse(ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        if (opcodeInfo == null || checkList == null || checkList.isEmpty()) {
            return;
        }

        analyseJumpOpcodeCase(opcodeInfo, checkList);
        analyseInstanceOfOpcodeCase(opcodeInfo, checkList);
    }

    private void analyseJumpOpcodeCase(ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        if (opcodeInfo == null) {
            return;
        }
        HashMap<Integer, ByteCodeParser.JumpOpcodeInfo> jumpOpcodeInfoHashMap = opcodeInfo.getJumpOpcodeInfoList();
        List<Integer> jumpOpcodeOffsetList = new ArrayList<>(jumpOpcodeInfoHashMap.keySet());
        Collections.sort(jumpOpcodeOffsetList);
        for (int index = 0; index < jumpOpcodeOffsetList.size(); index++) {
            int targetOffset = jumpOpcodeOffsetList.get(index);
            ByteCodeParser.JumpOpcodeInfo targetJumpOpcodeInfo = jumpOpcodeInfoHashMap.get(targetOffset);
            analyseJumpOpcode(targetOffset, targetJumpOpcodeInfo, checkList, opcodeInfo);
        }
    }

    private void analyseInstanceOfOpcodeCase(ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        if (opcodeInfo == null) {
            return;
        }
        HashMap<Integer, ByteCodeParser.InstanceOfOpcodeInfo> instanceOfOpcodeInfoHashMap = opcodeInfo.getInstanceOfOpcodeInfoHashMap();
        if (instanceOfOpcodeInfoHashMap == null || instanceOfOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        List<Integer> instanceOfOpcodeOffsetList = new ArrayList<>(instanceOfOpcodeInfoHashMap.keySet());
        Collections.sort(instanceOfOpcodeOffsetList);
        for (int index = 0; index < instanceOfOpcodeOffsetList.size(); index++) {
            int targetOffset = instanceOfOpcodeOffsetList.get(index);
            ByteCodeParser.InstanceOfOpcodeInfo instanceOfOpcodeInfo = instanceOfOpcodeInfoHashMap.get(targetOffset);
            analyseJumpOpcode(targetOffset, instanceOfOpcodeInfo, checkList, opcodeInfo);
        }
    }

    private void analyseJumpOpcode(int targetOffset, ByteCodeParser.BaseOpcodeInfo checkNullOpcodeInfo, List<OpcodeInfoItem> checkList, ByteCodeParser.OpcodeInfo opcodeInfo) {
        switch (checkNullOpcodeInfo.opcode) {
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                if (checkNullOpcodeInfo instanceof ByteCodeParser.JumpOpcodeInfo) {
                    analyseIfTypeOpcode(targetOffset, ((ByteCodeParser.JumpOpcodeInfo) checkNullOpcodeInfo).jumpTargetOffset, checkNullOpcodeInfo.opcode == Opcodes.IFNULL, opcodeInfo, checkList);
                }
                break;
            case Opcodes.INSTANCEOF:
                if (checkNullOpcodeInfo instanceof ByteCodeParser.InstanceOfOpcodeInfo) {
                    analyseInstanceOfTypeOpcode(targetOffset, opcodeInfo, checkList);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 当前判空跳转指令的字节码地址为targetOffset，目标跳转地址时jumpOffset，因此targetOffset与jumpOffset之间的区间
     * 是非空指针的作用域，jumpOffset之后字节码是为空指针的作用域
     * @param targetOffset
     * @param jumpOffset
     * @param isIfNull
     * @param opcodeInfo
     * @param checkList
     */
    private void analyseIfTypeOpcode(int targetOffset, int jumpOffset, boolean isIfNull, ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        OpcodeInfoItem checkNullOpcodeInfo = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, targetOffset);
        if (checkNullOpcodeInfo == null) {
            return;
        }

        OpcodeInfoItem returnOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, jumpOffset);
        boolean isReturnCode = returnOpcode != null && AnalyserUtil.classifyOpcode(returnOpcode.opcode) == ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE;
        if (!isIfNull && !isReturnCode) {
            return;
        }

        analyseSpecificTypeOpcodes(checkNullOpcodeInfo, jumpOffset, isIfNull, opcodeInfo, checkList);
    }

    private void analyseInstanceOfTypeOpcode(int targetOffset, ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        OpcodeInfoItem checkNullOpcodeInfo = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, targetOffset);
        if (checkNullOpcodeInfo == null) {
            return;
        }

        OpcodeInfoItem eqOpcodeInfo = AnalyserUtil.getAfterOpcodeInfo(opcodeInfo, targetOffset);
        if (eqOpcodeInfo.opcode != Opcodes.IFEQ && eqOpcodeInfo.opcode != Opcodes.IFNE) {
            return;
        }
        boolean isIfNull = eqOpcodeInfo.opcode == Opcodes.IFEQ;
        HashMap<Integer, ByteCodeParser.JumpOpcodeInfo> jumpOpcodeInfoHashMap = opcodeInfo.getJumpOpcodeInfoList();
        if (jumpOpcodeInfoHashMap == null || jumpOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        int jumpTargetOffset = jumpOpcodeInfoHashMap.get(eqOpcodeInfo.offset).jumpTargetOffset;

        OpcodeInfoItem returnOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, jumpTargetOffset);
        boolean isReturnCode = returnOpcode != null && AnalyserUtil.classifyOpcode(returnOpcode.opcode) == ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE;
        if (!isIfNull && !isReturnCode) {
            return;
        }

        analyseSpecificTypeOpcodes(checkNullOpcodeInfo, jumpTargetOffset, isIfNull, opcodeInfo, checkList);
    }

    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull, ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {

    }


    protected int getOpcodeIndexLessThan(int targetOffset, List<Integer> offsetList) {
        if (offsetList == null) {
            return ITaskFlowInstruction.DEFAULT_VALUE;
        }

        int index = offsetList.size() - 1;
        for (; index >= 0; index--) {
            int offset = offsetList.get(index);
            if (offset < targetOffset) {
                return index;
            }
        }

        return ITaskFlowInstruction.DEFAULT_VALUE;
    }

    protected int getOpcodeIndexGreaterThan(int targetOffset, List<Integer> offsetList) {
        if (offsetList == null) {
            return ITaskFlowInstruction.DEFAULT_VALUE;
        }

        int index = 0;
        for (; index < offsetList.size(); index++) {
            int offset = offsetList.get(index);
            if (offset >= targetOffset) {
                return index;
            }
        }

        return ITaskFlowInstruction.DEFAULT_VALUE;
    }

    private SimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .analysisList(mAnalysisList)
                .build();
    }
}
