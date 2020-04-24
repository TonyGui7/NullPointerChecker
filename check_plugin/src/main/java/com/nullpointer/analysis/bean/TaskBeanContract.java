package com.nullpointer.analysis.bean;

import com.example.gui.ByteCodeParser;

import java.util.List;

/**
 * 任务bean协议接口
 *
 * @author guizhihong
 */
public interface TaskBeanContract {
    interface IAtomicTaskInput {
        ByteCodeParser.OpcodeInfo getOpcodeInfo();

        void setOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo);

        List<OpcodeInfoItem> getCheckList();

        void setCheckList(List<OpcodeInfoItem> checkList);
    }

    interface IAtomicTaskOutput {
        List<OpcodeInfoItem> getCheckList();
    }

    interface ISimpleTaskInput extends IAtomicTaskInput {

    }

    interface ISimpleTaskOutput extends IAtomicTaskOutput {
        List<AnalysisResultBean> getAnalysisResult();
    }
}
