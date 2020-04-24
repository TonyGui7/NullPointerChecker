package com.nullpointer.analysis.bean.input;

import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.bean.TaskBeanContract;

import java.util.List;

/**
 * 普通任务的输入
 *
 * @author guizhihong
 */
public class SimpleTaskInput implements TaskBeanContract.ISimpleTaskInput {
    private ByteCodeParser.OpcodeInfo opcodeInfo;
    private List<OpcodeInfoItem> checkList;

    @Override
    public ByteCodeParser.OpcodeInfo getOpcodeInfo() {
        return opcodeInfo;
    }

    @Override
    public void setOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo) {
        this.opcodeInfo = opcodeInfo;
    }

    @Override
    public List<OpcodeInfoItem> getCheckList() {
        return checkList;
    }

    @Override
    public void setCheckList(List<OpcodeInfoItem> checkList) {
        this.checkList = checkList;
    }

}
