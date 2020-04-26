package com.nullpointer.analysis.tasks.analyser;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;

import java.util.List;

/**
 * cast类型字节码指令判断是否判空分析器
 *
 * @author guizhihong
 */

public class CastOpcodeAnalyser extends BaseOpcodeAnalyser {
    @Override
    protected void analyseSpecificTypeOpcodes(OpcodeInfoItem checkNullOpcode, int jumpOffset, boolean isIfNull, ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {

    }
}
