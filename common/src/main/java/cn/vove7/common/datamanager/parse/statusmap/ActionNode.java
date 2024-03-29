package cn.vove7.common.datamanager.parse.statusmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.vove7.common.app.GlobalLog;
import cn.vove7.common.datamanager.DAO;
import cn.vove7.common.datamanager.greendao.ActionDao;
import cn.vove7.common.datamanager.greendao.ActionDescDao;
import cn.vove7.common.datamanager.greendao.ActionNodeDao;
import cn.vove7.common.datamanager.greendao.ActionScopeDao;
import cn.vove7.common.datamanager.greendao.DaoSession;
import cn.vove7.common.datamanager.greendao.RegDao;
import cn.vove7.common.datamanager.parse.DataFrom;
import cn.vove7.common.datamanager.parse.model.Action;
import cn.vove7.common.datamanager.parse.model.ActionDesc;
import cn.vove7.common.datamanager.parse.model.ActionScope;
import cn.vove7.common.interfaces.Searchable;
import cn.vove7.common.model.UserInfo;
import cn.vove7.vtp.log.Vog;

/**
 * 状态图节点
 * - 1 Node对应1参数
 * Created by Vove on 2018/6/17
 */
@Entity
public class ActionNode implements Serializable, Searchable, DataFrom {
    public static final long serialVersionUID = 1210203;
    @Expose(serialize = false)
    @Id
    private Long id;
    /**
     * 一个操作对应多种"说法"
     */
    //指令作用范围
    private int actionScopeType = -1;

    public static final int NODE_SCOPE_GLOBAL = 0x1;
    public static final int NODE_SCOPE_IN_APP = 0x2;

    private boolean autoLaunchApp = true;
    @Deprecated
    public static final int NODE_SCOPE_GLOBAL_2 = 0x4;//follows
    @Deprecated
    public static final int NODE_SCOPE_IN_APP_2 = 0x3;//2后操作

    public boolean belongInApp() {
        return actionScopeType == NODE_SCOPE_IN_APP;
    }

    @Override
    public boolean onSearch(@org.jetbrains.annotations.NotNull String text) {
        return actionTitle.contains(text);
    }

    /**
     * 属于用户自己
     *
     * @return
     */
    public boolean belongSelf() {
        Long uId = UserInfo.getUserId();
        return from == null || DataFrom.FROM_USER.equals(from) ||
                (DataFrom.FROM_SHARED.equals(from) && (publishUserId == null || publishUserId.equals(uId)));
        //return DataFrom.FROM_SHARED.equals(from)
        //        && (publishUserId != null &&
        //        publishUserId.equals(UserInfo.getUserId()));
    }

    public boolean infoIsOk() {
        return actionTitle != null && DataFrom.FROM_USER.equals(from) && actionScopeType >= 1 && actionScopeType <= 4;
    }

    public static boolean belongInApp(Integer type) {
        if (type == null) return false;
        return type == NODE_SCOPE_IN_APP /*|| type == NODE_SCOPE_IN_APP_2*/;
    }

    public static boolean belongGlobal(int type) {
        return type == NODE_SCOPE_GLOBAL /*|| type == NODE_SCOPE_GLOBAL_2*/;
    }

    @ToMany(referencedJoinProperty = "nodeId")//一对多 reg 表 nodeId为外键
    private List<Reg> regs;

    /**
     * 一波操作
     */
    @ToOne(joinProperty = "actionId")//一对一,actionId外键
    private Action action;
    private Long actionId = -1L;

    /**
     * 运行时操作参数
     */
    //@ToOne(joinProperty = "paramId")
    @Expose(serialize = false)
    @Transient
    private Map<String, Object> param;
    //private long paramId;

    /**
     * APP作用域
     */
    private Long scopeId;
    @ToOne(joinProperty = "scopeId")
    @SerializedName("scope")
    private ActionScope actionScope;

    private Long descId;

    @ToOne(joinProperty = "descId")
    @SerializedName("desc")
    private ActionDesc desc;

    private String actionTitle;

    private String tagId;//与服务器数据匹配标志 Server端生成
    private int versionCode = 0;

    private Long publishUserId;//发布者

    //匹配优先级 bigger优先;
    // 相对于action chain :(全局命令下: 返回主页>返回) (同一界面下)
    private int priority;

    private String from = null;
    /**
     * 服务器端
     */
    @Expose(serialize = false)
    @Transient
    private boolean isDelete;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Long getPublishUserId() {
        return publishUserId;
    }

    public void setPublishUserId(Long publishUserId) {
        this.publishUserId = publishUserId;
    }

