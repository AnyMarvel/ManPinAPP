package com.mp.android.apps.readActivity.local;

import android.util.Log;

import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.dao.CollBookBeanDao;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.BookRecordBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.book.dao.BookRecordBeanDao;
import com.mp.android.apps.book.dao.DaoSession;
import com.mp.android.apps.readActivity.utils.BookManager;
import com.mp.android.apps.readActivity.utils.Constant;
import com.mp.android.apps.readActivity.utils.FileUtils;
import com.mp.android.apps.readActivity.utils.IOUtils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * 存储关于书籍内容的信息(CollBook(收藏书籍),BookChapter(书籍列表),ChapterInfo(书籍章节),BookRecord(记录))
 */

public class BookRepository {
    private static final String TAG = "CollBookManager";
    private static volatile BookRepository sInstance;
    private DaoSession mSession;
    private CollBookBeanDao mCollBookDao;

    private BookRepository() {
        mSession = DaoDbHelper.getInstance()
                .getSession();
        mCollBookDao = mSession.getCollBookBeanDao();
    }

    public static BookRepository getInstance() {
        if (sInstance == null) {
            synchronized (BookRepository.class) {
                if (sInstance == null) {
                    sInstance = new BookRepository();
                }
            }
        }
        return sInstance;
    }

    //存储已收藏书籍
    public void saveCollBookWithAsync(CollBookBean bean) {
        //启动异步存储
        mSession.startAsyncSession()
                .runInTx(
                        () -> {
                            if (bean.getBookChapters() != null) {
                                // 存储BookChapterBean
                                mSession.getBookChapterBeanDao()
                                        .insertOrReplaceInTx(bean.getBookChapters());
                            }
                            //存储CollBook (确保先后顺序，否则出错)
                            try {
                                mCollBookDao.insertOrReplace(bean);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                );
    }

    /**
     * 异步存储。
     * 同时保存BookChapter
     *
     * @param beans
     */
    public void saveCollBooksWithAsync(List<CollBookBean> beans) {
        mSession.startAsyncSession()
                .runInTx(
                        () -> {
                            for (CollBookBean bean : beans) {
                                if (bean.getBookChapters() != null) {
                                    //存储BookChapterBean(需要修改，如果存在id相同的则无视)
                                    mSession.getBookChapterBeanDao()
                                            .insertOrReplaceInTx(bean.getBookChapters());
                                }
                            }
                            //存储CollBook (确保先后顺序，否则出错)
                            mCollBookDao.insertOrReplaceInTx(beans);
                        }
                );
    }

    public void saveCollBook(CollBookBean bean) {
        mCollBookDao.insertOrReplace(bean);
    }

    public void saveCollBooks(List<CollBookBean> beans) {
        mCollBookDao.insertOrReplaceInTx(beans);
    }

    /**
     * 异步存储BookChapter
     *
     * @param beans
     */
    public void saveBookChaptersWithAsync(List<BookChapterBean> beans) {
        mSession.startAsyncSession()
                .runInTx(
                        () -> {
                            //存储BookChapterBean
                            mSession.getBookChapterBeanDao()
                                    .insertOrReplaceInTx(beans);
                            Log.d(TAG, "saveBookChaptersWithAsync: " + "进行存储");
                        }
                );
    }

    /**
     * 存储章节
     *
     * @param folderName
     * @param fileName
     * @param content
     */
    public void saveChapterInfo(String folderName, String fileName, String content) {
        File file = BookManager.getBookFile(folderName, fileName);
        //获取流并存储
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            IOUtils.close(writer);
        }
    }

    public void saveBookRecord(BookRecordBean bean) {
        mSession.getBookRecordBeanDao()
                .insertOrReplace(bean);
    }

    /*****************************get************************************************/
    public CollBookBean getCollBook(String bookId) {
        CollBookBean bean = mCollBookDao.queryBuilder()
                .where(CollBookBeanDao.Properties._id.eq(bookId))
                .unique();
        return bean;
    }


    public List<CollBookBean> getCollBooks() {
        return mCollBookDao
                .queryBuilder()
                .orderDesc(CollBookBeanDao.Properties.LastRead)
                .list();
    }


    //获取书籍列表
    public Single<List<BookChapterBean>> getBookChaptersInRx(String bookId) {
        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
            @Override
            public void subscribe(SingleEmitter<List<BookChapterBean>> e) throws Exception {
                List<BookChapterBean> beans = mSession
                        .getBookChapterBeanDao()
                        .queryBuilder()
                        .where(BookChapterBeanDao.Properties.BookId.eq(bookId))
                        .list();
                e.onSuccess(beans);
            }
        });
    }

    //获取阅读记录
    public BookRecordBean getBookRecord(String bookId) {
        return mSession.getBookRecordBeanDao()
                .queryBuilder()
                .where(BookRecordBeanDao.Properties.BookId.eq(bookId))
                .unique();
    }

    //TODO:需要进行获取编码并转换的问题
    public ChapterInfoBean getChapterInfoBean(String folderName, String fileName) {
        File file = new File(Constant.BOOK_CACHE_PATH + folderName
                + File.separator + fileName + FileUtils.SUFFIX_NB);
        if (!file.exists()) return null;
        Reader reader = null;
        String str = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(reader);
        }

        ChapterInfoBean bean = new ChapterInfoBean();
        bean.setTitle(fileName);
        bean.setBody(sb.toString());
        return bean;
    }

    /************************************************************/
    /**
     * 基于 CollBookBean 获取当前阅读章节进度
     *
     * @param book
     * @return
     */
    public String getRecordBookChapterTitle(CollBookBean book) {
        BookRecordBean bookRecordBeans = mSession.getBookRecordBeanDao().queryBuilder().where(BookRecordBeanDao.Properties.BookId.eq(book.get_id())).unique();
        if (bookRecordBeans != null) {
            BookChapterBean currentBookChapterBean = mSession.getBookChapterBeanDao().queryBuilder()
                    .where(BookChapterBeanDao.Properties.BookId.eq(book.get_id()), BookChapterBeanDao.Properties.Position.eq(bookRecordBeans.getChapter())).unique();
            if (currentBookChapterBean != null) {
                return currentBookChapterBean.getTitle();
            }

        }
        return "无章节";

    }

    /**
     * 异步删除书架收藏记录
     *
     * @param bean
     * @return
     */
    public Single<Void> deleteCollBookInRx(CollBookBean bean) {
        return Single.create(new SingleOnSubscribe<Void>() {
            @Override
            public void subscribe(SingleEmitter<Void> e) throws Exception {
                try {
                    //查看文本中是否存在删除的数据
                    deleteBook(bean.get_id());
                    //删除目录
                    deleteBookChapter(bean.get_id());

                    e.onSuccess(new Void());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    /**
     * 同步删除数据库记录
     *
     * @param bean
     */
    public void deleteCollBookSync(CollBookBean bean) {
        //删除书籍
        mCollBookDao.delete(bean);
        //删除目录
        deleteBookChapter(bean.get_id());
        //删除阅读记录
        deleteBookRecord(bean.get_id());
    }


    //这个需要用rx，进行删除
    public void deleteBookChapter(String bookId) {
        mSession.getBookChapterBeanDao()
                .queryBuilder()
                .where(BookChapterBeanDao.Properties.BookId.eq(bookId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }


    //删除书籍
    public void deleteBook(String bookId) {
        FileUtils.deleteFile(Constant.BOOK_CACHE_PATH + bookId);
    }

    public void deleteBookRecord(String id) {
        mSession.getBookRecordBeanDao()
                .queryBuilder()
                .where(BookRecordBeanDao.Properties.BookId.eq(id))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public DaoSession getSession() {
        return mSession;
    }

    public void saveDownloadTask(DownloadTaskBean bean) {
//        saveBookChaptersWithAsync(bean.getBookChapters());
        mSession.getDownloadTaskBeanDao()
                .insertOrReplace(bean);
    }


}
