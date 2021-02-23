package com;

import com.nullpointer.analysis.bean.AnalysisResultBean;

import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

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
        if (result != null && result.getAnalysisResult() != null) {
            List<AnalysisResultBean> analysisResultBeans = result.getAnalysisResult();
            Logging.getLogger("NPChecker").error("\n/*****************************");
            Logging.getLogger("NPChecker").error(" *       NPCheck results     *");
            Logging.getLogger("NPChecker").error(" *****************************/\n");
            for (AnalysisResultBean bean : analysisResultBeans) {
                String classFullName = bean.clzzName.replace('/', '.');
                String[] splits = Pattern.compile("[.]").split(classFullName);
                String classSimpleName = (splits == null || splits.length <= 0) ? "" : splits[splits.length - 1];
                String logInfo = classFullName + "." + bean.methodName + "(" + classSimpleName + ".java:"
                        + bean.lineNumber + ")\n";
                Logging.getLogger("NPChecker").warn(logInfo);
            }
            Logging.getLogger("NPChecker").error("/****************End*************/\n");
        }
    }
}
