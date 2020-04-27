package com.bytecode.parser;

import com.android.annotations.NonNull;

import org.jetbrains.annotations.Contract;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 通过Asm来解析所需的字节码信息，并将其按空指针检测与分析的目的进行分类存储
 *
 * @author guizhihong
 */
public class ByteCodeParser implements IOpcodesParser {


    /**
     * 存储LineNumberTable
     */
    private HashMap<Integer, Integer> mLineNumberTableInfo;


    /**
     * 存储所有跳转指令的字节偏移量 和 该跳转指令的标签信息Label
     * 主要是为了在字节码code遍历完成后，通过{@link Label#getOffset()}得到该跳转指令在满足条件后要跳转的位置
     * <p>
     * 跳转指令包括 {@link Opcodes#IFEQ,
     * IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT,
     * IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL}
     */
    private HashMap<Integer, JumpOpcodeInfo> mJumpOpcodeLabelInfo;


    private HashMap<Integer, InvokeOpcodeInfo> mInvokeOpcodeInfoMap;


    private HashMap<Integer, VarOpcodeInfo> mVarOpcodeInfoMap;

    private HashMap<Integer, FieldOpcodeInfo> mFieldOpcodeInfoMap;

    private HashMap<Integer, InstanceOfOpcodeInfo> mInstanceOfOpcodeInfoMap;

    private HashMap<Integer, MultiArrayOpcodeInfo> mMultiArrayOpcodeInfoHashMap;

    /**
     * 存储所有字节码指令及其对应的字节偏移量（相对当前栈帧）
     * key是当前指令字节偏移量
     * value是当前操作码指令
     */
    private List<Integer> mOpcodeOffsetList;
    private List<Integer> mOpcodeList;


    private Listener mListener;


    public ByteCodeParser(Listener listener) {
        this.mListener = listener;
    }

    public void start() {
        mOpcodeList = new ArrayList<>();
        mOpcodeOffsetList = new ArrayList<>();


        mLineNumberTableInfo = new HashMap<>();


        mJumpOpcodeLabelInfo = new HashMap<>();
        mInvokeOpcodeInfoMap = new HashMap<>();
        mVarOpcodeInfoMap = new HashMap<>();
        mFieldOpcodeInfoMap = new HashMap<>();
        mInstanceOfOpcodeInfoMap = new HashMap<>();
        mMultiArrayOpcodeInfoHashMap = new HashMap<>();

        if (mListener != null) {
            mListener.onParseStart();
        }

    }

    public void finish(String clzzName, String methodName) {

        OpcodeInfo opcodeInfo = new OpcodeInfo();
        opcodeInfo.currClzzName = clzzName;
        opcodeInfo.currMethodName = methodName;
        opcodeInfo.opcodeInfo = new GeneralOpcodeInfo(mOpcodeOffsetList, mOpcodeList);
        opcodeInfo.varOpcodeInfoHashMap = mVarOpcodeInfoMap;
        opcodeInfo.fieldOpcodeInfoHashMap = mFieldOpcodeInfoMap;
        opcodeInfo.invokeOpcodeInfoHashMap = mInvokeOpcodeInfoMap;
        opcodeInfo.instanceOfOpcodeInfoHashMap = mInstanceOfOpcodeInfoMap;
        opcodeInfo.multiArrayOpcodeInfoHashMap = mMultiArrayOpcodeInfoHashMap;

        arrangeJumpOpcodeInfo();
        opcodeInfo.jumpOpcodeInfoMap = mJumpOpcodeLabelInfo;

        opcodeInfo.lineNumberTable = mLineNumberTableInfo;

        if (mListener != null) {
            mListener.onParseEnd(opcodeInfo);
        }
    }

    private void arrangeJumpOpcodeInfo() {
        if (mJumpOpcodeLabelInfo == null || mJumpOpcodeLabelInfo.isEmpty()) {
            return;
        }

        Collection<JumpOpcodeInfo> values = mJumpOpcodeLabelInfo.values();
        for (JumpOpcodeInfo jumpOpcodeInfo : values) {
            jumpOpcodeInfo.releaseLabel();
        }
    }


    public int parseLdcOpcode(Object object) {
        boolean isLongOrDouble = object instanceof Long || object instanceof Double;

        char firstDescriptorChar;
        boolean isConstantDynamic = object instanceof ConstantDynamic &&
                ((firstDescriptorChar = ((ConstantDynamic) object).getDescriptor().charAt(0)) == 'J'
                        || firstDescriptorChar == 'D');

        if (isLongOrDouble || isConstantDynamic) {
            return LDC2_W;
        }

        //@todo @guizhihong LDC_W 操作码指令判断的依据时当前常量在常量池中的索引大于256，但是Asm没有公共的Api提供获取常量池索引

        return Opcodes.LDC;
    }

