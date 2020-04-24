package com.nullpointer.analysis;

import com.example.gui.ByteCodeParser;
import com.example.gui.IOpcodesParser;
import com.nullpointer.analysis.bean.TaskBeanContract;

/**
 * 字节码解析结果接受器
 *
 * @author guizhihong
 */
public class OpcodeParserReceiver implements IOpcodesParser.Listener, ITaskFlowInstruction.Callback {

    private ByteCodeParser.OpcodeInfo mOpcodeInfo;
    private ITaskFlowInstruction mManager;
    private TaskBeanContract.ISimpleTaskOutput mResult;

    @Override
    public void onParseStart() {
        mManager = new TaskFlowManager();
        mManager.setCallback(this);
    }

    @Override
    public void onParseEnd(ByteCodeParser.OpcodeInfo opcodeInfo) {
        if (opcodeInfo == null) {
            return;
        }

        mOpcodeInfo = opcodeInfo;
        mManager.assembleTasks();
        mManager.begin(opcodeInfo);
    }

    @Override
    public void onTaskFinished(TaskBeanContract.ISimpleTaskOutput result) {
        mResult = result;
    }
}
