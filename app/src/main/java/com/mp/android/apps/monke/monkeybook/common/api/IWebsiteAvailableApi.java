package com.mp.android.apps.monke.monkeybook.common.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
public interface IWebsiteAvailableApi {
    @GET("/app/availableWebsit")
    Observable<String> getWebsite();

}
