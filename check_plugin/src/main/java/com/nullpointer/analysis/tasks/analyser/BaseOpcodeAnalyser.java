package com.nullpointer.analysis.tasks.analyser;

import com.android.annotations.NonNull;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.nullpointer.analysis.bean.TaskBeanContract;
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
    protected ByteCodeParser.OpcodeInfo mOpcodeInfo;
    protected List<OpcodeInfoItem> mCheckList;
    private Listener<TaskBeanContract.IAtomicTaskOutput> mListener;

    @Override
    public void start(@NonNull TaskBeanContract.IAtomicTaskInput iAtomicTaskInput, @NonNull Listener<TaskBeanContract.IAtomicTaskOutput> listener) {
        mCheckList = iAtomicTaskInput.getCheckList();
        mOpcodeInfo = iAtomicTaskInput.getOpcodeInfo();
        mListener = listener;
        startAnalyse();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    public void startAnalyse() {
        if (mOpcodeInfo == null || mCheckList == null || mCheckList.isEmpty()) {
            end();
            return;
        }

        analyseJumpOpcodeCase();
        analyseInstanceOfOpcodeCase();
        end();
    }

    private void analyseJumpOpcodeCase() {
        if (mOpcodeInfo == null) {
            return;
        }
        HashMap<Integer, ByteCodeParser.JumpOpcodeInfo> jumpOpcodeInfoHashMap = mOpcodeInfo.getJumpOpcodeInfoList();
        List<Integer> jumpOpcodeOffsetList = new ArrayList<>(jumpOpcodeInfoHashMap.keySet());
        Collections.sort(jumpOpcodeOffsetList);
        for (int index = 0; index < jumpOpcodeOffsetList.size(); index++) {
            int targetOffset = jumpOpcodeOffsetList.get(index);
            ByteCodeParser.JumpOpcodeInfo targetJumpOpcodeInfo = jumpOpcodeInfoHashMap.get(targetOffset);
            analyseJumpOpcode(targetOffset, targetJumpOpcodeInfo);
        }
    }

    private void analyseInstanceOfOpcodeCase() {
        if (mOpcodeInfo == null) {
            return;
        }
        HashMap<Integer, ByteCodeParser.InstanceOfOpcodeInfo> instanceOfOpcodeInfoHashMap = mOpcodeInfo.getInstanceOfOpcodeInfoHashMap();
        if (instanceOfOpcodeInfoHashMap == null || instanceOfOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        List<Integer> instanceOfOpcodeOffsetList = new ArrayList<>(instanceOfOpcodeInfoHashMap.keySet());
        Collections.sort(instanceOfOpcodeOffsetList);
        for (int index = 0; index < instanceOfOpcodeOffsetList.size(); index++) {
            int targetOffset = instanceOfOpcodeOffsetList.get(index);
            ByteCodeParser.InstanceOfOpcodeInfo instanceOfOpcodeInfo = instanceOfOpcodeInfoHashMap.get(targetOffset);
            analyseJumpOpcode(targetOffset, instanceOfOpcodeInfo);
        }
    }

    private void analyseJumpOpcode(int targetOffset, ByteCodeParser.BaseOpcodeInfo checkNullOpcodeInfo) {
        switch (checkNullOpcodeInfo.opcode) {
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                if (checkNullOpcodeInfo instanceof ByteCodeParser.JumpOpcodeInfo) {
                    analyseIfTypeOpcode(targetOffset, ((ByteCodeParser.JumpOpcodeInfo) checkNullOpcodeInfo).jumpTargetOffset, checkNullOpcodeInfo.opcode == Opcodes.IFNULL);
                }
                break;
            case Opcodes.INSTANCEOF:
                if (checkNullOpcodeInfo instanceof ByteCodeParser.InstanceOfOpcodeInfo) {
                    analyseInstanceOfTypeOpcode(targetOffset);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 当前判空跳转指令的字节码地址为targetOffset，目标跳转地址时jumpOffset，因此targetOffset与jumpOffset之间的区间
     * 是非空指针的作用域，jumpOffset之后字节码是为空指针的作用域
     *
     * @param targetOffset
     * @param jumpOffset
     * @param isIfNull
     */
    private void analyseIfTypeOpcode(int targetOffset, int jumpOffset, boolean isIfNull) {
        OpcodeInfoItem checkNullOpcodeInfo = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, targetOffset);
        if (checkNullOpcodeInfo == null) {
            return;
        }

        OpcodeInfoItem returnOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, jumpOffset);
        if (returnOpcode == null || AnalyserUtil.classifyOpcode(returnOpcode.opcode) != ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE) {
            return;
        }

        analyseSpecificTypeOpcodes(checkNullOpcodeInfo, jumpOffset, isIfNull);
    }

    private void analyseInstanceOfTypeOpcode(int targetOffset) {
        OpcodeInfoItem checkNullOpcodeInfo = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, targetOffset);
        if (checkNullOpcodeInfo == null) {
            return;
        }

        OpcodeInfoItem eqOpcodeInfo = AnalyserUtil.getAfterOpcodeInfo(mOpcodeInfo, targetOffset);
        if (eqOpcodeInfo.opcode != Opcodes.IFEQ && eqOpcodeInfo.opcode != Opcodes.IFNE) {
            return;
        }
        boolean isIfNull = eqOpcodeInfo.opcode == Opcodes.IFEQ;
        HashMap<Integer, ByteCodeParser.JumpOpcodeInfo> jumpOpcodeInfoHashMap = mOpcodeInfo.getJumpOpcodeInfoList();
        if (jumpOpcodeInfoHashMap == null || jumpOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        int jumpTargetOffset = jumpOpcodeInfoHashMap.get(eqOpcodeInfo.offset).jumpTargetOffset;

        OpcodeInfoItem returnOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, jumpTargetOffset);
        if (returnOpcode == null || AnalyserUtil.classifyOpcode(returnOpcode.opcode) != ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE) {
            return;
        }

        analyseSpecificTypeOpcodes(checkNullOpcodeInfo, jumpTargetOffset, isIfNull);
    }

    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull) {

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
                .checkList(mCheckList)
                .build();
    }
}
