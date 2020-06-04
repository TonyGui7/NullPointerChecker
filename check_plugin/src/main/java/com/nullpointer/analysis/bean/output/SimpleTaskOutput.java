package com.nullpointer.analysis.bean.output;

import com.CommonOpcodeAnalysisItem;
import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.AnalysisResultBean;
import com.TaskBeanContract;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 普通任务的输出
 *
 * @author guizhihong
 */

public class SimpleTaskOutput implements TaskBeanContract.ISimpleTaskOutput {
    private List<CommonOpcodeAnalysisItem> analysisList;
    private List<AnalysisResultBean> analysisResultBeanList;
    private List<ByteCodeParser.OpcodeInfo> mOpcodeInfoList;
    private Collection<File> mCheckFiles;


    @Override
    public List<CommonOpcodeAnalysisItem> getAnalysisList() {
        return analysisList;
    }

    @Override
    public List<AnalysisResultBean> getAnalysisResult() {
        return analysisResultBeanList;
    }

    @Override
    public List<ByteCodeParser.OpcodeInfo> getOpcodeInfoList() {
        return mOpcodeInfoList;
    }

    @Override
    public Collection<File> getCheckFiles() {
        return mCheckFiles;
    }

    public static class Builder {
        private List<CommonOpcodeAnalysisItem> list;
        private List<AnalysisResultBean> analysisList;
        private List<ByteCodeParser.OpcodeInfo> opcodeInfoList;
        private Collection<File> checkFiles;

        public Builder analysisList(List<CommonOpcodeAnalysisItem> analysisItemList) {
            this.list = analysisItemList;
            return this;
        }

        public Builder analysisResult(List<AnalysisResultBean> resultList) {
            this.analysisList = resultList;
            return this;
        }

        public Builder opcodeInfoList(List<ByteCodeParser.OpcodeInfo> opcodeInfoList) {
            this.opcodeInfoList = opcodeInfoList;
            return this;
        }

        public Builder checkFiles(Collection<File> checkFiles) {
            this.checkFiles = checkFiles;
            return this;
        }

        public SimpleTaskOutput build() {
            SimpleTaskOutput output = new SimpleTaskOutput();
            output.analysisList = list;
            output.analysisResultBeanList = analysisList;
            output.mOpcodeInfoList = opcodeInfoList;
            output.mCheckFiles = checkFiles;
            return output;
        }
    }

}
