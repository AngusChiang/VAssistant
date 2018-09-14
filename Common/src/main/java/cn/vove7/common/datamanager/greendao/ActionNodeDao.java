package cn.vove7.common.datamanager.greendao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import cn.vove7.common.datamanager.parse.model.Action;
import cn.vove7.common.datamanager.parse.model.ActionScope;
import cn.vove7.common.datamanager.parse.statusmap.ActionNode;

import cn.vove7.common.datamanager.parse.statusmap.ActionNode;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACTION_NODE".
*/
public class ActionNodeDao extends AbstractDao<ActionNode, Long> {

    public static final String TABLENAME = "ACTION_NODE";

    /**
     * Properties of entity ActionNode.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ActionScopeType = new Property(1, int.class, "actionScopeType", false, "ACTION_SCOPE_TYPE");
        public final static Property ActionId = new Property(2, long.class, "actionId", false, "ACTION_ID");
        public final static Property ParentId = new Property(3, Long.class, "parentId", false, "PARENT_ID");
        public final static Property ScopeId = new Property(4, long.class, "scopeId", false, "SCOPE_ID");
        public final static Property DescTitle = new Property(5, String.class, "descTitle", false, "DESC_TITLE");
        public final static Property TagId = new Property(6, String.class, "tagId", false, "TAG_ID");
        public final static Property PublishUserId = new Property(7, Long.class, "publishUserId", false, "PUBLISH_USER_ID");
        public final static Property Priority = new Property(8, int.class, "priority", false, "PRIORITY");
        public final static Property From = new Property(9, String.class, "from", false, "FROM");
    }

    private DaoSession daoSession;

    private Query<ActionNode> actionNode_FollowsQuery;

    public ActionNodeDao(DaoConfig config) {
        super(config);
    }
    
    public ActionNodeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACTION_NODE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"ACTION_SCOPE_TYPE\" INTEGER NOT NULL ," + // 1: actionScopeType
                "\"ACTION_ID\" INTEGER NOT NULL ," + // 2: actionId
                "\"PARENT_ID\" INTEGER," + // 3: parentId
                "\"SCOPE_ID\" INTEGER NOT NULL ," + // 4: scopeId
                "\"DESC_TITLE\" TEXT," + // 5: descTitle
                "\"TAG_ID\" TEXT," + // 6: tagId
                "\"PUBLISH_USER_ID\" INTEGER," + // 7: publishUserId
                "\"PRIORITY\" INTEGER NOT NULL ," + // 8: priority
                "\"FROM\" TEXT);"); // 9: from
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACTION_NODE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ActionNode entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getActionScopeType());
        stmt.bindLong(3, entity.getActionId());
 
        Long parentId = entity.getParentId();
        if (parentId != null) {
            stmt.bindLong(4, parentId);
        }
        stmt.bindLong(5, entity.getScopeId());
 
        String descTitle = entity.getDescTitle();
        if (descTitle != null) {
            stmt.bindString(6, descTitle);
        }
 
        String tagId = entity.getTagId();
        if (tagId != null) {
            stmt.bindString(7, tagId);
        }
 
        Long publishUserId = entity.getPublishUserId();
        if (publishUserId != null) {
            stmt.bindLong(8, publishUserId);
        }
        stmt.bindLong(9, entity.getPriority());
 
        String from = entity.getFrom();
        if (from != null) {
            stmt.bindString(10, from);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ActionNode entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getActionScopeType());
        stmt.bindLong(3, entity.getActionId());
 
        Long parentId = entity.getParentId();
        if (parentId != null) {
            stmt.bindLong(4, parentId);
        }
        stmt.bindLong(5, entity.getScopeId());
 
        String descTitle = entity.getDescTitle();
        if (descTitle != null) {
            stmt.bindString(6, descTitle);
        }
 
        String tagId = entity.getTagId();
        if (tagId != null) {
            stmt.bindString(7, tagId);
        }
 
        Long publishUserId = entity.getPublishUserId();
        if (publishUserId != null) {
            stmt.bindLong(8, publishUserId);
        }
        stmt.bindLong(9, entity.getPriority());
 
        String from = entity.getFrom();
        if (from != null) {
            stmt.bindString(10, from);
        }
    }

    @Override
    protected final void attachEntity(ActionNode entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ActionNode readEntity(Cursor cursor, int offset) {
        ActionNode entity = new ActionNode( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // actionScopeType
            cursor.getLong(offset + 2), // actionId
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // parentId
            cursor.getLong(offset + 4), // scopeId
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // descTitle
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // tagId
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // publishUserId
            cursor.getInt(offset + 8), // priority
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // from
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ActionNode entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setActionScopeType(cursor.getInt(offset + 1));
        entity.setActionId(cursor.getLong(offset + 2));
        entity.setParentId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setScopeId(cursor.getLong(offset + 4));
        entity.setDescTitle(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setTagId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setPublishUserId(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setPriority(cursor.getInt(offset + 8));
        entity.setFrom(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ActionNode entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ActionNode entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ActionNode entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "follows" to-many relationship of ActionNode. */
    public List<ActionNode> _queryActionNode_Follows(Long parentId) {
        synchronized (this) {
            if (actionNode_FollowsQuery == null) {
                QueryBuilder<ActionNode> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ParentId.eq(null));
                actionNode_FollowsQuery = queryBuilder.build();
            }
        }
        Query<ActionNode> query = actionNode_FollowsQuery.forCurrentThread();
        query.setParameter(0, parentId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getActionDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getActionNodeDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T2", daoSession.getActionScopeDao().getAllColumns());
            builder.append(" FROM ACTION_NODE T");
            builder.append(" LEFT JOIN ACTION T0 ON T.\"ACTION_ID\"=T0.\"_id\"");
            builder.append(" LEFT JOIN ACTION_NODE T1 ON T.\"PARENT_ID\"=T1.\"_id\"");
            builder.append(" LEFT JOIN ACTION_SCOPE T2 ON T.\"SCOPE_ID\"=T2.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected ActionNode loadCurrentDeep(Cursor cursor, boolean lock) {
        ActionNode entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Action action = loadCurrentOther(daoSession.getActionDao(), cursor, offset);
         if(action != null) {
            entity.setAction(action);
        }
        offset += daoSession.getActionDao().getAllColumns().length;

        ActionNode parent = loadCurrentOther(daoSession.getActionNodeDao(), cursor, offset);
        entity.setParent(parent);
        offset += daoSession.getActionNodeDao().getAllColumns().length;

        ActionScope actionScope = loadCurrentOther(daoSession.getActionScopeDao(), cursor, offset);
         if(actionScope != null) {
            entity.setActionScope(actionScope);
        }

        return entity;    
    }

    public ActionNode loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<ActionNode> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<ActionNode> list = new ArrayList<ActionNode>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<ActionNode> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<ActionNode> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
