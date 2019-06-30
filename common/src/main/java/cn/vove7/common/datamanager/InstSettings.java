package cn.vove7.common.datamanager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Administrator on 9/26/2018
 */
@Entity(indexes = {
        @Index("name")
})
public class InstSettings {
    @Id
    private Long id;
    private String name;
    private String dataJson;
    private String rawJson;//设置信息
    private String from;//来源
    private int version;

    public InstSettings(String name, String rawJson) {
        this.name = name;
        this.rawJson = rawJson;
    }

    public InstSettings(Long id, String name, String dataJson, int v) {
        this.id = id;
        this.name = name;
        this.dataJson = dataJson;
        this.version = v;
    }

    public boolean insertNew() {
        return DAO.INSTANCE.getDaoSession().getInstSettingsDao().insert(this) > 0;
    }

    public void update() {
        DAO.INSTANCE.getDaoSession().getInstSettingsDao().update(this);
    }

    @Generated(hash = 1331071478)
    public InstSettings(Long id, String name, String dataJson, String rawJson,
            String from, int version) {
        this.id = id;
        this.name = name;
        this.dataJson = dataJson;
        this.rawJson = rawJson;
        this.from = from;
        this.version = version;
    }

    @Generated(hash = 441776490)
    public InstSettings() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
