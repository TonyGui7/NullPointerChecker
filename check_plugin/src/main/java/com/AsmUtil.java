package com;

import java.io.File;

public class AsmUtil implements ITaskFlowInstruction.Callback {
    private ITaskFlowInstruction mManager;
    private TaskBeanContract.ISimpleTaskOutput mOutput;

    private AsmUtil() {
        mManager = new TaskFlowManager();
        mManager.setCallback(this);
    }

    private static AsmUtil mInstance;

    public static synchronized AsmUtil getInstance() {
        if (mInstance == null) {
            mInstance = new AsmUtil();
        }
        return mInstance;
    }

    public void handleClassFile(File src, File dest) {
        if (src == null || !src.exists()) {
            return;
        }
        mManager.assembleTasks();
        mManager.begin(src);
    }

    @Override
    public void onTaskFinished(TaskBeanContract.ISimpleTaskOutput result) {
        mOutput = result;
    }
}
