package com;

import com.android.annotations.NonNull;

/**
 * 任务协议接口
 *
 * @author guizhihong
 */
public interface TaskContract {
    /**
     * 抽象任务
     *
     * @param <Input>  任务流的输入数据
     * @param <Output> 任务流的输出数据
     */
    interface AbstractTask<Input, Output> {
        void start(@NonNull Input input, @NonNull Listener<Output> listener);

        void end();

        interface Listener<Output> {
            void notifyEnd(@NonNull Output output);
        }
    }

    /**
     * 原子任务，不可拆分
     *
     * @param <T1> 任务流的输入数据
     * @param <T2> 任务流的输出数据
     */
    interface AtomicTask<T1, T2> extends AbstractTask<T1, T2> {

    }

    /**
     * 普通任务，可由多个原子任务组成
     *
     * @param <S1> 任务流的输入数据
     * @param <S2> 任务流的输出数据
     */
    interface SimpleTask<S1, S2> extends AtomicTask<S1, S2> {

    }
}
