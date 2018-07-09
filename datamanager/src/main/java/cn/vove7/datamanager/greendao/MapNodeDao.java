package cn.vove7.datamanager.greendao;

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

import cn.vove7.datamanager.parse.model.Action;
import cn.vove7.datamanager.parse.model.ActionScope;
import cn.vove7.datamanager.parse.model.Param;

import cn.vove7.datamanager.parse.statusmap.MapNode;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MAP_NODE".
*/
public class MapNodeDao extends AbstractDao<MapNode, Long> {

    public static final String TABLENAME = "MAP_NODE";

    /**
     * Properties of entity MapNode.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property NodeType = new Property(1, int.class, "nodeType", false, "NODE_TYPE");
        public final static Property ActionId = new Property(2, long.class, "actionId", false, "ACTION_ID");
        public final static Property Follows = new Property(3, String.class, "follows", false, "FOLLOWS");
        public final static Property ParamId = new Property(4, long.class, "paramId", false, "PARAM_ID");
        public final static Property ScopeId = new Property(5, long.class, "scopeId", false, "SCOPE_ID");
    }

    private DaoSession daoSession;


    public MapNodeDao(DaoConfig config) {
        super(config);
    }
    
    public MapNodeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MAP_NODE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"NODE_TYPE\" INTEGER NOT NULL ," + // 1: nodeType
                "\"ACTION_ID\" INTEGER NOT NULL ," + // 2: actionId
                "\"FOLLOWS\" TEXT NOT NULL ," + // 3: follows
                "\"PARAM_ID\" INTEGER NOT NULL ," + // 4: paramId
                "\"SCOPE_ID\" INTEGER NOT NULL );"); // 5: scopeId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MAP_NODE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, MapNode entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getNodeType());
        stmt.bindLong(3, entity.getActionId());
        stmt.bindString(4, entity.getFollows());
        stmt.bindLong(5, entity.getParamId());
        stmt.bindLong(6, entity.getScopeId());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, MapNode entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getNodeType());
        stmt.bindLong(3, entity.getActionId());
        stmt.bindString(4, entity.getFollows());
        stmt.bindLong(5, entity.getParamId());
        stmt.bindLong(6, entity.getScopeId());
    }

    @Override
    protected final void attachEntity(MapNode entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public MapNode readEntity(Cursor cursor, int offset) {
        MapNode entity = new MapNode( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // nodeType
            cursor.getLong(offset + 2), // actionId
            cursor.getString(offset + 3), // follows
            cursor.getLong(offset + 4), // paramId
            cursor.getLong(offset + 5) // scopeId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, MapNode entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setNodeType(cursor.getInt(offset + 1));
        entity.setActionId(cursor.getLong(offset + 2));
        entity.setFollows(cursor.getString(offset + 3));
        entity.setParamId(cursor.getLong(offset + 4));
        entity.setScopeId(cursor.getLong(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(MapNode entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(MapNode entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(MapNode entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getActionDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T1", daoSession.getParamDao().getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T2", daoSession.getActionScopeDao().getAllColumns());
            builder.append(" FROM MAP_NODE T");
            builder.append(" LEFT JOIN ACTION T0 ON T.\"ACTION_ID\"=T0.\"_id\"");
            builder.append(" LEFT JOIN PARAM T1 ON T.\"PARAM_ID\"=T1.\"_id\"");
            builder.append(" LEFT JOIN ACTION_SCOPE T2 ON T.\"SCOPE_ID\"=T2.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected MapNode loadCurrentDeep(Cursor cursor, boolean lock) {
        MapNode entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Action action = loadCurrentOther(daoSession.getActionDao(), cursor, offset);
         if(action != null) {
            entity.setAction(action);
        }
        offset += daoSession.getActionDao().getAllColumns().length;

        Param param = loadCurrentOther(daoSession.getParamDao(), cursor, offset);
         if(param != null) {
            entity.setParam(param);
        }
        offset += daoSession.getParamDao().getAllColumns().length;

        ActionScope actionScope = loadCurrentOther(daoSession.getActionScopeDao(), cursor, offset);
         if(actionScope != null) {
            entity.setActionScope(actionScope);
        }

        return entity;    
    }

    public MapNode loadDeep(Long key) {
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
    public List<MapNode> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<MapNode> list = new ArrayList<MapNode>(count);
        
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
    
    protected List<MapNode> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<MapNode> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
