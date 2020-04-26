package com.nullpointer.analysis.tasks.analyser;

import com.CommonOpcodeAnalysisItem;
import com.android.annotations.NonNull;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.ITaskFlowInstruction;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.TaskBeanContract;
import com.TaskContract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 总字节码指令判空分析器，属于SimpleTask，内部包含多个原子任务AtomicTask
 *
 * @author guizhihong
 */
public class OpcodeAnalyser implements ITaskFlowInstruction.IOpcodeAnalyser<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput>, TaskContract.AbstractTask.Listener<TaskBeanContract.IAtomicTaskOutput> {
    private Queue<TaskContract.AtomicTask<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput>> mAnalyserTasks;
    private OpcodeAnalyserCreator mCreators;
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;
    private TaskBeanContract.ISimpleTaskInput mInput;

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput analyserInput, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mInput = analyserInput;
        mListener = listener;
        mAnalyserTasks = prepareAnalyserTask();
        startTasks();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    private void startTasks() {
        if (mAnalyserTasks == null || mAnalyserTasks.isEmpty()) {
            end();
            return;
        }

        scheduleNextTask(mInput.getAnalysisList());
    }

    private void scheduleNextTask(List<CommonOpcodeAnalysisItem> opcodeInfoItems) {
        if (mAnalyserTasks == null || mAnalyserTasks.isEmpty()) {
            end();
            return;
        }

        TaskContract.AtomicTask<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput> atomicTask = mAnalyserTasks.poll();
        mInput.setAnalysisList(opcodeInfoItems);
        atomicTask.start(mInput, this);
    }

    private Queue<TaskContract.AtomicTask<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput>> prepareAnalyserTask() {
        List<Integer> typeList = prepareAnalyserTypes();
        Queue<TaskContract.AtomicTask<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput>> taskList = new LinkedList<>();
        if (typeList == null || typeList.isEmpty()) {
            return taskList;
        }

        for (Integer type : typeList) {
            taskList.add(getAnalyser(type));
        }
        return taskList;
    }

    private List<Integer> prepareAnalyserTypes() {
        List<Integer> result = new ArrayList<>();
        result.add(INVOKE_TYPE);
        result.add(FIELD_TYPE);
        result.add(VARIABLE_TYPE);
        result.add(CAST_TYPE);
        return result;
    }

    private ITaskFlowInstruction.IOpcodeAnalyser.IAtomicOpcodeAnalyser<TaskBeanContract.IAtomicTaskInput, TaskBeanContract.IAtomicTaskOutput> getAnalyser(@ITaskFlowInstruction.IOpcodeAnalyser.OpcodeType int type) {
        if (mCreators == null) {
            mCreators = new OpcodeAnalyserCreator();
        }

        return mCreators.create(type);
    }

    private SimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .analysisList(mInput.getAnalysisList())
                .build();
    }

    @Override
    public void notifyEnd(@NonNull TaskBeanContract.IAtomicTaskOutput output) {
        scheduleNextTask(output.getAnalysisList());
    }
}
