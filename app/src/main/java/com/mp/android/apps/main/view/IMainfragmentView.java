package com.mp.android.apps.main.view;


import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.monke.basemvplib.IView;


import java.util.List;

public interface IMainfragmentView extends IView {
    void notifyRecyclerView(List<HomeDesignBean> list, List<String> carouselImages, List<SourceListContent> listContents);

    void notifyContentItemUpdate(int position, List<SourceListContent> sourceListContents);
}
