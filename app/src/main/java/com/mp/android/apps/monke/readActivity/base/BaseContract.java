package com.mp.android.apps.monke.readActivity.base;



public interface BaseContract {

    interface BasePresenter<T> {

        void attachView(T view);

        void detachView();
    }

    interface BaseView {

        void showError();

        void complete();
    }
}
