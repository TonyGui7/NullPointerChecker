package com.nullpointer.analysis.tasks.analyser;

import com.ITaskFlowInstruction;
import com.TaskBeanContract;
import com.nullpointer.analysis.tasks.analyser.atomicAnalyser.CastOpcodeAnalyser;
import com.nullpointer.analysis.tasks.analyser.atomicAnalyser.FieldOpcodeAnalyser;
import com.nullpointer.analysis.tasks.analyser.atomicAnalyser.InvokeOpcodeAnalyser;
import com.nullpointer.analysis.tasks.analyser.atomicAnalyser.VariableOpcodeAnalyser;

import static com.ITaskFlowInstruction.IOpcodeAnalyser.CAST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

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
