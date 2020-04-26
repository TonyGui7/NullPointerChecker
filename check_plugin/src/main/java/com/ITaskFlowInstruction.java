package com;

import com.bytecode.parser.ByteCodeParser;
import com.nullpointer.analysis.bean.OpcodeInfoItem;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 整个空指针检测分析的任务流说明接口
 *
 * <p>
 * 该接口定义所有任务接口，若由新增的任务，则添加对应的任务接口，并实现该任务
 * 然后在{@link TaskFlowManager#assembleTasks()}中添加新增任务，需要注意不同任务的执行顺序
 * </>
 *
 * <p>
 * {@link IOpcodeFilter} 过滤器接口， 过滤出需要进行空指针检测分析的字节码指令集
 * {@link IOpcodeAnalyser} 空指针分析器接口，接受过滤器输出的数据进行空指针分析
 * {@link IOpcodeTranslator} 空指针信息翻译器接口，接受分析器输出的字节码信息，将其翻译成java源码层级的信息，以便阅读
 * </>
 *
 * @author guizhihong
 */
public interface ITaskFlowInstruction {

    int DEFAULT_VALUE = -1;

    interface IBytecodeParser<BInput, BOutput> extends TaskContract.SimpleTask<BInput, BOutput>{

    }

    interface IOpcodeFilter<FInput, FOutput> extends TaskContract.SimpleTask<FInput, FOutput> {

        List<OpcodeInfoItem> filter(ByteCodeParser.OpcodeInfo opcodeInfo);
    }

    interface IOpcodeAnalyser<AInput, AOutput> extends TaskContract.SimpleTask<AInput, AOutput> {

        @interface IntRef {
            int[] ref();
        }

        @IntRef(ref = {INVOKE_TYPE, FIELD_TYPE, VARIABLE_TYPE, CAST_TYPE, LDC_TYPE, RETURN_TYPE, CONST_TYPE})
        @interface OpcodeType {
        }

        int INVOKE_TYPE = 1;
        int FIELD_TYPE = 2;
        int VARIABLE_TYPE = 3;
        int CAST_TYPE = 4;
        int LDC_TYPE = 5;
        int RETURN_TYPE = 6;
        int CONST_TYPE = 7;

        interface IAtomicOpcodeAnalyser<AInput, AOutput> extends TaskContract.AtomicTask<AInput, AOutput> {

        }
    }

    interface IOpcodeTranslator<TInput, TOutput> extends TaskContract.SimpleTask<TInput, TOutput> {

    }


    void assembleTasks();

    void begin(Collection<File> classFileList);

    void setCallback(Callback callback);

    interface Callback {
        void onTaskFinished(TaskBeanContract.ISimpleTaskOutput result);
    }
}
