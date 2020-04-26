package com.nullpointer.analysis.tasks.filter;

import com.CommonOpcodeAnalysisItem;
import com.android.annotations.NonNull;
import com.nullpointer.analysis.tools.ClassUtil;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.TaskBeanContract;
import com.nullpointer.analysis.tools.AnalyserUtil;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ITaskFlowInstruction.IOpcodeAnalyser.CONST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

/**
 * 过滤出需要进行空指针检测的字节码指令集
 *
 * @author guizhihong
 */
public class NonNullOpcodeFilter implements ITaskFlowInstruction.IOpcodeFilter<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput> {
    private List<ByteCodeParser.OpcodeInfo> mOpcodeInfoList;
    private List<CommonOpcodeAnalysisItem> mFilterdResults;
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;

    @Override
    public List<OpcodeInfoItem> filter(ByteCodeParser.OpcodeInfo opcodeInfo) {
        return filterInvokeOpcodeInfo(opcodeInfo);
    }

    private List<OpcodeInfoItem> filterInvokeOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo) {
        List<OpcodeInfoItem> result = new ArrayList<>();
        if (opcodeInfo == null || opcodeInfo.getInvokeOpcodeInfoHashMap() == null) {
            return result;
        }

        HashMap<Integer, ByteCodeParser.InvokeOpcodeInfo> infoHashMap = opcodeInfo.getInvokeOpcodeInfoHashMap();

        for (Integer offset : infoHashMap.keySet()) {
            ByteCodeParser.InvokeOpcodeInfo invokeOpcodeInfo = infoHashMap.get(offset);
            if (invokeOpcodeInfo.opcode == Opcodes.INVOKESTATIC || invokeOpcodeInfo.opcode == Opcodes.INVOKEDYNAMIC || invokeOpcodeInfo.opcode == Opcodes.INVOKESPECIAL) {
                continue;
            }
            OpcodeInfoItem targetOffset = getTargetObjectOffset(offset, invokeOpcodeInfo, opcodeInfo);

            //调用对象是当前class对象 或者 是直接new的对象调用，这两种情况不用判空
            if (isSelfOpcode(targetOffset.offset, opcodeInfo) || AnalyserUtil.isInvokeInit(infoHashMap.get(targetOffset.offset))) {
                continue;
            }
            result.add(targetOffset);
        }
        return result;
    }

    private boolean isSelfOpcode(int offset, ByteCodeParser.OpcodeInfo opcodeInfo) {
        if (opcodeInfo == null || opcodeInfo.getVarOpcodeInfoHashMap() == null) {
            return false;
        }

        HashMap<Integer, ByteCodeParser.VarOpcodeInfo> varOpcodeInfoHashMap = opcodeInfo.getVarOpcodeInfoHashMap();
        if (!varOpcodeInfoHashMap.keySet().contains(offset)) {
            return false;
        }

        ByteCodeParser.VarOpcodeInfo varOpcodeInfo = varOpcodeInfoHashMap.get(offset);
        return varOpcodeInfo.opcode == Opcodes.ALOAD && varOpcodeInfo.var == 0;
    }

    private OpcodeInfoItem getTargetObjectOffset(int byteCodeOffset, ByteCodeParser.InvokeOpcodeInfo invokeOpcodeInfo, ByteCodeParser.OpcodeInfo opcodeInfo) {
        if (opcodeInfo == null || opcodeInfo.getGeneralOpcodeInfo() == null || invokeOpcodeInfo == null) {
            return null;
        }

        HashMap<Integer, ByteCodeParser.InvokeOpcodeInfo> infoHashMap = opcodeInfo.getInvokeOpcodeInfoHashMap();

        if (infoHashMap == null) {
            return null;
        }
        List<String> params = ClassUtil.parseArguments(invokeOpcodeInfo.descriptor);
        OpcodeInfoItem result;
        OpcodeInfoItem preOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, byteCodeOffset);
        if (params == null || params.size() == 0) {//无参方法
            result = preOpcode;
        } else {
            int dealCount = params.size();
            OpcodeInfoItem currOpcode = preOpcode;
            while (dealCount > 0) {
                switch (AnalyserUtil.classifyOpcode(currOpcode.opcode)) {
                    case FIELD_TYPE:
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, currOpcode.offset).offset);
                        break;
                    case LDC_TYPE:
                    case VARIABLE_TYPE:
                    case CONST_TYPE:
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, currOpcode.offset);
                        break;
                    case INVOKE_TYPE:
                        if (currOpcode.opcode == Opcodes.INVOKESPECIAL) {
                            if (AnalyserUtil.isInvokeInit(infoHashMap.get(currOpcode.offset))) {
                                currOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, currOpcode.offset).offset);
                            } else {
                                currOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, currOpcode.offset);
                            }
                        } else {
                            while (AnalyserUtil.classifyOpcode(currOpcode.opcode) == INVOKE_TYPE) {
                                currOpcode = getTargetObjectOffset(currOpcode.offset, infoHashMap.get(currOpcode.offset), opcodeInfo);
                            }
                        }
                        currOpcode = AnalyserUtil.getBeforeOpcodeInfo(opcodeInfo, currOpcode.offset);
                        break;
                    default:
                        break;
                }
                dealCount--;
            }
            result = currOpcode;
        }


        return result;
    }

    private void checkToStart() {
        if (mOpcodeInfoList == null || mOpcodeInfoList.isEmpty()) {
            end();
            return;
        }

        if (mFilterdResults == null) {
            mFilterdResults = new ArrayList<>();
        }

        for (ByteCodeParser.OpcodeInfo opcodeInfo : mOpcodeInfoList) {
            if (AnalyserUtil.isStaticBlockCode(opcodeInfo)) {//静态代码块不用判断
                continue;
            }
            mFilterdResults.add(AnalyserUtil.getAnalysisItem(opcodeInfo, filter(opcodeInfo)));
        }
        end();
    }

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput input, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mListener = listener;
        mOpcodeInfoList = input.getOpcodeInfoList();
        mFilterdResults = new ArrayList<>();
        checkToStart();
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
                .analysisList(mFilterdResults)
                .build();
    }
}
