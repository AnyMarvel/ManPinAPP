package com.mp.android.apps.book.presenter.impl;

import android.os.Environment;

import com.mp.android.apps.basemvplib.impl.BaseActivity;
import com.mp.android.apps.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.presenter.IImportBookPresenter;
import com.mp.android.apps.book.utils.media.MediaStoreHelper;
import com.mp.android.apps.book.view.IImportBookView;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;
import com.mp.android.apps.readActivity.utils.Constant;
import com.mp.android.apps.readActivity.utils.StringUtils;
import com.mp.android.apps.utils.MD5Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImportBookPresenterImpl extends BasePresenterImpl<IImportBookView> implements IImportBookPresenter {


    public ImportBookPresenterImpl() {

    }

    @Override
    public void searchLocationBook() {
        Observable.create(new ObservableOnSubscribe<ArrayList<File>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<File>> emitter) throws Exception {
                scanFile(getSDPath(), ".txt");
                emitter.onNext(lists);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<ArrayList<File>>() {
            @Override
            public void onNext(ArrayList<File> s) {
                orderByLength(lists);
                mView.setSystemBooks(lists);
                mView.searchFinish();
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private ArrayList<File> lists = new ArrayList<File>();

    public File getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir;

    }


    public void scanFile(File rootPath, final String filterName) {

        rootPath.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (((pathname + "").toLowerCase()).endsWith(filterName)) {
                    lists.add(pathname);
                    return true;
                }
                if (pathname.isDirectory()) {//如果是目录
                    scanFile(pathname, filterName);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * 比较文件大小，递减排序
     *
     * @param fileList
     */
    private void orderByLength(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.length() - f2.length();
                if (diff > 0)
                    return -1;
                else if (diff == 0)
                    return 0;
                else
                    return 1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }
        });
    }


    @Override
    public void importBooks(List<File> books) {
        try {
            //转换成CollBook,并存储
            List<CollBookBean> collBooks = convertCollBook(books);
            BookRepository.getInstance()
                    .saveCollBooks(collBooks);
            mView.addSuccess();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void detachView() {

    }

    /**
     * 将文件转换成CollBook
     *
     * @param files:需要加载的文件列表
     * @return
     */
    private List<CollBookBean> convertCollBook(List<File> files) {
        List<CollBookBean> collBooks = new ArrayList<>(files.size());
        for (File file : files) {
            //判断文件是否存在
            if (!file.exists()) continue;

            CollBookBean collBook = new CollBookBean();
            collBook.set_id(MD5Utils.strToMd5By16(file.getAbsolutePath()));
            collBook.setTitle(file.getName().replace(".txt", ""));
            collBook.setAuthor("");
            collBook.setShortIntro("无");
            collBook.setCover(file.getAbsolutePath());
            collBook.setLocal(true);
            collBook.setUpdate(false);
            collBook.setLastChapter("开始阅读");
            collBook.setUpdated(StringUtils.dateConvert(file.lastModified(), Constant.FORMAT_BOOK_DATE));
            collBook.setLastRead(StringUtils.
                    dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
            collBooks.add(collBook);
        }
        return collBooks;
    }
}
