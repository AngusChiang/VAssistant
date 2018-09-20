package cn.vove7.common.datamanager.history;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

import cn.vove7.common.model.UserInfo;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by 17719247306 on 2018/9/11
 */
@Entity
public class CommandHistory {
    @Id
    private Long id;

    private String commandText;
    private Date commandDate;
    private Long userId = UserInfo.getUserId();
    @Generated(hash = 1154937964)
    public CommandHistory(Long id, String commandText, Date commandDate,
            Long userId) {
        this.id = id;
        this.commandText = commandText;
        this.commandDate = commandDate;
        this.userId = userId;
    }
    @Generated(hash = 676261819)
    public CommandHistory() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCommandText() {
        return this.commandText;
    }
    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }
    public Date getCommandDate() {
        return this.commandDate;
    }
    public void setCommandDate(Date commandDate) {
        this.commandDate = commandDate;
    }
    public Long getUserId() {
        return this.userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
