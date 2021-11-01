package com.mp.android.apps.main.bookR.view.popupwindow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.R;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.bean.BaseResponseBean;
import com.mp.android.apps.book.model.ObtainBookInfoUtils;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.book.presenter.impl.BookShelUtils;
import com.mp.android.apps.book.view.impl.BookSourceActivity;
import com.mp.android.apps.book.view.popupwindow.UnifiedCheckDialog;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.main.bookR.view.popupwindow.bean.UserBookCorrespondenceBean;
import com.mp.android.apps.main.bookR.view.popupwindow.utils.BCSettingModel;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.readActivity.bean.BookRecordBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;
import com.mp.android.apps.readActivity.local.DaoDbHelper;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * 图书的设置界面
 */

public class BCSettingPopupwindow extends PopupWindow {
    private View rootView;
    private Button backBooks;
    private Button recoveryBooks;
    private Button setBookSource;
    private Context context;
    private  BCSettingModel bcSettingModel;

    private UnifiedCheckDialog backUnifiedCheckDialog;
    private UnifiedCheckDialog reconverUnifiedCheckDialog;


    public BCSettingPopupwindow(Context context) {
        super(context);
        this.context=context;
        rootView= LayoutInflater.from(context).inflate(R.layout.view_pop_window_bc_setting,null);
        this.setContentView(rootView);
        bcSettingModel=new BCSettingModel();
        setFocusable(true);
        setTouchable(true);
        if (rootView!=null){
            initView();
        }
    }
    private void initView(){
            backBooks=rootView.findViewById(R.id.manpin_back_book_collection);
            recoveryBooks=rootView.findViewById(R.id.manpin_recovery_book_collection);
            setBookSource=rootView.findViewById(R.id.manpin_setting_book_source);
        if (backUnifiedCheckDialog==null){
            backUnifiedCheckDialog=new UnifiedCheckDialog(context,new UnifiedCheckDialog.CheckDialogListener() {
                @Override
                public String getContent() {
                    return "备份书架将覆盖线上数据，请谨慎操作";
                }

                @Override
                public void confirmDialog() {
                    handleBackBooks();
                    backUnifiedCheckDialog.dismiss();
                }
            });
        }
        if (reconverUnifiedCheckDialog ==null){
            reconverUnifiedCheckDialog=new UnifiedCheckDialog(context,new UnifiedCheckDialog.CheckDialogListener() {
                @Override
                public String getContent() {
                    return "恢复书架将会覆盖本地数据，请谨慎操作";
                }

                @Override
                public void confirmDialog() {
                    handleRecoveryBooks();
                    reconverUnifiedCheckDialog.dismiss();
                }
            });
        }


        //备份图书
            backBooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LoginManager.getInstance().checkLoginInfo()){

                        backUnifiedCheckDialog.show();
                        BCSettingPopupwindow.this.dismiss();
                    }else {
                        Toast.makeText(context,"当前未登陆,请到先登陆",Toast.LENGTH_LONG).show();
                    }

                }
            });
            //还原图书
            recoveryBooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LoginManager.getInstance().checkLoginInfo()){
                        reconverUnifiedCheckDialog.show();
                        BCSettingPopupwindow.this.dismiss();
                    }else {
                        Toast.makeText(context,"当前未登陆,请到先登陆",Toast.LENGTH_LONG).show();
                    }
                }
            });

            //设置书源
            setBookSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BCSettingPopupwindow.this.dismiss();
                    Intent intent2 = new Intent(context, BookSourceActivity.class);
                    context.startActivity(intent2);
                }
            });

    }

    /**
     * 处理的还原逻辑
     */
    private void handleRecoveryBooks(){

        List<CollBookBean> collBookBeanList = DaoDbHelper.getInstance().getSession().getCollBookBeanDao().queryBuilder().list();
        List<String> localCollBooks=new ArrayList<>();

        for (CollBookBean collBookBean:collBookBeanList) {
            localCollBooks.add(collBookBean.get_id());
        }

        bcSettingModel.backUserBookCollections(LoginManager.getInstance().getLoginInfo().getUniqueID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                if (!TextUtils.isEmpty(s)){
                    JSONObject jsonObject=JSON.parseObject(s);
                    if (jsonObject!=null){
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        if (data!=null){
                        UserBookCorrespondenceBean userBookCorrespondence= JSON.parseObject(data.toJSONString(),UserBookCorrespondenceBean.class);
                            if (userBookCorrespondence != null) {
                                for (SourceListContent sourceListContent : userBookCorrespondence.getBookList()) {
                                    if (!TextUtils.isEmpty(sourceListContent.getNoteUrl()) && !localCollBooks.contains(sourceListContent.getNoteUrl())) {
                                        //添加进书架
                                        CollBookBean collBookBean = new CollBookBean();
                                        collBookBean.set_id(sourceListContent.getNoteUrl());
                                        collBookBean.setTitle(sourceListContent.getName());
                                        Uri uri=Uri.parse(sourceListContent.getNoteUrl());
                                        String bookTag=uri.getScheme()+"://"+uri.getHost();
                                        collBookBean.setBookTag(bookTag);
                                        WebBookModelControl.getInstance().getBookInfo(collBookBean)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io()).subscribe(new SimpleObserver<CollBookBean>() {
                                            @Override
                                            public void onNext(CollBookBean collBookBean) {
                                                BookShelUtils.getInstance().addToBookShelfUtils(collBookBean);
                                                HashMap<String, Integer> booksRecord=userBookCorrespondence.getUserBookRelay();
                                                if (booksRecord!=null && booksRecord.size()>0){
                                                    for (HashMap.Entry<String, Integer> entry:booksRecord.entrySet()) {
                                                        BookRecordBean bookRecordBean=new BookRecordBean();
                                                        bookRecordBean.setBookId(entry.getKey());
                                                        bookRecordBean.setChapter(entry.getValue());
                                                        BookRepository.getInstance().saveBookRecord(bookRecordBean);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(MyApplication.getInstance(), collBookBean.getTitle() + "放入书架失败!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            }
                            //刷新书架列表

                        }

                    }

                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context,"恢复失败，请稍后重试或联系小编",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 处理备份逻辑
     */
    private void handleBackBooks(){
        List<CollBookBean> temp = DaoDbHelper.getInstance().getSession().getCollBookBeanDao().queryBuilder().list();
        if (temp!=null && temp.size()>0){
            List<SourceListContent> sourceListContents=new ArrayList<>();
            for (CollBookBean collBookBean:temp) {
                sourceListContents.add(ObtainBookInfoUtils.getInstance().translateBookInfo(collBookBean));
            }
            List<BookRecordBean> tempRecords = DaoDbHelper.getInstance().getSession().getBookRecordBeanDao().queryBuilder().list();
            if (sourceListContents.size()>0){
                //拼装客户端数据，用于发送到server
                UserBookCorrespondenceBean userBookCorrespondenceBean=new UserBookCorrespondenceBean();
                userBookCorrespondenceBean.setBookList(sourceListContents);

                //设置阅读记录
                HashMap<String,Integer> bookRelay=new HashMap<>();
                if (tempRecords!=null && tempRecords.size()>0){
                    for (BookRecordBean bookRecordBean:tempRecords) {
                        bookRelay.put(bookRecordBean.getBookId(),bookRecordBean.getChapter());
                    }

                }
                userBookCorrespondenceBean.setUserBookRelay(bookRelay);
                userBookCorrespondenceBean.setUniqueID(LoginManager.getInstance().getLoginInfo().getUniqueID());
                bcSettingModel.userBookCorrespondence(userBookCorrespondenceBean)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io()).subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        if (!TextUtils.isEmpty(s)){
                            BaseResponseBean baseResponseBean= JSON.parseObject(s,BaseResponseBean.class);
                            if (baseResponseBean!=null && "success".equals(baseResponseBean.getMsg())){
                                String result="备份成功";
                                if (baseResponseBean.getTime()!=0){
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    result=result+":"+sdf.format(baseResponseBean.getTime());
                                }
                                Toast.makeText(context,result,Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context,"备份失败，请稍后重试或联系小编",Toast.LENGTH_LONG).show();
                    }
                });

            }
        }else {
            Toast.makeText(context,"书架为空",Toast.LENGTH_LONG).show();
        }

    }


}
