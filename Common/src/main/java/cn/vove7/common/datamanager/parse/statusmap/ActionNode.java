package cn.vove7.common.datamanager.parse.statusmap;

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
import java.util.ArrayList;
import java.util.List;

import cn.vove7.common.datamanager.greendao.ActionDao;
import cn.vove7.common.datamanager.greendao.ActionNodeDao;
import cn.vove7.common.datamanager.greendao.ActionScopeDao;
import cn.vove7.common.datamanager.greendao.DaoSession;
import cn.vove7.common.datamanager.greendao.RegDao;
import cn.vove7.common.datamanager.parse.DataFrom;
import cn.vove7.common.datamanager.parse.model.Action;
import cn.vove7.common.datamanager.parse.model.ActionParam;
import cn.vove7.common.datamanager.parse.model.ActionScope;

/**
 * 状态图节点
 * - 1 Node对应1参数
 * Created by Vove on 2018/6/17
 */
@Entity
public class ActionNode implements Serializable, DataFrom {
    public static final long serialVersionUID = 1210203;
    @Id
    private Long id;
    /**
     * 一个操作对应多种"说法"
     */
    //指令作用范围
    private int actionScopeType = -1;

    public static final int NODE_SCOPE_GLOBAL = 0x1;
    public static final int NODE_SCOPE_GLOBAL_2 = 0x4;//follows
    public static final int NODE_SCOPE_IN_APP = 0x2;
    public static final int NODE_SCOPE_IN_APP_2 = 0x3;//2后操作

    public static boolean belongInApp(int type) {
        return type == NODE_SCOPE_IN_APP || type == NODE_SCOPE_IN_APP_2;
    }

    public static boolean belongGlobal(int type) {
        return type == NODE_SCOPE_GLOBAL || type == NODE_SCOPE_GLOBAL_2;
    }

    @ToMany(referencedJoinProperty = "nodeId")//一对多 reg 表 nodeId为外键
    private List<Reg> regs;

    /**
     * 一波操作
     */
    @ToOne(joinProperty = "actionId")//一对一,actionId外键
    private Action action;
    private long actionId;

    /**
     * 后续节点id format: nodeId1,nodeId2,..
     */
    //@NotNull
    //private String follows = "";
    //
    @ToMany(referencedJoinProperty = "parentId")
    private List<ActionNode> follows;
    private Long parentId;

    @ToOne(joinProperty = "parentId")
    private ActionNode parent;

    /**
     * 操作参数
     */
    //@ToOne(joinProperty = "paramId")
    @Transient
    private ActionParam param;
    //private long paramId;

    /**
     * APP作用域
     */
    @ToOne(joinProperty = "scopeId")
    private
    ActionScope actionScope;
    private long scopeId;
    private String descTitle;

    private String tagId;//与服务器数据匹配标志
    private int versionCode = 0;

    private Long publishUserId;//发布者

    private int priority;//优先级 bigger优先; 相对于action chain :(全局命令下: 返回主页>返回) (同一界面下)

    private String from = null;

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
    @Generated(hash = 1293412156)
    private transient Long parent__resolvedKey;

    public Long getParentId() {
        return parentId;
    }

