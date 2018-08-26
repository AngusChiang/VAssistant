package cn.vove7.common.datamanager.executor.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Vove on 2018/6/23
 */

@Entity(indexes = {@Index(value = "key")})
public class ServerContact {
   @Id
   private Long id;

   private String key;//关键字
   private String regexStr;

   private String value;

   @Generated(hash = 2120011913)
   public ServerContact(Long id, String key, String regexStr, String value) {
       this.id = id;
       this.key = key;
       this.regexStr = regexStr;
       this.value = value;
   }

   @Generated(hash = 1073561844)
   public ServerContact() {
   }

   public Long getId() {
       return this.id;
   }

   public void setId(Long id) {
       this.id = id;
   }

   public String getKey() {
       return this.key;
   }

   public void setKey(String key) {
       this.key = key;
   }

   public String getRegexStr() {
       return this.regexStr;
   }

   public void setRegexStr(String regexStr) {
       this.regexStr = regexStr;
   }

   public String getValue() {
       return this.value;
   }

   public void setValue(String value) {
       this.value = value;
   }
}
