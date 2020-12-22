package com.mp.android.apps.monke.readActivity.local;


import com.mp.android.apps.monke.monkeybook.dao.DownloadTaskBeanDao;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 由于 BookChapterBean 做了一次表的大更改，所以需要自定义更新。
 * 作用：将数据库1.0 升级到 2.0
 */

public class Update2Helper {
    private static final String TAG = "BookChapterHelper";
    private static final String CONVERSION_CLASS_NOT_FOUND_EXCEPTION = "MIGRATION HELPER - CLASS DOESN'T MATCH WITH THE CURRENT PARAMETERS";

    private static final String DIVIDER = ",";
    private static final String QUOTE = "'%s'";

    private static Update2Helper instance;

    public static Update2Helper getInstance() {
        if (instance == null) {
            instance = new Update2Helper();
        }
        return instance;
    }

    /**
     * 删除不存在的表，创建DownloadTask表内容
     *
     * @param db
     */
    public void update(Database db) {
        deleteDeprecatedTables(db);
        createOrignalTables(db, DownloadTaskBeanDao.class);
    }

    /**
     * 删除弃用 table表
     *
     * @param db
     */
    private void deleteDeprecatedTables(Database db) {
        List<String> tables = new ArrayList<>();
        tables.add("BOOK_CONTENT_BEAN");
        tables.add("BOOK_INFO_BEAN");
        tables.add("BOOK_SHELF_BEAN");
        tables.add("CHAPTER_LIST_BEAN");
        tables.add("DOWNLOAD_CHAPTER_BEAN");

        //对每个表名调用DROP TABLE
        for (String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS \"" + table + "\"";
            db.execSQL(dropQuery);
        }
    }

    /**
     * 通过反射，重新创建要更新的表
     */
    private void createOrignalTables(Database db, Class<? extends AbstractDao<?, ?>> bookChapterClass) {
        try {
            Method method = bookChapterClass.getMethod("createTable", Database.class, boolean.class);
            method.invoke(null, db, false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


}