    @Generated(hash = 462335395)
    private transient Long action__resolvedKey;
    @Generated(hash = 1119678907)
    private transient Long actionScope__resolvedKey;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1336300321)
    private transient ActionNodeDao myDao;
    @Generated(hash = 1157301878)
    private transient Long desc__resolvedKey;

    public void setRegs(List<Reg> regs) {
        this.regs = regs;
    }

    @Keep
    public ActionNode(Long id, int type) {
        this.id = id;
        this.actionScopeType = type;
    }

    @Keep
    public ActionNode(Long id, long actionId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.actionScopeType = type;
    }

    @Keep
    public ActionNode(Long id, long actionId, long scopeId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.actionScopeType = type;
    }

    public ActionNode(String actionTitle, Long id, long actionId, long scopeId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.actionScopeType = type;
        this.actionTitle = actionTitle;
    }

    public ActionNode(String actionTitle, Action action) {
        this.actionTitle = actionTitle;
        this.action = action;
    }

    public ActionNode(String actionTitle, Long id, int type) {
        this.id = id;
        this.actionScopeType = type;
        this.actionTitle = actionTitle;
    }


    @Keep
    public ActionNode(String actionTitle, Long id, long actionId, int type, long parentId) {
        this.id = id;
        this.actionId = actionId;
        this.actionScopeType = type;
        this.actionTitle = actionTitle;
    }

    @Keep
    public ActionNode(String actionTitle, Long id, long actionId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.actionScopeType = type;
        this.actionTitle = actionTitle;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public void setActionTitle(String actionTitle) {
        this.actionTitle = actionTitle;
    }

    public long getActionId() {
        return actionId;
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }

    public Long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    @Keep
    public ActionNode() {
    }

    @Keep
    public ActionNode(Long id, int actionScopeType, long actionId, long scopeId,
                      String actionTitle, Long parentId, int priority) {
        this.id = id;
        this.actionScopeType = actionScopeType;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.actionTitle = actionTitle;
        this.priority = priority;
    }

    @Generated(hash = 1114179883)
    public ActionNode(Long id, int actionScopeType, boolean autoLaunchApp, Long actionId, Long scopeId, Long descId,
            String actionTitle, String tagId, int versionCode, Long publishUserId, int priority, String from) {
        this.id = id;
        this.actionScopeType = actionScopeType;
        this.autoLaunchApp = autoLaunchApp;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.descId = descId;
        this.actionTitle = actionTitle;
        this.tagId = tagId;
        this.versionCode = versionCode;
        this.publishUserId = publishUserId;
        this.priority = priority;
        this.from = from;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public Action getAction() {
        if (action != null) {// 防止重复执行new Action
            return action;
        }
        if (actionId < 0) {
            this.action = new Action();
            return action;
        }
        long __key = this.actionId;
        if (action__resolvedKey == null || !action__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                return null;
                //throw new DaoException("Entity is detached from DAO context");
            }
            ActionDao targetDao = daoSession.getActionDao();
            Action actionNew = targetDao.load(__key);
            actionNew.setActionScopeType(this.actionScopeType);//同步
            synchronized (this) {
                action = actionNew;
                action__resolvedKey = __key;
            }
        }
        return action;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 2057391106)
    public void setAction(Action action) {
        synchronized (this) {
            this.action = action;
            actionId = action == null ? null : action.getId();
            action__resolvedKey = actionId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public Map<String, Object> getParam() {
        if (this.param != null) {
            return this.param;
        }
        this.param = new HashMap<>();
        return param;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Keep//(hash = 1554982243)
    public void setParam(@NotNull Map<String, Object> param) {
        this.param = param;
    }


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1845482996)
    public void setActionScope(ActionScope actionScope) {
        synchronized (this) {
            this.actionScope = actionScope;
            scopeId = actionScope == null ? null : actionScope.getId();
            actionScope__resolvedKey = scopeId;
        }
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1320822772)
    public synchronized void resetRegs() {
        regs = null;
    }


    public int getActionScopeType() {
        return this.actionScopeType;
    }

    public void setActionScopeType(int actionScopeType) {
        this.actionScopeType = actionScopeType;
    }

    @Override
    public String toString() {
        return actionTitle;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public ActionScope getActionScope() {
        if (actionScope != null) return actionScope;
        Long __key = this.scopeId;
        if (actionScope__resolvedKey == null || !actionScope__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                return null;
                //throw new DaoException("Entity is detached from DAO context");
            }
            ActionScopeDao targetDao = daoSession.getActionScopeDao();
            ActionScope actionScopeNew = targetDao.load(__key);
            synchronized (this) {
                actionScope = actionScopeNew;
                actionScope__resolvedKey = __key;
            }
        }
        return actionScope;
    }

    public List<Reg> getRegsWithoutCache() {
        regs = null;
        return getRegs();
    }

    /**
     * To-many relationship, resolved on first access (and after set).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep
    public List<Reg> getRegs() {
        if (daoSession == null)
            return regs;
        if (regs == null) {
            final DaoSession daoSession = this.daoSession;
            RegDao targetDao = daoSession.getRegDao();
            List<Reg> regsNew = targetDao._queryActionNode_Regs(id);
            synchronized (this) {
                if (regs == null) {
                    regs = regsNew;
                }
            }
        }
        return regs;
    }


    @Keep
    public void assembly2(boolean needParent) {
        getAction();
        getDesc();
        getRegs();
        getActionScope();
    }

    @Keep
    public void assembly() {
        assembly2(true);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Keep
    public void update() {
        if (myDao == null) {
            DAO.INSTANCE.getDaoSession().getActionNodeDao().update(this);
        } else
            myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1450058789)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getActionNodeDao() : null;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public Long getDescId() {
        return descId;
    }

    public void setDescId(Long descId) {
        this.descId = descId;
    }

    public void setScopeId(Long scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public ActionDesc getDesc() {
        if (desc != null) return desc;
        Long __key = this.descId;
        if (desc__resolvedKey == null || !desc__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                return null;
                //throw new DaoException("Entity is detached from DAO context");
            }
            ActionDescDao targetDao = daoSession.getActionDescDao();
            ActionDesc descNew = targetDao.load(__key);
            synchronized (this) {
                desc = descNew;
                desc__resolvedKey = __key;
            }
        }
        return desc;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1849761388)
    public void setDesc(ActionDesc desc) {
        synchronized (this) {
            this.desc = desc;
            descId = desc == null ? null : desc.getId();
            desc__resolvedKey = descId;
        }
    }

    private static final String PreOpen_JS =
            "system.openAppByPkg('%s', true)\n" +
                    "sleep(1000)";

    private static final String PreOpen_LUA =
            "system.openAppByPkg('%s', true)\n" +
                    "sleep(1000)\n";

    private String getInappToGlobal(String pkg, String cmd) {
        return "local packageName = '" + pkg + "'\n" +
                "local cmd = '" + cmd + "'\n" +
                "\n" +
                "function getAppName(packageName)\n" +
                "    import 'com.google.gson.JsonParser'\n" +
                "    local result = http.get('http://47.96.87.117:8082/coolapk/'..packageName)\n" +
                "    if result then\n" +
                "        local arr = JsonParser().parse(result)\n" +
                "        return arr.get(0).asString\n" +
                "    else \n" +
                "        return packageName\n" +
                "    end\n" +
                "end\n" +
                "\n" +
                "if not system.hasInstall(packageName) then\n" +
                "    app.toastError('请先安装 ['.. getAppName(packageName)..']', 1)\n" +
                "    return\n" +
                "end\n" +
                "\n" +
                "result = utils.runAppCommand(packageName, cmd, argMap)\n" +
                "if not result then\n" +
                "    toast(\"指令 [\"..cmd..\"] 未找到\")\n" +
                "end\n";
    }

    /**
     * 从inApp复制一个全局Node
     * 改变脚本
     * 跟随操作最多一层
     *
     * @return
     */
    public ActionNode cloneGlobal(boolean containChild) {
        ActionNode newNode = new ActionNode();
        this.assembly();

        newNode.regs = this.getRegs();

        String script = getInappToGlobal(getActionScope().getPackageName(), getActionTitle());
        newNode.action = new Action(script, Action.SCRIPT_TYPE_LUA);
        newNode.from = DataFrom.FROM_USER;
        newNode.actionTitle = this.actionTitle;
        newNode.publishUserId = UserInfo.getUserId();
        newNode.actionScopeType = NODE_SCOPE_GLOBAL;

        Vog.INSTANCE.d("cloneGlobal ---> \n" + script);
        if (this.desc != null) {
            try {
                newNode.desc = this.getDesc().clone();
            } catch (CloneNotSupportedException e) {
                GlobalLog.INSTANCE.err(e);
            }
        }

        newNode.actionScopeType = ActionNode.NODE_SCOPE_GLOBAL;
        return newNode;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public ActionNode shareData() {
        ActionNode shareNode = new ActionNode();
        shareNode.action = getAction();
        if (this.getDesc() != null) {
            try {
                shareNode.desc = getDesc().clone();
            } catch (CloneNotSupportedException e) {
            }
        }
        shareNode.actionScope = getActionScope();
        shareNode.actionScopeType = getActionScopeType();
        shareNode.autoLaunchApp = getAutoLaunchApp();
        shareNode.actionTitle = getActionTitle();
        shareNode.priority = getPriority();
        shareNode.regs = getRegs();
        return shareNode;
    }

    /**
     * 比较tagId
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionNode that = (ActionNode) o;
        return Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId);
    }

    public boolean getAutoLaunchApp() {
        return this.autoLaunchApp;
    }

    public void setAutoLaunchApp(boolean autoLaunchApp) {
        this.autoLaunchApp = autoLaunchApp;
    }
}