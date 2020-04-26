package com;

import com.android.SdkConstants;
import com.bytecode.parser.ASMConfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.Collection;

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

        Collection<File> files = FileUtils.listFiles(src, new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
        Collection<File> targetFiles = FileUtils.listFiles(src, new NameFileFilter(ASMConfig.TARGET_CLASS), TrueFileFilter.INSTANCE);

        mManager.assembleTasks();
        mManager.begin(targetFiles);
    }

//    private void processTargetClassFile(File targetFile) {
//        if (targetFile == null || !targetFile.exists()) {
//            return;
//        }
//
//        String targetFileName = targetFile.getName();
//        int endIndex = targetFileName.length() - SdkConstants.DOT_CLASS.length();
//        String targetClassName = targetFileName.substring(0, endIndex);
//        if (!ASMConfig.CLASS_NAME.equals(targetClassName)) {
//            return;
//        }
//    }

    @Override
    public void onTaskFinished(TaskBeanContract.ISimpleTaskOutput result) {
         mOutput = result;
    }
}
