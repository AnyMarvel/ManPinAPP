
package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;

public interface IGxwztvBookModel extends IStationBookModel {

    Observable<List<SearchBookBean>> getKindBook(String url, int page);

}
