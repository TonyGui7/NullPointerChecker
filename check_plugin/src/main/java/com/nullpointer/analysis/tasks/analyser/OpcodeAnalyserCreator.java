package com.nullpointer.analysis.tasks.analyser;

import com.nullpointer.analysis.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.TaskBeanContract;

import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.CAST_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.nullpointer.analysis.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

/**
 * 特定类型字节码指令分析器工厂
 *
 * @author guizhihong
 */
public class OpcodeAnalyserCreator {
    public ITaskFlowInstruction.IOpcodeAnalyser.IAtomicOpcodeAnalyser<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput> create(@ITaskFlowInstruction.IOpcodeAnalyser.OpcodeType int type) {
        switch (type) {
            case INVOKE_TYPE:
                return new InvokeOpcodeAnalyser();
            case FIELD_TYPE:
                return new FieldOpcodeAnalyser();
            case CAST_TYPE:
                return new CastOpcodeAnalyser();
            case LDC_TYPE:
            case VARIABLE_TYPE:
            default:
                return new VariableOpcodeAnalyser();
        }
    }
}
