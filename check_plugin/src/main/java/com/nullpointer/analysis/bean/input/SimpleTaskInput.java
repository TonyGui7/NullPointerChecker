package com.nullpointer.analysis.bean.input;

import com.CommonOpcodeAnalysisItem;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.TaskBeanContract;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 普通任务的输入
 *
 * @author guizhihong
 */
public class SimpleTaskInput implements TaskBeanContract.ISimpleTaskInput {
    private List<ByteCodeParser.OpcodeInfo> opcodeInfoList;
    private List<CommonOpcodeAnalysisItem> analysisList;
    private Collection<File> classFiles;

    @Override
    public List<ByteCodeParser.OpcodeInfo> getOpcodeInfoList() {
        return opcodeInfoList;
    }

    @Override
    public void setOpcodeInfoList(List<ByteCodeParser.OpcodeInfo> opcodeInfoList) {
        this.opcodeInfoList = opcodeInfoList;
    }

    @Override
    public List<CommonOpcodeAnalysisItem> getAnalysisList() {
        return analysisList;
    }

    @Override
    public void setAnalysisList(List<CommonOpcodeAnalysisItem> checkList) {
        this.analysisList = checkList;
    }

    @Override
    public void setClassFiles(Collection<File> classFiles) {
        this.classFiles = classFiles;
    }

    @Override
    public Collection<File> getClassFiles() {
        return classFiles;
    }
}
