package cn.vove7.common.datamanager.executor.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import cn.vove7.common.datamanager.parse.DataFrom;

/**
 * Created by Vove on 2018/6/23
 */
@Entity(indexes = {
        @Index(value = "key")
})
public class MarkedContact implements DataFrom {

    @Id
    private Long id;
    @NotNull
    private String key;

    private String contactName;
    @NotNull
    private
    String phone;

    private String from = null;


    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Generated(hash = 1590683838)
    public MarkedContact() {
    }

    @Generated(hash = 1839013724)
    public MarkedContact(Long id, @NotNull String key, String contactName,
            @NotNull String phone, String from) {
        this.id = id;
        this.key = key;
        this.contactName = contactName;
        this.phone = phone;
        this.from = from;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
