// IDownloadBookInterface.aidl
package com.mp.android.apps;

// Declare any non-default types here with import statements
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
interface IDownloadBookInterface {
    void addTask(in DownloadTaskBean downloadTaskBean);
}
