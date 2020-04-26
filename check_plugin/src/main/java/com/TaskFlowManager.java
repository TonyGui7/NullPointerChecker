package com;

import com.android.annotations.NonNull;
import com.bytecode.parser.tasks.BytecodeParserTask;
import com.nullpointer.analysis.tasks.analyser.OpcodeAnalyser;
import com.nullpointer.analysis.bean.input.SimpleTaskInput;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.nullpointer.analysis.tasks.filter.NonNullOpcodeFilter;
import com.nullpointer.analysis.tasks.translator.OpcodeTranslator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 任务流管理
 *
 * @author guizhihong
 */
public class TaskFlowManager implements ITaskFlowInstruction, TaskContract.AbstractTask.Listener<TaskBeanContract.ISimpleTaskOutput> {
    private Queue<TaskContract.SimpleTask<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput>> mTaskQueue = new LinkedList<>();
    private List<CommonOpcodeAnalysisItem> mResult = new ArrayList<>();
    private Callback mCallback;

    private SimpleTaskInput mInput;
    private TaskBeanContract.ISimpleTaskOutput mOutput;


    @Override
    public void assembleTasks() {
        assemble();
    }

    @Override
    public void begin(Collection<File> classFileList) {
        mOutput = buildOutput();
        getInput().setAnalysisList(mResult);
        getInput().setClassFiles(classFileList);
        checkToStart();
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void assemble() {
        if (mTaskQueue == null) {
            mTaskQueue = new LinkedList<>();
        }
        mTaskQueue.add(new BytecodeParserTask());
        mTaskQueue.add(new NonNullOpcodeFilter());
        mTaskQueue.add(new OpcodeAnalyser());
        mTaskQueue.add(new OpcodeTranslator());
    }

    private void checkToStart() {
        if (mTaskQueue == null || mTaskQueue.isEmpty()) {
            if (mCallback != null) {
                mCallback.onTaskFinished(mOutput);
            }
            return;
        }
        scheduleNextSimpleTask();
    }

    private void scheduleNextSimpleTask() {
        if (mTaskQueue == null || mTaskQueue.isEmpty()) {
            if (mCallback != null) {
                mCallback.onTaskFinished(mOutput);
            }
            return;
        }

        TaskContract.SimpleTask<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput> simpleTask = mTaskQueue.poll();
        simpleTask.start(getInput(), this);
    }

    private SimpleTaskInput getInput() {
        if (mInput == null) {
            mInput = new SimpleTaskInput();
        }
        return mInput;
    }

    private SimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .analysisList(mResult)
                .build();
    }

    @Override
    public void notifyEnd(@NonNull TaskBeanContract.ISimpleTaskOutput output) {
        if (output.getAnalysisList() != null) {
            getInput().setAnalysisList(output.getAnalysisList());
        }
        if (output.getOpcodeInfoList() != null) {
            getInput().setOpcodeInfoList(output.getOpcodeInfoList());
        }
        mOutput = output;
        scheduleNextSimpleTask();
    }
}
