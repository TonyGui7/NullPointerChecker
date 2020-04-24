package com.nullpointer.analysis.tasks.filter;

import com.android.annotations.NonNull;
import com.nullpointer.analysis.tools.ClassUtil;
import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.nullpointer.analysis.bean.TaskBeanContract;
import com.nullpointer.analysis.tools.AnalyserUtil;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.CONST_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

/**
 * 过滤出需要进行空指针检测的字节码指令集
 *
 * @author guizhihong
 */
public class NonNullOpcodeFilter implements ITaskFlowInstruction.IOpcodeFilter<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput> {
    private ByteCodeParser.OpcodeInfo mOpcodeInfo;
    private List<OpcodeInfoItem> mFilteredResult;
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;

    @Override
    public void filter(ByteCodeParser.OpcodeInfo opcodeInfo) {
        filterInvokeOpcodeInfo();
    }

    private void filterInvokeOpcodeInfo() {
        if (mOpcodeInfo == null || mOpcodeInfo.getInvokeOpcodeInfoHashMap() == null) {
            end();
            return;
        }

        HashMap<Integer, ByteCodeParser.InvokeOpcodeInfo> infoHashMap = mOpcodeInfo.getInvokeOpcodeInfoHashMap();


        if (mFilteredResult == null) {
            mFilteredResult = new ArrayList<>();
        }
        for (Integer offset : infoHashMap.keySet()) {
            ByteCodeParser.InvokeOpcodeInfo invokeOpcodeInfo = infoHashMap.get(offset);
            if (invokeOpcodeInfo.opcode == Opcodes.INVOKESTATIC || invokeOpcodeInfo.opcode == Opcodes.INVOKEDYNAMIC) {
                continue;
            }
            OpcodeInfoItem targetOffset = getTargetObjectOffset(offset, invokeOpcodeInfo);

            if (isSelfOpcode(targetOffset.offset)) {
                continue;
            }
            mFilteredResult.add(targetOffset);
        }
        end();
    }

    private boolean isSelfOpcode(int offset) {
        if (mOpcodeInfo == null || mOpcodeInfo.getVarOpcodeInfoHashMap() == null) {
            return false;
        }

        HashMap<Integer, ByteCodeParser.VarOpcodeInfo> varOpcodeInfoHashMap = mOpcodeInfo.getVarOpcodeInfoHashMap();
        if (!varOpcodeInfoHashMap.keySet().contains(offset)) {
            return false;
        }

        ByteCodeParser.VarOpcodeInfo varOpcodeInfo = varOpcodeInfoHashMap.get(offset);
        return varOpcodeInfo.opcode == Opcodes.ALOAD && varOpcodeInfo.var == 0;
    }

    private OpcodeInfoItem getTargetObjectOffset(int byteCodeOffset, ByteCodeParser.InvokeOpcodeInfo invokeOpcodeInfo) {
        if (mOpcodeInfo == null || mOpcodeInfo.getGeneralOpcodeInfo() == null || invokeOpcodeInfo == null) {
            return null;
        }

        HashMap<Integer, ByteCodeParser.InvokeOpcodeInfo> infoHashMap = mOpcodeInfo.getInvokeOpcodeInfoHashMap();

        if (infoHashMap == null) {
            return null;
        }
        List<String> params = ClassUtil.parseArguments(invokeOpcodeInfo.descriptor);
        OpcodeInfoItem result;
        OpcodeInfoItem preOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, byteCodeOffset);
        if (params == null || params.size() == 0) {//无参方法
            result = preOpcode;
        } else {
            int dealCount = params.size();
            OpcodeInfoItem currOpcode = preOpcode;
            while (dealCount > 0) {
                switch (AnalyserUtil.classifyOpcode(currOpcode.opcode)) {
                    case FIELD_TYPE:
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, currOpcode.offset).offset);
                        dealCount--;
                        break;
                    case LDC_TYPE:
                    case VARIABLE_TYPE:
                    case CONST_TYPE:
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, currOpcode.offset);
                        dealCount--;
                        break;
                    case INVOKE_TYPE:
                        while (AnalyserUtil.classifyOpcode(currOpcode.opcode) == INVOKE_TYPE) {
                            currOpcode = getTargetObjectOffset(currOpcode.offset, infoHashMap.get(currOpcode.offset));
                        }
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(mOpcodeInfo, currOpcode.offset);
                        dealCount--;
                        break;
                    default:
                        break;
                }
            }
            result = currOpcode;
        }


        return result;
    }

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput input, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mListener = listener;
        mOpcodeInfo = input.getOpcodeInfo();
        mFilteredResult = new ArrayList<>();
        filter(mOpcodeInfo);
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    private @NonNull
    TaskBeanContract.ISimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .checkList(mFilteredResult)
                .build();
    }
}
