package cn.vove7.datamanager.parse.statusmap;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

import cn.vove7.datamanager.greendao.ActionDao;
import cn.vove7.datamanager.greendao.ActionScopeDao;
import cn.vove7.datamanager.greendao.DaoSession;
import cn.vove7.datamanager.greendao.MapNodeDao;
import cn.vove7.datamanager.greendao.ParamDao;
import cn.vove7.datamanager.greendao.RegDao;
import cn.vove7.datamanager.parse.model.Action;
import cn.vove7.datamanager.parse.model.ActionScope;
import cn.vove7.datamanager.parse.model.Param;

/**
 * 状态图节点
 * - 1 Node对应1参数
 * Created by Vove on 2018/6/17
 */
@Entity
public class MapNode {
    @Id
    private Long id;
    /**
     * 一个操作对应多种"说法"
     */

    private int nodeType = -1;

    public static final int NODE_TYPE_GLOBAL = 1;
    public static final int NODE_TYPE_IN_APP = 2;
    public static final int NODE_TYPE_IN_APP_2 = 3;//2后操作

    @ToMany(referencedJoinProperty = "nodeId")
    private List<Reg> regs;
    /**
     * 一波操作
     */
    @ToOne(joinProperty = "actionId")
    private Action action;
    private long actionId;
    /**
     * 后续节点id
     */
    @NotNull
    private String follows = "";
    /**
     * 操作参数
     */
    @ToOne(joinProperty = "paramId")
    private Param param;
    private long paramId;
    /**
     * APP作用域
     */
    @ToOne(joinProperty = "scopeId")
    private
    ActionScope actionScope;
    private long scopeId;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1299205774)
    private transient MapNodeDao myDao;
    @Generated(hash = 462335395)
    private transient Long action__resolvedKey;
    @Generated(hash = 1162281612)
    private transient Long param__resolvedKey;
    @Generated(hash = 1119678907)
    private transient Long actionScope__resolvedKey;

    private String descTitle;

    @Keep
    public MapNode(Long id, String follows, int type) {
        this.id = id;
        this.follows = follows;
        this.nodeType = type;
    }

    @Keep
    public MapNode(Long id, long actionId, String follows, long paramId,int type) {
        this.id = id;
        this.actionId = actionId;
        this.follows = follows;
        this.paramId = paramId;
        this.nodeType = type;
    }

    @Keep
    public MapNode(Long id, long actionId, String follows, long paramId, long scopeId,int type) {
        this.id = id;
        this.actionId = actionId;
        this.follows = follows;
        this.paramId = paramId;
        this.scopeId = scopeId;
        this.nodeType = type;
    }

    @Keep
    public MapNode(String descTitle,Long id, long actionId, long paramId, long scopeId,int type) {
        this.id = id;
        this.actionId = actionId;
        this.paramId = paramId;
        this.scopeId = scopeId;
        this.nodeType = type;
        this.descTitle = descTitle;
    }
    @Keep
    public MapNode(String descTitle,Long id, long actionId, long paramId, long scopeId,String f,int type) {
        this.id = id;
        this.actionId = actionId;
        this.paramId = paramId;
        this.scopeId = scopeId;
        this.follows=f;
        this.nodeType = type;
        this.descTitle = descTitle;
    }

    @Keep
    public MapNode(String descTitle,Long id, long actionId, long paramId,int type) {
        this.id = id;
        this.actionId = actionId;
        this.paramId = paramId;
        this.nodeType = type;
        this.descTitle = descTitle;
    }

    @Keep
    public MapNode(String descTitle,Long id, long actionId,int type) {
        this.id = id;
        this.actionId = actionId;
        this.nodeType = type;
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
    public MapNode() {
    }

    @Generated(hash = 1601296698)
    public MapNode(Long id, int nodeType, long actionId, @NotNull String follows, long paramId, long scopeId,
            String descTitle) {
        this.id = id;
        this.nodeType = nodeType;
        this.actionId = actionId;
        this.follows = follows;
        this.paramId = paramId;
        this.scopeId = scopeId;
        this.descTitle = descTitle;
    }

    public long getParamId() {
        return paramId;
    }

    public void setParamId(long paramId) {
        this.paramId = paramId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFollows() {
        return this.follows;
    }

    public void setFollows(String follows) {
        this.follows = follows;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 2001125006)
    public Action getAction() {
        long __key = this.actionId;
        if (action__resolvedKey == null || !action__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ActionDao targetDao = daoSession.getActionDao();
            Action actionNew = targetDao.load(__key);
            synchronized (this) {
                action = actionNew;
                action__resolvedKey = __key;
            }
        }
        return action;
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
    public Param getParam() {
        if (this.param != null) {
            return this.param;
        }
        long __key = this.paramId;
        if (param__resolvedKey == null || !param__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ParamDao targetDao = daoSession.getParamDao();
            Param paramNew = targetDao.load(__key);
            synchronized (this) {
                param = paramNew;
                param__resolvedKey = __key;
            }
        }
        return param;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1554982243)
    public void setParam(@NotNull Param param) {
        if (param == null) {
            throw new DaoException(
                    "To-one property 'paramId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.param = param;
            paramId = param.getId();
            param__resolvedKey = paramId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 2119521526)
    public ActionScope getActionScope() {
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
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1954309052)
    public List<Reg> getRegs() {
        if (regs == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RegDao targetDao = daoSession.getRegDao();
            List<Reg> regsNew = targetDao._queryMapNode_Regs(id);
            synchronized (this) {
                if (regs == null) {
                    regs = regsNew;
                }
            }
        }
        return regs;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1320822772)
    public synchronized void resetRegs() {
        regs = null;
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
    @Generated(hash = 979527667)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMapNodeDao() : null;
    }

    public int getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String toString() {
        return descTitle;
    }
}