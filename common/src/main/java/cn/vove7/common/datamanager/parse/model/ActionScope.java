package cn.vove7.common.datamanager.parse.model;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import java.util.Objects;

/**
 * #ActionScope
 * 指令作用域
 *
 * @see Action
 * <p>
 * Created by Vove on 2018/6/18
 */
@Entity()
public class ActionScope {
    @Expose(serialize = false)
    @Id
    private
    Long id;
    /**
     * App包名
     */
    private String packageName;
    /**
     * Activity页面
     */
    private String activity;
    private Integer hashCode;


    @Keep
    public ActionScope(Long id, String packageName, String activity) {
        this.id = id;
        this.packageName = packageName;
        this.activity = activity;
    }

    public int genHashCode() {
        hashCode = Objects.hash(packageName, activity);
        return hashCode;
    }

    @Keep
    public ActionScope(String packageName, String activity) {
        this.packageName = packageName;
        this.activity = activity;
    }

    @Keep
    public ActionScope(String packageName) {
        this.packageName = packageName;
    }


    @Generated(hash = 1143247331)
    public ActionScope() {
    }

    @Generated(hash = 432106659)
    public ActionScope(Long id, String packageName, String activity,
                       Integer hashCode) {
        this.id = id;
        this.packageName = packageName;
        this.activity = activity;
        this.hashCode = hashCode;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public String toString() {
        return "{" + packageName + ", " + activity + '}';
    }

    /**
     * 比较规则
     * A<->B
     * pkg startWith
     * activity 可空√ startWith
     * activity A:null = B:""
     * activity A:"a.b.ccc" = B:"ccc"
     *
     * @param o
     * @return
     */
    public boolean equalsActivityNullable(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionScope that = (ActionScope) o;
        return packageName != null && that.packageName != null && packageName.startsWith(that.packageName) &&
                (that.activity == null || activity == null ||
                        activity.endsWith("." + that.activity) ||
                        activity.endsWith("$" + that.activity) ||
                        that.activity.endsWith("." + activity) ||
                        that.activity.endsWith("$" + activity));
    }


    public boolean assertEquals(ActionScope that) {
        if (that == null) return false;
        return packageName != null && packageName.startsWith(that.packageName) &&
                (that.activity != null && activity != null && (
                        activity.endsWith("." + that.activity) ||
                                activity.endsWith("$" + that.activity)
                ));
    }

    /**
     * 此实例页面是否在指定activity
     *
     * @param fact
     * @return
     */
    public boolean inActivity(String fact) {
        if (this.activity == null || fact == null) return false;

        return fact.endsWith(activity);
    }

    public boolean eqPkg(ActionScope o) {
        if (o.packageName == null) return false;
        return o.packageName.startsWith(this.packageName) ||
                packageName.startsWith(o.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, activity);
    }
}
