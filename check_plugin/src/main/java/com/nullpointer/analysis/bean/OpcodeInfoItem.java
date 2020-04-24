package com.nullpointer.analysis.bean;

/**
 * 每条字节码指令通用封装对象，方便不同task传递
 *
 * @author guizhihong
 */
public class OpcodeInfoItem {
    public int offset;
    public int opcode;

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof OpcodeInfoItem)) {
            return false;
        }

        OpcodeInfoItem opcodeInfoItem = (OpcodeInfoItem) object;
        if (opcodeInfoItem.offset != this.offset || opcodeInfoItem.opcode != this.opcode) {
            return false;
        }

        return true;
    }
}
