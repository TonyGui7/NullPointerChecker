package com.nullpointer.analysis.tasks.analyser;

import com.android.annotations.NonNull;
import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.ITaskFlowInstruction;
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

        HashMap<Integer, ByteCodeParser.JumpOpcodeInfo> jumpOpcodeInfoHashMap = mOpcodeInfo.getJumpOpcodeInfoList();
        List<Integer> jumpOpcodeOffsetList = new ArrayList<>(jumpOpcodeInfoHashMap.keySet());
        Collections.sort(jumpOpcodeOffsetList);
        for (int index = 0; index < jumpOpcodeOffsetList.size(); index++) {
            int targetOffset = jumpOpcodeOffsetList.get(index);
            ByteCodeParser.JumpOpcodeInfo targetJumpOpcodeInfo = jumpOpcodeInfoHashMap.get(targetOffset);
            analyseJumpOpcode(targetOffset, targetJumpOpcodeInfo);
        }
        end();
    }

    //todo @guizhihong instanceof和checkCast指令的分析
    private void analyseJumpOpcode(int targetOffset, ByteCodeParser.JumpOpcodeInfo targetJumpInfo) {
        switch (targetJumpInfo.opcode) {
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                analyseNullChecker(targetOffset, targetJumpInfo.jumpTargetOffset, targetJumpInfo.opcode == Opcodes.IFNULL);
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
    private void analyseNullChecker(int targetOffset, int jumpOffset, boolean isIfNull) {
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

    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull) {

    }


    protected int getOpcodeIndexLessThan(int targetOffset, List<Integer> offsetList) {
        if (offsetList == null) {
            return -1;
        }

        int index = offsetList.size() - 1;
        for (; index >= 0; index--) {
            int offset = offsetList.get(index);
            if (offset < targetOffset) {
                return index;
            }
        }

        return -1;
    }

    protected int getOpcodeIndexGreaterThan(int targetOffset, List<Integer> offsetList) {
        if (offsetList == null) {
            return -1;
        }

        int index = 0;
        for (; index < offsetList.size(); index++) {
            int offset = offsetList.get(index);
            if (offset >= targetOffset) {
                return index;
            }
        }

        return -1;
    }

    private SimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .checkList(mCheckList)
                .build();
    }
}
