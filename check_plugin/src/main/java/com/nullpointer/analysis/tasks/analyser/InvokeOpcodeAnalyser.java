package com.nullpointer.analysis.tasks.analyser;

import com.example.gui.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.tools.AnalyserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * invoke类型字节码指令判断是否判空分析器
 *
 * @author guizhihong
 */
public class InvokeOpcodeAnalyser extends BaseOpcodeAnalyser {

    @Override
    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull) {
        if (mCheckList == null || checkNullOpcode == null || mOpcodeInfo == null) {
            return;
        }

        HashMap<Integer, ByteCodeParser.InvokeOpcodeInfo> invokeOpcodeInfoHashMap = mOpcodeInfo.getInvokeOpcodeInfoHashMap();
        if (invokeOpcodeInfoHashMap == null) {
            return;
        }
        ByteCodeParser.InvokeOpcodeInfo checkNullInvokeOpcodeInfo = invokeOpcodeInfoHashMap.get(checkNullOpcode.offset);
        if (checkNullInvokeOpcodeInfo == null) {
            return;
        }

        List<Integer> invokeOffsetList = new ArrayList<>(invokeOpcodeInfoHashMap.keySet());
        Collections.sort(invokeOffsetList);

        int startIndex = isIfNull ? invokeOffsetList.indexOf(checkNullOpcode.offset) + 1 : getOpcodeIndexGreaterThan(jumpOffset, invokeOffsetList);
        int endIndex = isIfNull ? getOpcodeIndexLessThan(jumpOffset, invokeOffsetList) : invokeOffsetList.size();
        if (startIndex > endIndex || startIndex < 0 || endIndex > invokeOffsetList.size()) {
            return;
        }

        for (int index = startIndex; index < endIndex; index++) {
            int offset = invokeOffsetList.get(index);
            if (checkNullInvokeOpcodeInfo.equals(invokeOpcodeInfoHashMap.get(offset))) {
                //删除作用域范围内的invoke指令信息，这些指令信息已判空
                OpcodeInfoItem opcodeInfoItem = AnalyserUtil.constructOpcodeInfoItem(checkNullInvokeOpcodeInfo.opcode, offset);
                if (mCheckList.contains(opcodeInfoItem)) {
                    mCheckList.remove(opcodeInfoItem);
                }
            }
        }
    }
}
