package com.mp.android.apps.readActivity.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.dao.BookRecordBeanDao;
import com.mp.android.apps.book.dao.CollBookBeanDao;
import com.mp.android.apps.book.dao.DaoMaster;
import com.mp.android.apps.book.dao.DownloadTaskBeanDao;
import com.mp.android.apps.book.dao.SearchHistoryBeanDao;

import org.greenrobot.greendao.database.Database;

public class UpgradeOpenHelper extends DaoMaster.DevOpenHelper {
    public UpgradeOpenHelper(Context context, String name) {
        super(context, name);
    }

    public UpgradeOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                },
                BookChapterBeanDao.class,
                BookRecordBeanDao.class,
                CollBookBeanDao.class,
                DownloadTaskBeanDao.class,
                SearchHistoryBeanDao.class
        );
    }
}
