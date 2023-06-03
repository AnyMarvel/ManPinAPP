package com.mp.android.apps.main.home.view;


import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.basemvplib.IView;


import java.util.List;
import java.util.Map;

public interface IMainfragmentView extends IView {
    void notifyRecyclerHomePage(List<Map<String,String>> carouselList,List<Map<String,String>> recommendInfoList);
    void notifyContentItemUpdate(int position, List<SourceListContent> sourceListContents);
}
