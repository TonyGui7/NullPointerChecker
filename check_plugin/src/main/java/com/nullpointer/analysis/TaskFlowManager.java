package com.nullpointer.analysis;

import com.android.annotations.NonNull;
import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.tasks.analyser.OpcodeAnalyser;
import com.nullpointer.analysis.bean.input.SimpleTaskInput;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;
import com.nullpointer.analysis.bean.TaskBeanContract;
import com.nullpointer.analysis.tasks.filter.NonNullOpcodeFilter;
import com.nullpointer.analysis.tasks.translator.OpcodeTranslator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 任务流管理
 *
 * @author guizhihong
 */
public class TaskFlowManager implements ITaskFlowInstruction, TaskContract.AbstractTask.Listener<TaskBeanContract.ISimpleTaskOutput> {

    private ByteCodeParser.OpcodeInfo mOpcodeInfo;
    private Queue<TaskContract.SimpleTask<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput>> mTaskQueue = new LinkedList<>();
    private List<OpcodeInfoItem> mResult = new ArrayList<>();
    private Callback mCallback;

    private SimpleTaskInput mInput;
    private TaskBeanContract.ISimpleTaskOutput mOutput;


    @Override
    public void assembleTasks() {
        assemble();
    }

    @Override
    public void begin(ByteCodeParser.OpcodeInfo opcodeInfo) {
        mOutput = buildOutput();
        mOpcodeInfo = opcodeInfo;
        getInput().setCheckList(mResult);
        getInput().setOpcodeInfo(opcodeInfo);
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
                .checkList(mResult)
                .build();
    }

    @Override
    public void notifyEnd(@NonNull TaskBeanContract.ISimpleTaskOutput output) {
        getInput().setCheckList(output.getCheckList());
        mOutput = output;
        scheduleNextSimpleTask();
    }
}
