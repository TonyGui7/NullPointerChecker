package com.nullpointer.analysis.tasks.analyser.atomicAnalyser;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.tools.AnalyserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 成员变量类型字节码指令判断是否判空分析器
 *
 * @author guizhihong
 */
public class FieldOpcodeAnalyser extends BaseOpcodeAnalyser {
    @Override
    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull, ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        if (checkList == null || checkNullOpcode == null || opcodeInfo == null) {
            return;
        }

        HashMap<Integer, ByteCodeParser.FieldOpcodeInfo> fieldOpcodeInfoHashMap = opcodeInfo.getFieldOpcodeInfoHashMap();
        if (fieldOpcodeInfoHashMap == null || fieldOpcodeInfoHashMap.isEmpty()) {
            return;
        }

        ByteCodeParser.FieldOpcodeInfo fieldOpcodeInfo = fieldOpcodeInfoHashMap.get(checkNullOpcode.offset);
        if (fieldOpcodeInfo == null) {
            return;
        }

        List<Integer> offsetList = new ArrayList<>(fieldOpcodeInfoHashMap.keySet());
        Collections.sort(offsetList);

        int startIndex = isIfNull ? offsetList.indexOf(checkNullOpcode.offset) + 1 : getOpcodeIndexGreaterThan(jumpOffset, offsetList);
        int endIndex = isIfNull ? getOpcodeIndexLessThan(jumpOffset, offsetList) : offsetList.size();
        if (startIndex > endIndex || startIndex < 0 || endIndex > offsetList.size()) {
            return;
        }

        for (int index = startIndex; index < endIndex; index++) {
            int offset = offsetList.get(index);
            if (fieldOpcodeInfo.equals(fieldOpcodeInfoHashMap.get(offset))) {
                //删除作用域范围内的var指令信息，这些指令信息已判空
                OpcodeInfoItem opcodeInfoItem = AnalyserUtil.constructOpcodeInfoItem(fieldOpcodeInfo.opcode, offset);
                if (checkList.contains(opcodeInfoItem)) {
                    checkList.remove(opcodeInfoItem);
                }
            }
        }
    }
}
