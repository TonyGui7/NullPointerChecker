package com.nullpointer.analysis.bean.output;

import com.nullpointer.analysis.bean.AnalysisResultBean;
import com.nullpointer.analysis.bean.OpcodeInfoItem;
import com.nullpointer.analysis.bean.TaskBeanContract;

import java.util.List;

/**
 * 普通任务的输出
 *
 * @author guizhihong
 */

public class SimpleTaskOutput implements TaskBeanContract.ISimpleTaskOutput {
    private List<OpcodeInfoItem> checkList;
    private List<AnalysisResultBean> analysisResultBeanList;

    @Override
    public List<OpcodeInfoItem> getCheckList() {
        return checkList;
    }

    @Override
    public List<AnalysisResultBean> getAnalysisResult() {
        return analysisResultBeanList;
    }

    public static class Builder {
        private List<OpcodeInfoItem> list;
        private List<AnalysisResultBean> analysisList;

        public Builder checkList(List<OpcodeInfoItem> checkList) {
            this.list = checkList;
            return this;
        }

        public Builder analysisResult(List<AnalysisResultBean> resultList) {
            this.analysisList = resultList;
            return this;
        }


        public SimpleTaskOutput build() {
            SimpleTaskOutput output = new SimpleTaskOutput();
            output.checkList = list;
            output.analysisResultBeanList = analysisList;
            return output;
        }
    }

}