    public void setRegs(List<Reg> regs) {
        this.regs = regs;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public ActionNode(String descTitle, Long id, long actionId, long scopeId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.actionScopeType = type;
        this.descTitle = descTitle;
    }

    public ActionNode(String descTitle, Action action) {
        this.descTitle = descTitle;
        this.action = action;
    }

    public ActionNode(String descTitle, Long id, int type) {
        this.id = id;
        this.actionScopeType = type;
        this.descTitle = descTitle;
    }


    @Keep
    public ActionNode(String descTitle, Long id, long actionId, int type, long parentId) {
        this.id = id;
        this.actionId = actionId;
        this.actionScopeType = type;
        this.descTitle = descTitle;
        this.parentId = parentId;
    }

    @Keep
    public ActionNode(String descTitle, Long id, long actionId, int type) {
        this.id = id;
        this.actionId = actionId;
        this.actionScopeType = type;
        this.descTitle = descTitle;
    }

    public String getDescTitle() {
        return descTitle;
    }

    public void setDescTitle(String descTitle) {
        this.descTitle = descTitle;
    }

    public long getActionId() {
        return actionId;
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }

    public long getScopeId() {
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
                      String descTitle, Long parentId, int priority) {
        this.id = id;
        this.actionScopeType = actionScopeType;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.descTitle = descTitle;
        this.parentId = parentId;
        this.priority = priority;
    }

    @Keep
    public ActionNode(Long id, int actionScopeType, long actionId, long scopeId,
                      String descTitle, Long parentId, String from, int priority) {
        this.id = id;
        this.actionScopeType = actionScopeType;
        this.actionId = actionId;
        this.scopeId = scopeId;
        this.descTitle = descTitle;
        this.parentId = parentId;
        this.from = from;
        this.priority = priority;
    }

    @Generated(hash = 1239043208)
    public ActionNode(Long id, int actionScopeType, long actionId, Long parentId, long scopeId,
                      String descTitle, String tagId, Long publishUserId, int priority, String from) {
        this.id = id;
        this.actionScopeType = actionScopeType;
        this.actionId = actionId;
        this.parentId = parentId;
        this.scopeId = scopeId;
        this.descTitle = descTitle;
        this.tagId = tagId;
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
        if (action != null) {
            return action;
        }
        if (actionId <= 0) {
            this.action = new Action();
            return action;
        }
        long __key = this.actionId;
        if (action__resolvedKey == null || !action__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
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
    @Generated(hash = 474548544)
    public void setAction(@NotNull Action action) {
        if (action == null) {
            throw new DaoException(
                    "To-one property 'actionId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.action = action;
            actionId = action.getId();
            action__resolvedKey = actionId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public ActionParam getParam() {
        if (this.param != null) {
            return this.param;
        }
        this.param = new ActionParam();
        return param;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Keep//(hash = 1554982243)
    public void setParam(@NotNull ActionParam param) {
        this.param = param;
    }


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1856235649)
    public void setActionScope(@NotNull ActionScope actionScope) {
        if (actionScope == null) {
            throw new DaoException(
                    "To-one property 'scopeId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.actionScope = actionScope;
            scopeId = actionScope.getId();
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
        return descTitle;
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
        long __key = this.scopeId;
        if (actionScope__resolvedKey == null || !actionScope__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 576232639)
    public List<Reg> getRegs() {
        if (regs == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep
    public List<ActionNode> getFollows() {
        if (follows == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                follows = new ArrayList<>();
                return follows;
                //throw new DaoException("Entity is detached from DAO context");
            }
            ActionNodeDao targetDao = daoSession.getActionNodeDao();
            List<ActionNode> followsNew = targetDao._queryActionNode_Follows(id);
            synchronized (this) {
                if (follows == null) {
                    follows = followsNew;
                }
            }
        }
        return follows;
    }


    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 880764975)
    public synchronized void resetFollows() {
        follows = null;
    }

    public void setFollows(List<ActionNode> follows) {
        this.follows = follows;
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
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
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

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 356998130)
    public ActionNode getParent() {
        Long __key = this.parentId;
        if (parent__resolvedKey == null || !parent__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ActionNodeDao targetDao = daoSession.getActionNodeDao();
            ActionNode parentNew = targetDao.load(__key);
            synchronized (this) {
                parent = parentNew;
                parent__resolvedKey = __key;
            }
        }
        return parent;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 55088872)
    public void setParent(ActionNode parent) {
        synchronized (this) {
            this.parent = parent;
            parentId = parent == null ? null : parent.getId();
            parent__resolvedKey = parentId;
        }
    }
}