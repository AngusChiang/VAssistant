package cn.vove7.datamanager.parse.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Action集合
 * Created by Vove on 2018/6/18
 */
@Entity
public class Action implements Comparable<Action> {
   @Id
   private
   Long id;

   private String matchWord;
   /**
    * 执行优先级
    */
   private int priority;
   private long nodeId;

   /**
    * 格式：
    * openApp:$var
    * clickText:$var
    * clickId:$var
    * back
    * recent
    * pullNotification
    * call
    */
   private String actionScript;
   /**
    * 操作参数
    */
   @Transient
   private
   Param param;
   /**
    * 请求结果
    */
   Boolean responseResult = true;
   /**
    * 返回数据
    */
   @Transient
   Bundle responseBundle = new Bundle();
   /**
    * 启动App，其他
    */
   public static int ACTION_OPEN = 1;
   /**
    * 拨打电话
    */
   public static int ACTION_CALL = 2;
   public static int ACTION_CLICK = 3;

   @Generated(hash = 1657105256)
   public Action(Long id, String matchWord, int priority, long nodeId,
           String actionScript, Boolean responseResult) {
       this.id = id;
       this.matchWord = matchWord;
       this.priority = priority;
       this.nodeId = nodeId;
       this.actionScript = actionScript;
       this.responseResult = responseResult;
   }

   @Generated(hash = 2056262033)
   public Action() {
   }

   public Action(Long id, String actionScript) {
      this.id = id;
      this.actionScript = actionScript;
   }

   public Action(String actionScript) {
      this.actionScript = actionScript;
   }

   public Action(int priority, String actionScript) {
      this.priority = priority;
      this.actionScript = actionScript;
   }

   public Param getParam() {
      return param;
   }

   public void setParam(Param param) {
      this.param = param;
   }

   public Bundle getResponseBundle() {
      return responseBundle;
   }

   public void setResponseBundle(Bundle responseBundle) {
      this.responseBundle = responseBundle;
   }

   @Override
   public int compareTo(@NonNull Action o) {
      return priority - o.priority;
   }

   public Long getId() {
       return this.id;
   }

   public void setId(Long id) {
       this.id = id;
   }

   public String getMatchWord() {
       return this.matchWord;
   }

   public void setMatchWord(String matchWord) {
       this.matchWord = matchWord;
   }

   public int getPriority() {
       return this.priority;
   }

   public void setPriority(int priority) {
       this.priority = priority;
   }

   public long getNodeId() {
       return this.nodeId;
   }

   public void setNodeId(long nodeId) {
       this.nodeId = nodeId;
   }

   public String getActionScript() {
       return this.actionScript;
   }

   public void setActionScript(String actionScript) {
       this.actionScript = actionScript;
   }

   public Boolean getResponseResult() {
       return this.responseResult;
   }

   public void setResponseResult(Boolean responseResult) {
       this.responseResult = responseResult;
   }
}
