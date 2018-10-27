package cn.vove7.jarvis.tools.backup;

import java.util.ArrayList;
import java.util.List;

import cn.vove7.common.datamanager.AppAdInfo;
import cn.vove7.common.datamanager.executor.entity.MarkedData;
import cn.vove7.common.datamanager.parse.statusmap.ActionNode;

/**
 * Created by Administrator on 2018/10/17
 */
public class BackupWrap {
    private List<MarkedData> markedDataList;
    private List<AppAdInfo> appAdList;
    private List<ActionNode> instList;

    public BackupWrap() {
        markedDataList = new ArrayList<>();
        appAdList = new ArrayList<>();
        instList = new ArrayList<>();
    }

    public List<MarkedData> getMarkedDataList() {
        return markedDataList;
    }

    public void setMarkedDataList(List<MarkedData> markedDataList) {
        this.markedDataList = markedDataList;
    }

    public List<AppAdInfo> getAppAdList() {
        return appAdList;
    }

    public void setAppAdList(List<AppAdInfo> appAdList) {
        this.appAdList = appAdList;
    }

    public List<ActionNode> getInstList() {
        return instList;
    }

    public void setInstList(List<ActionNode> instList) {
        this.instList = instList;
    }
}
