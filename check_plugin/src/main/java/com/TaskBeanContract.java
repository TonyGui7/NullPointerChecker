package com;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.AnalysisResultBean;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 任务bean协议接口
 *
 * @author guizhihong
 */
public interface TaskBeanContract {
    interface IAtomicTaskInput {
        List<ByteCodeParser.OpcodeInfo> getOpcodeInfoList();

        void setOpcodeInfoList(List<ByteCodeParser.OpcodeInfo> opcodeInfoList);

        List<CommonOpcodeAnalysisItem> getAnalysisList();

        void setAnalysisList(List<CommonOpcodeAnalysisItem> checkList);
    }

    interface IAtomicTaskOutput {
        List<CommonOpcodeAnalysisItem> getAnalysisList();
    }

    interface ISimpleTaskInput extends IAtomicTaskInput {
        void setClassFiles(Collection<File> classFiles);

        Collection<File> getClassFiles();
    }

    interface ISimpleTaskOutput extends IAtomicTaskOutput {
        List<AnalysisResultBean> getAnalysisResult();

        List<ByteCodeParser.OpcodeInfo> getOpcodeInfoList();
    }
}
