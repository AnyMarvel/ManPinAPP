package com.mp.android.apps.book.presenter.impl;

import android.os.Environment;

import com.mp.android.apps.base.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.presenter.IImportBookPresenter;
import com.mp.android.apps.book.view.IImportBookView;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.local.BookRepository;
import com.mp.android.apps.read.utils.Constant;
import com.mp.android.apps.read.utils.StringUtils;
import com.mp.android.apps.utils.MD5Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImportBookPresenterImpl extends BasePresenterImpl<IImportBookView> implements IImportBookPresenter {


    public ImportBookPresenterImpl() {

    }

    /** 扫描时忽略的目录名（大文件/无书籍） */
    private static final String[] SKIP_DIRS = {"Android"};
    /** 最大递归深度，防止超深目录导致的耗时与栈溢出 */
    private static final int MAX_SCAN_DEPTH = 12;

    private Disposable scanDisposable;

    @Override
    public void searchLocationBook() {
        Observable.create(new ObservableOnSubscribe<ArrayList<File>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<File>> emitter) throws Exception {
                lists.clear();
                File root = getSDPath();
                if (root == null) {
                    throw new IllegalStateException("存储不可用");
                }
                scanFile(root, ".txt", 0);
                emitter.onNext(lists);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<ArrayList<File>>() {
            @Override
            public void onSubscribe(Disposable d) {
                scanDisposable = d;
            }

            @Override
            public void onNext(ArrayList<File> s) {
                if (mView == null) return;
                orderByLength(lists);
                mView.setSystemBooks(lists);
                mView.searchFinish();
            }

            @Override
            public void onError(Throwable e) {
                android.util.Log.e("ImportBookPresenter", "scan failed", e);
                if (mView == null) return;
                mView.searchError("扫描失败，请重试");
            }
        });
    }

    private ArrayList<File> lists = new ArrayList<File>();

    public File getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        return sdCardExist ? Environment.getExternalStorageDirectory() : null;
    }

    /**
     * 递归扫描目录下指定后缀的文件。
     * 跳过隐藏目录与 {@link #SKIP_DIRS} 中的重目录，限制最大深度。
     */
    private void scanFile(File dir, String filterName, int depth) {
        if (dir == null || depth > MAX_SCAN_DEPTH) return;
        File[] files = dir.listFiles();
        if (files == null) return; //无读权限或非目录
        for (File file : files) {
            if (file == null) continue;
            String name = file.getName();
            if (name.startsWith(".")) continue; //跳过隐藏文件/目录
            if (file.isDirectory()) {
                if (isSkipDir(name)) continue;
                scanFile(file, filterName, depth + 1);
            } else if (name.toLowerCase().endsWith(filterName) && file.length() > 0) {
                lists.add(file);
            }
        }
    }

    private boolean isSkipDir(String name) {
        for (String skip : SKIP_DIRS) {
            if (skip.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /**
     * 比较文件大小，递减排序
     *
     * @param fileList
     */
    private void orderByLength(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.length(), f1.length()); //按大小递减
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
            if (mView != null) mView.addSuccess();
        }catch (Exception e){
            android.util.Log.e("ImportBookPresenter", "import failed", e);
            if (mView != null) mView.addError();
        }

    }

    @Override
    public void detachView() {
        if (scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
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