    public int parseVarOpcode(int opcode, int var) {
        int result = opcode;
        if (var >= 0 && var <= 3) {
            switch (opcode) {
                case Opcodes.ILOAD:
                    result = ILOAD_0 + var;
                    break;
                case Opcodes.LLOAD:
                    result = LLOAD_0 + var;
                    break;
                case Opcodes.FLOAD:
                    result = FLOAD_0 + var;
                    break;
                case Opcodes.DLOAD:
                    result = DLOAD_0 + var;
                    break;
                case Opcodes.ALOAD:
                    result = ALOAD_0 + var;
                    break;
                case Opcodes.ISTORE:
                    result = ISTORE_0 + var;
                    break;
                case Opcodes.LSTORE:
                    result = LSTORE_0 + var;
                    break;
                case Opcodes.FSTORE:
                    result = FSTORE_0 + var;
                    break;
                case Opcodes.DSTORE:
                    result = DSTORE_0 + var;
                    break;
                case Opcodes.ASTORE:
                    result = ASTORE_0 + var;
                    break;
            }
        }
        return result;
    }


    public int parseOpcodeOffset(int opcode, int lastOffset) {
        int result = lastOffset;
        switch (opcode) {
            case Opcodes.NOP:
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.IALOAD:
            case Opcodes.LALOAD:
            case Opcodes.FALOAD:
            case Opcodes.DALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
            case Opcodes.IASTORE:
            case Opcodes.LASTORE:
            case Opcodes.FASTORE:
            case Opcodes.DASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
            case Opcodes.POP:
            case Opcodes.POP2:
            case Opcodes.DUP:
            case Opcodes.DUP_X1:
            case Opcodes.DUP_X2:
            case Opcodes.DUP2:
            case Opcodes.DUP2_X1:
            case Opcodes.DUP2_X2:
            case Opcodes.SWAP:
            case Opcodes.IADD:
            case Opcodes.LADD:
            case Opcodes.FADD:
            case Opcodes.DADD:
            case Opcodes.ISUB:
            case Opcodes.LSUB:
            case Opcodes.FSUB:
            case Opcodes.DSUB:
            case Opcodes.IMUL:
            case Opcodes.LMUL:
            case Opcodes.FMUL:
            case Opcodes.DMUL:
            case Opcodes.IDIV:
            case Opcodes.LDIV:
            case Opcodes.FDIV:
            case Opcodes.DDIV:
            case Opcodes.IREM:
            case Opcodes.LREM:
            case Opcodes.FREM:
            case Opcodes.DREM:
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.ISHL:
            case Opcodes.LSHL:
            case Opcodes.ISHR:
            case Opcodes.LSHR:
            case Opcodes.IUSHR:
            case Opcodes.LUSHR:
            case Opcodes.IAND:
            case Opcodes.LAND:
            case Opcodes.IOR:
            case Opcodes.LOR:
            case Opcodes.IXOR:
            case Opcodes.LXOR:
            case Opcodes.I2L:
            case Opcodes.I2F:
            case Opcodes.I2D:
            case Opcodes.L2I:
            case Opcodes.L2F:
            case Opcodes.L2D:
            case Opcodes.F2I:
            case Opcodes.F2L:
            case Opcodes.F2D:
            case Opcodes.D2I:
            case Opcodes.D2L:
            case Opcodes.D2F:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.LCMP:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
            case Opcodes.RETURN:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
                result += 1;
                break;
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
                result += 1;
                break;
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
                result += 1;
                break;
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.GOTO:
            case Opcodes.JSR:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                result += 3;
                break;

            // todo @guizhihong asm 共有API无法识别这两个操作码指令
//            case Opcodes.GOTO_W:
//            case Opcodes.JSR_W:
//                methodVisitor.visitJumpInsn(
//                        opcode - wideJumpOpcodeDelta,
//                        labels[currentBytecodeOffset + readInt(result + 1)]);
//                result += 5;
//                break;


//            case Opcodes.ASM_IFEQ:
//            case Opcodes.ASM_IFNE:
//            case Opcodes.ASM_IFLT:
//            case Opcodes.ASM_IFGE:
//            case Opcodes.ASM_IFGT:
//            case Opcodes.ASM_IFLE:
//            case Opcodes.ASM_IF_ICMPEQ:
//            case Opcodes.ASM_IF_ICMPNE:
//            case Opcodes.ASM_IF_ICMPLT:
//            case Opcodes.ASM_IF_ICMPGE:
//            case Opcodes.ASM_IF_ICMPGT:
//            case Opcodes.ASM_IF_ICMPLE:
//            case Opcodes.ASM_IF_ACMPEQ:
//            case Opcodes.ASM_IF_ACMPNE:
//            case Opcodes.ASM_GOTO:
//            case Opcodes.ASM_JSR:
//            case Opcodes.ASM_IFNULL:
//            case Opcodes.ASM_IFNONNULL: {
//                // A forward jump with an offset > 32767. In this case we automatically replace ASM_GOTO
//                // with GOTO_W, ASM_JSR with JSR_W and ASM_IFxxx <l> with IFNOTxxx <L> GOTO_W <l> L:...,
//                // where IFNOTxxx is the "opposite" opcode of ASMS_IFxxx (e.g. IFNE for ASM_IFEQ) and
//                // where <L> designates the instruction just after the GOTO_W.
//                // First, change the ASM specific opcodes ASM_IFEQ ... ASM_JSR, ASM_IFNULL and
//                // ASM_IFNONNULL to IFEQ ... JSR, IFNULL and IFNONNULL.
//                 rentOffset += 3;
//                break;
//            }
//            case Opcodes.ASM_GOTO_W:
//                // Replace ASM_GOTO_W with GOTO_W.
//                result += 5;
//                break;


            case WIDE:
                // todo @guizhihong 暂不清楚该操作码指令的用途，另外该操作码指令的长度不定长
//                opcode = classBuffer[result + 1] & 0xFF;
//                if (opcode == Opcodes.IINC) {
//                    methodVisitor.visitIincInsn(
//                            readUnsignedShort(result + 2), readShort(result + 4));
//                    result += 6;
//                } else {
//                    methodVisitor.visitVarInsn(opcode, readUnsignedShort(result + 2));
//                    result += 4;
//                }
                break;
            case Opcodes.TABLESWITCH: {
                // Skip 0 to 3 padding bytes.
                // todo @guizhihong 暂不清楚该操作码指令的用途，另外该操作码指令的长度不定长
                break;
            }
            case Opcodes.LOOKUPSWITCH:
                // Skip 0 to 3 padding bytes.
                // todo @guizhihong 暂不清楚该操作码指令的用途，另外该操作码指令的长度不定长
                break;
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
            case Opcodes.ISTORE:
            case Opcodes.LSTORE:
            case Opcodes.FSTORE:
            case Opcodes.DSTORE:
            case Opcodes.ASTORE:
            case Opcodes.RET:
                result += 2;
                break;
            case Opcodes.BIPUSH:
            case Opcodes.NEWARRAY:
                result += 2;
                break;
            case Opcodes.SIPUSH:
                result += 3;
                break;
            case Opcodes.LDC:
                result += 2;
                break;


            //常量push到常量池中，这里常量一般是long型，或者double型
            case LDC_W:
            case LDC2_W:
                result += 3;
                break;


            case Opcodes.GETSTATIC:
            case Opcodes.PUTSTATIC:
            case Opcodes.GETFIELD:
            case Opcodes.PUTFIELD:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                if (opcode == Opcodes.INVOKEINTERFACE) {
                    result += 5;
                } else {
                    result += 3;
                }
                break;
            }
            case Opcodes.INVOKEDYNAMIC:
                result += 5;
                break;
            case Opcodes.NEW:
            case Opcodes.ANEWARRAY:
            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF:
                result += 3;
                break;
            case Opcodes.IINC:
                result += 3;
                break;
            case Opcodes.MULTIANEWARRAY:
                result += 4;
                break;
            default:
                break;
        }

