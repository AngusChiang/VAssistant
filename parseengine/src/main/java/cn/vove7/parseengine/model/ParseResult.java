package cn.vove7.parseengine.model;

import java.util.PriorityQueue;

import cn.vove7.datamanager.parse.model.Action;

/**
 * 解析结果
 * Created by Vove on 2018/6/18
 */
public class ParseResult {
   private boolean isSuccess = false;
   private PriorityQueue<Action> actionQueue;

   public ParseResult(Boolean isSuccess) {
      this.isSuccess = isSuccess;
   }

   public ParseResult(Boolean isSuccess, PriorityQueue<Action> actionQueue) {
      this.isSuccess = isSuccess;
      this.actionQueue = actionQueue;
   }

   public boolean isSuccess() {
      return isSuccess;
   }

   public void setSuccess(boolean success) {
      isSuccess = success;
   }

   public PriorityQueue<Action> getActionQueue() {
      return actionQueue;
   }

   public void setActionQueue(PriorityQueue<Action> actionQueue) {
      this.actionQueue = actionQueue;
   }
}