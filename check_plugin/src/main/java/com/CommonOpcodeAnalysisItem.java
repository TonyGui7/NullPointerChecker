package com;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;

import java.util.List;

public class CommonOpcodeAnalysisItem {
    private ByteCodeParser.OpcodeInfo opcodeInfo;
    private List<OpcodeInfoItem> checkList;
    public ByteCodeParser.OpcodeInfo getOpcodeInfo() {
        return opcodeInfo;
    }

    public void setOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo) {
        this.opcodeInfo = opcodeInfo;
    }

    public List<OpcodeInfoItem> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<OpcodeInfoItem> checkList) {
        this.checkList = checkList;
    }
}