        return result;
    }


    public void cacheLineNumberTableInfo(int byteCodeOffset, int lineNumber) {
        if (mLineNumberTableInfo != null) {
            mLineNumberTableInfo.put(byteCodeOffset, lineNumber);
        }
    }


    public void cacheGeneralByteCodeInfo(int opcode, int offset) {
        if (mOpcodeList == null || mOpcodeOffsetList == null) {
            return;
        }

        mOpcodeList.add(opcode);
        mOpcodeOffsetList.add(offset);
    }

    public void cacheJumpOpcodeInfo(int offset, int opcode, Label label) {
        if (mJumpOpcodeLabelInfo != null) {
            mJumpOpcodeLabelInfo.put(offset, new JumpOpcodeInfo(opcode, label));
        }
    }

    //todo @guizhihong 确认invokeDynamic指令是否需要处理
    public void cacheInvokeOpcodeInfo(int offset, int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (mInvokeOpcodeInfoMap == null) {
            return;
        }

        mInvokeOpcodeInfoMap.put(offset, new InvokeOpcodeInfo(opcode, owner, name, descriptor, isInterface));
    }

    public void cacheVarOpcodeInfo(int offset, int opcode, int var) {
        if (mVarOpcodeInfoMap == null) {
            return;
        }

        mVarOpcodeInfoMap.put(offset, new VarOpcodeInfo(opcode, var));
    }

    public void cacheInstanceOfOpcodeInfo(int offset, int opcode, String type) {
        if (mInstanceOfOpcodeInfoMap == null) {
            return;
        }

        mInstanceOfOpcodeInfoMap.put(offset, new InstanceOfOpcodeInfo(opcode, type));
    }

    public void cacheMultiArrayOpcodeInfo(int offset, int opcode, int dimension) {
        if (mMultiArrayOpcodeInfoHashMap == null) {
            return;
        }
        mMultiArrayOpcodeInfoHashMap.put(offset, new MultiArrayOpcodeInfo(opcode, dimension));
    }

    public void cacheFieldOpcodeInfo(int offset, int opcode, String owner, String name, String descriptor) {
        if (mFieldOpcodeInfoMap == null) {
            return;
        }

        mFieldOpcodeInfoMap.put(offset, new FieldOpcodeInfo(opcode, owner, name, descriptor));
    }

    public class OpcodeInfo {
        private String currClzzName;
        private String currMethodName;

        //所有操作码及其字节偏移量
        private GeneralOpcodeInfo opcodeInfo;

        //跳转指令信息集合
        private HashMap<Integer, JumpOpcodeInfo> jumpOpcodeInfoMap;

        //invoke指令信息集合
        private HashMap<Integer, InvokeOpcodeInfo> invokeOpcodeInfoHashMap;

        //访问局部变量表指令信息集合
        private HashMap<Integer, VarOpcodeInfo> varOpcodeInfoHashMap;

        //访问class全局变量指令信息集合
        private HashMap<Integer, FieldOpcodeInfo> fieldOpcodeInfoHashMap;

        private HashMap<Integer, Integer> lineNumberTable;

        private HashMap<Integer, InstanceOfOpcodeInfo> instanceOfOpcodeInfoHashMap;

        private HashMap<Integer, MultiArrayOpcodeInfo> multiArrayOpcodeInfoHashMap;

        public String getCurrClzzName() {
            return currClzzName;
        }

        public String getCurrMethodName() {
            return currMethodName;
        }

        public GeneralOpcodeInfo getOpcodeInfo() {
            return opcodeInfo;
        }

        public HashMap<Integer, JumpOpcodeInfo> getJumpOpcodeInfoList() {
            return jumpOpcodeInfoMap;
        }

        public HashMap<Integer, InvokeOpcodeInfo> getInvokeOpcodeInfoHashMap() {
            return invokeOpcodeInfoHashMap;
        }

        public HashMap<Integer, VarOpcodeInfo> getVarOpcodeInfoHashMap() {
            return varOpcodeInfoHashMap;
        }

        public HashMap<Integer, FieldOpcodeInfo> getFieldOpcodeInfoHashMap() {
            return fieldOpcodeInfoHashMap;
        }

        public HashMap<Integer, InstanceOfOpcodeInfo> getInstanceOfOpcodeInfoHashMap() {
            return instanceOfOpcodeInfoHashMap;
        }

        public HashMap<Integer, MultiArrayOpcodeInfo> getMultiArrayOpcodeInfoHashMap() {
            return multiArrayOpcodeInfoHashMap;
        }

        public HashMap<Integer, Integer> getLineNumberTable() {
            return lineNumberTable;
        }

        public GeneralOpcodeInfo getGeneralOpcodeInfo() {
            return opcodeInfo;
        }
    }


    public class GeneralOpcodeInfo {
        //指令字节偏移量
        private List<Integer> opcodeOffsetList;

        //具体操作码
        private List<Integer> opcodeList;

        public GeneralOpcodeInfo(List<Integer> offsetList, List<Integer> opcodeList) {
            this.opcodeList = opcodeList;
            this.opcodeOffsetList = offsetList;
        }

        public List<Integer> getOpcodeOffsetList() {
            return opcodeOffsetList;
        }

        public List<Integer> getOpcodeList() {
            return opcodeList;
        }
    }

    public class InvokeOpcodeInfo extends BaseOpcodeInfo {
        public String owner;
        public String name;
        public String descriptor;
        public boolean isInterface;

        public InvokeOpcodeInfo(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            this.opcode = opcode;
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
            this.isInterface = isInterface;
        }

        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof InvokeOpcodeInfo)) {
                return false;
            }

            InvokeOpcodeInfo invokeOpcodeInfo = (InvokeOpcodeInfo) object;
            if (invokeOpcodeInfo == this) {
                return true;
            }

            if (this.opcode != invokeOpcodeInfo.opcode) {
                return false;
            }

            if (!this.owner.equals(invokeOpcodeInfo.owner)) {
                return false;
            }

            if (!this.name.equals(invokeOpcodeInfo.name)) {
                return false;
            }

            if (!this.descriptor.equals(invokeOpcodeInfo.descriptor)) {
                return false;
            }

            if (!(this.isInterface == invokeOpcodeInfo.isInterface)) {
                return false;
            }

            return true;
        }

    }

    public class VarOpcodeInfo extends BaseOpcodeInfo {
        public int var;

        public VarOpcodeInfo(int opcode, int var) {
            this.opcode = opcode;
            this.var = var;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof VarOpcodeInfo)) {
                return false;
            }

            VarOpcodeInfo varOpcodeInfo = (VarOpcodeInfo) object;
            if (varOpcodeInfo == this) {
                return true;
            }

            if (this.opcode != varOpcodeInfo.opcode) {
                return false;
            }

            if (this.var != varOpcodeInfo.var) {
                return false;
            }

            return true;
        }
    }

    public class JumpOpcodeInfo extends BaseOpcodeInfo {
        Label label;

        //跳转指令满足条件跳转目标指令的字节偏移量
        public int jumpTargetOffset;

        public JumpOpcodeInfo(int opcode, Label label) {
            this.opcode = opcode;
            this.label = label;
        }

        public void releaseLabel() {
            if (label == null) {
                return;
            }

            jumpTargetOffset = label.getOffset();
            label = null;
        }

    }

    public class FieldOpcodeInfo extends BaseOpcodeInfo {
        @NonNull
        public String owner;
        @NonNull
        public String name;
        @NonNull
        public String descriptor;

        public FieldOpcodeInfo(int opcode, String owner, String name, String descriptor) {
            this.opcode = opcode;
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof FieldOpcodeInfo)) {
                return false;
            }

            FieldOpcodeInfo fieldOpcodeInfo = (FieldOpcodeInfo) object;
            if (fieldOpcodeInfo == this) {
                return true;
            }

            if (this.opcode != fieldOpcodeInfo.opcode) {
                return false;
            }

            if (!this.owner.equals(fieldOpcodeInfo.owner)) {
                return false;
            }

            if (!this.name.equals(fieldOpcodeInfo.name)) {
                return false;
            }

            if (!this.descriptor.equals(fieldOpcodeInfo.descriptor)) {
                return false;
            }

            return true;
        }
    }

    public class InstanceOfOpcodeInfo extends BaseOpcodeInfo {
        public String type;

        public InstanceOfOpcodeInfo(int opcode, String type) {
            this.opcode = opcode;
            this.type = type;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof InstanceOfOpcodeInfo)) {
                return false;
            }

            InstanceOfOpcodeInfo instanceOfOpcodeInfo = (InstanceOfOpcodeInfo) object;
            if (instanceOfOpcodeInfo == this) {
                return true;
            }

            if (this.opcode != instanceOfOpcodeInfo.opcode) {
                return false;
            }

            if (!this.type.equals(instanceOfOpcodeInfo.opcode)) {
                return false;
            }

            return true;
        }

    }

    public class MultiArrayOpcodeInfo extends BaseOpcodeInfo {
        public int dimension;

        public MultiArrayOpcodeInfo(int opcode, int dimension) {
            this.opcode = opcode;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MultiArrayOpcodeInfo)) {
                return false;
            }

            MultiArrayOpcodeInfo arrayOpcodeInfo = (MultiArrayOpcodeInfo) object;
            if (arrayOpcodeInfo == this) {
                return true;
            }

            if (this.opcode != arrayOpcodeInfo.opcode) {
                return false;
            }

            if (this.dimension != arrayOpcodeInfo.dimension) {
                return false;
            }

            return true;
        }
    }

    public class BaseOpcodeInfo {
        public int opcode;
    }

}
