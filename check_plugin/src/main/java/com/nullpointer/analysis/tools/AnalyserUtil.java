package com.nullpointer.analysis.tools;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;

import org.objectweb.asm.Opcodes;

import static com.ITaskFlowInstruction.IOpcodeAnalyser.CAST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.CONST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

/**
 * 空指针分析器工具
 *
 * @author guizhihong
 */
public class AnalyserUtil {

    public static OpcodeInfoItem getBeforeOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo, int targetOffset) {
        if (opcodeInfo == null || opcodeInfo.getGeneralOpcodeInfo() == null) {
            return null;
        }

        ByteCodeParser.GeneralOpcodeInfo generalOpcodeInfo = opcodeInfo.getGeneralOpcodeInfo();
        if (generalOpcodeInfo.getOpcodeList() == null || generalOpcodeInfo.getOpcodeOffsetList() == null) {
            return null;
        }

        int targetIndex = generalOpcodeInfo.getOpcodeOffsetList().indexOf(targetOffset);
        if (targetIndex - 1 < 0 || targetIndex - 1 > generalOpcodeInfo.getOpcodeList().size() - 1) {
            return null;
        }

        OpcodeInfoItem result = new OpcodeInfoItem();
        result.offset = generalOpcodeInfo.getOpcodeOffsetList().get(targetIndex - 1);
        result.opcode = generalOpcodeInfo.getOpcodeList().get(targetIndex - 1);
        return result;
    }


    public static OpcodeInfoItem getAfterOpcodeInfo(ByteCodeParser.OpcodeInfo opcodeInfo, int targetOffset) {
        if (opcodeInfo == null || opcodeInfo.getGeneralOpcodeInfo() == null) {
            return null;
        }

        ByteCodeParser.GeneralOpcodeInfo generalOpcodeInfo = opcodeInfo.getGeneralOpcodeInfo();
        if (generalOpcodeInfo.getOpcodeList() == null || generalOpcodeInfo.getOpcodeOffsetList() == null) {
            return null;
        }

        int targetIndex = generalOpcodeInfo.getOpcodeOffsetList().indexOf(targetOffset);
        if (targetIndex + 1 < 0 || targetIndex + 1 > generalOpcodeInfo.getOpcodeList().size() - 1) {
            return null;
        }

        OpcodeInfoItem result = new OpcodeInfoItem();
        result.offset = generalOpcodeInfo.getOpcodeOffsetList().get(targetIndex + 1);
        result.opcode = generalOpcodeInfo.getOpcodeList().get(targetIndex + 1);
        return result;
    }


    public static OpcodeInfoItem constructOpcodeInfoItem(int opcode, int offset) {
        OpcodeInfoItem opcodeInfoItem = new OpcodeInfoItem();
        opcodeInfoItem.opcode = opcode;
        opcodeInfoItem.offset = offset;
        return opcodeInfoItem;
    }

    public static int classifyOpcode(int opcode) {
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEINTERFACE) {
            return INVOKE_TYPE;
        }

        if (opcode == Opcodes.GETFIELD) {
            return FIELD_TYPE;
        }

        //todo @guizhihong 加载局部变量的指令不止这些，需要进一步排查
        if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD) {
            return VARIABLE_TYPE;
        }

        if (opcode == Opcodes.LDC || opcode == Opcodes.GETSTATIC) {
            return LDC_TYPE;
        }

        if (opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DCONST_1) {
            return CONST_TYPE;
        }

        //todo @guizhihong 强转指令没有缓存相关信息，需要进一步check
        if (opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) {
            return CAST_TYPE;
        }

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            return RETURN_TYPE;
        }

        return 0;
    }


}
