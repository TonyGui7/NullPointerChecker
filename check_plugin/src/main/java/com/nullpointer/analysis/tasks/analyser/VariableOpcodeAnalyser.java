package com.nullpointer.analysis.tasks.analyser;

import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.tools.AnalyserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 局部变量类型字节码指令判断是否判空分析器
 *
 * @author guizhihong
 */
public class VariableOpcodeAnalyser extends BaseOpcodeAnalyser {
    @Override
    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull) {
        if (mCheckList == null || checkNullOpcode == null || mOpcodeInfo == null) {
            return;
        }

        HashMap<Integer, ByteCodeParser.VarOpcodeInfo> varOpcodeInfoHashMap = mOpcodeInfo.getVarOpcodeInfoHashMap();
        if (varOpcodeInfoHashMap == null || varOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        ByteCodeParser.VarOpcodeInfo varOpcodeInfo = varOpcodeInfoHashMap.get(checkNullOpcode.offset);
        if (varOpcodeInfo == null) {
            return;
        }

        List<Integer> offsetList = new ArrayList<>(varOpcodeInfoHashMap.keySet());
        Collections.sort(offsetList);

        int startIndex = isIfNull ? offsetList.indexOf(checkNullOpcode.offset) + 1 : getOpcodeIndexGreaterThan(jumpOffset, offsetList);
        int endIndex = isIfNull ? getOpcodeIndexLessThan(jumpOffset, offsetList) : offsetList.size();
        if (startIndex > endIndex || startIndex < 0 || endIndex > offsetList.size()) {
            return;
        }

        for (int index = startIndex; index < endIndex; index++) {
            int offset = offsetList.get(index);
            if (varOpcodeInfo.equals(varOpcodeInfoHashMap.get(offset))) {
                //删除作用域范围内的var指令信息，这些指令信息已判空
                OpcodeInfoItem opcodeInfoItem = AnalyserUtil.constructOpcodeInfoItem(varOpcodeInfo.opcode, offset);
                if (mCheckList.contains(opcodeInfoItem)) {
                    mCheckList.remove(opcodeInfoItem);
                }
            }
        }


    }
}
