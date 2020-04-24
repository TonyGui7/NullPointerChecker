package com.nullpointer.analysis.bean;

/**
 * 空指针检测结果bean
 *
 * @author guizhihong
 */
public class AnalysisResultBean {
    public String clzzName;
    public String methodName;
    public int lineNumber;

    public AnalysisResultBean(String clzzName, String methodName, int lineNumber) {
        this.clzzName = clzzName;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }
}
