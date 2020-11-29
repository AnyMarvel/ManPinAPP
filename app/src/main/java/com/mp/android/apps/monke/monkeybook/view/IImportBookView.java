
package com.mp.android.apps.monke.monkeybook.view;

import com.mp.android.apps.monke.basemvplib.IView;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public interface IImportBookView extends IView {

    /**
     * 设置本地书籍
     */
    void setSystemBooks(List<File> files);

    /**
     * 书籍搜索完成
     */
    void searchFinish();

    /**
     * 添加成功
     */
    void addSuccess();

    /**
     * 添加失败
     */
    void addError();
}