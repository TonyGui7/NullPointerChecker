package com.nullpointer.analysis.tools;

import com.CommonOpcodeAnalysisItem;
import com.ITaskFlowInstruction;
import com.bytecode.parser.ByteCodeParser;
import com.bytecode.parser.IOpcodesParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;

import org.objectweb.asm.Opcodes;

import java.util.List;

import static com.ITaskFlowInstruction.IOpcodeAnalyser.ARITHMETIC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.ARRAY_LENGTH_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.ARRAY_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.CAST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.COMPARE_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.CONST_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.CONVERT_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.DUP_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.EXCEPTION_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.FIELD_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.INVOKE_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.JUMP_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.LDC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.LOCK_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.NEW_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.POP_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.PUSH_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.RETURN_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.STATIC_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.SWAP_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.SWITCH_TYPE;
import static com.ITaskFlowInstruction.IOpcodeAnalyser.VARIABLE_TYPE;

/**
 * 空指针分析器工具
 *
 * @author guizhihong
 */
public class AnalyserUtil {

    public static CommonOpcodeAnalysisItem getAnalysisItem(ByteCodeParser.OpcodeInfo opcodeInfo, List<OpcodeInfoItem> checkList) {
        CommonOpcodeAnalysisItem analysisItem = new CommonOpcodeAnalysisItem();
        analysisItem.setCheckList(checkList);
        analysisItem.setOpcodeInfo(opcodeInfo);
        return analysisItem;
    }

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

    @ITaskFlowInstruction.IOpcodeAnalyser.OpcodeType
    public static int classifyOpcode(int opcode) {
        //TODO @guizhihong invokeDynamic指令
        if (opcode >= Opcodes.INVOKEVIRTUAL && opcode <= Opcodes.INVOKEINTERFACE) {
            return INVOKE_TYPE;
        }

        if (opcode >= Opcodes.GETFIELD && opcode <= Opcodes.PUTFIELD) {
            return FIELD_TYPE;
        }

        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
            return STATIC_TYPE;
        }

        if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD) {
            return VARIABLE_TYPE;
        }

        if (opcode >= IOpcodesParser.ILOAD_0 && opcode <= IOpcodesParser.ALOAD_3) {
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

        if (opcode == Opcodes.NEW) {
            return NEW_TYPE;
        }

        if (opcode == Opcodes.NEWARRAY || opcode == Opcodes.ANEWARRAY || opcode == Opcodes.MULTIANEWARRAY) {
            return ARRAY_TYPE;
        }

        if (opcode == Opcodes.ARRAYLENGTH) {
            return ARRAY_LENGTH_TYPE;
        }

        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
            return PUSH_TYPE;
        }

        if (opcode == Opcodes.POP || opcode == Opcodes.POP2) {
            return POP_TYPE;
        }

        if (opcode >= Opcodes.DUP && opcode <= Opcodes.DUP2_X2) {
            return DUP_TYPE;
        }

        if (opcode == Opcodes.SWAP) {
            return SWAP_TYPE;
        }

        if (opcode >= Opcodes.IADD && opcode <= Opcodes.IINC) {
            return ARITHMETIC_TYPE;
        }

        if (opcode >= Opcodes.I2L && opcode <= Opcodes.I2S) {
            return CONVERT_TYPE;
        }

        if (opcode >= Opcodes.LCMP && opcode <= Opcodes.DCMPG) {
            return COMPARE_TYPE;
        }

        if (opcode >= Opcodes.IFEQ && opcode <= Opcodes.GOTO || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL
                || opcode == Opcodes.JSR || opcode == Opcodes.RET) {
            return JUMP_TYPE;
        }

        if (opcode == Opcodes.MONITORENTER || opcode == Opcodes.MONITOREXIT) {
            return LOCK_TYPE;
        }

        if (opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH) {
            return SWITCH_TYPE;
        }

        if (opcode == Opcodes.ATHROW) {
            return EXCEPTION_TYPE;
        }


        return 0;
    }

    public static boolean isInvokeInit(ByteCodeParser.InvokeOpcodeInfo invokeOpcodeInfo) {
        if (invokeOpcodeInfo == null) {
            return false;
        }
        return "<init>".equals(invokeOpcodeInfo.name) && invokeOpcodeInfo.opcode == Opcodes.INVOKESPECIAL;
    }

    public static boolean isStaticBlockCode(ByteCodeParser.OpcodeInfo opcodeInfo) {
        if (opcodeInfo == null) {
            return false;
        }
        return "<clinit>".equals(opcodeInfo.getCurrMethodName());
    }


}
