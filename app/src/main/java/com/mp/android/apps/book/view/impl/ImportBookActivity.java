
package com.mp.android.apps.book.view.impl;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.book.base.MBaseActivity;
import com.mp.android.apps.book.presenter.IImportBookPresenter;
import com.mp.android.apps.book.presenter.impl.ImportBookPresenterImpl;
import com.mp.android.apps.book.utils.FileUtil;
import com.mp.android.apps.book.view.IImportBookView;
import com.mp.android.apps.book.view.adapter.ImportBookAdapter;
import com.mp.android.apps.book.widget.modialog.MoProgressHUD;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.util.List;

public class ImportBookActivity extends MBaseActivity<IImportBookPresenter> implements IImportBookView {
    private LinearLayout llContent;
    private ImageButton ivReturn;
    private TextView tvScan;

    private RotateLoading rlLoading;
    private TextView tvCount;

    private TextView tvAddshelf;
    private TextView scan_tv_addshelf_local;

    private RecyclerView rcvBooks;

    private ImportBookAdapter importBookAdapter;

    private Animation animIn;
    private Animation animOut;

    private MoProgressHUD moProgressHUD;

    private FrameLayout scanContentLayout;

    boolean manualSearch;
    @Override
    protected IImportBookPresenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_importbook);
    }
    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);

        importBookAdapter = new ImportBookAdapter(new ImportBookAdapter.OnCheckBookListener() {
            @Override
            public void checkBook(int count) {
                tvAddshelf.setVisibility(count == 0 ? View.INVISIBLE : View.VISIBLE);
            }
        },getContext());
    }

    @Override
    protected void bindView() {
        moProgressHUD = new MoProgressHUD(this);

        llContent = (LinearLayout) findViewById(R.id.ll_content);
        ivReturn = (ImageButton) findViewById(R.id.iv_return);
        tvScan = (TextView) findViewById(R.id.tv_scan);

        rlLoading = (RotateLoading) findViewById(R.id.rl_loading);
        tvCount = (TextView) findViewById(R.id.tv_count);

        tvAddshelf = (TextView) findViewById(R.id.scan_tv_addshelf);
        scan_tv_addshelf_local = findViewById(R.id.scan_tv_addshelf_local);

        rcvBooks = (RecyclerView) findViewById(R.id.rcv_books);
        rcvBooks.setAdapter(importBookAdapter);
        rcvBooks.setLayoutManager(new LinearLayoutManager(this));

        scanContentLayout = findViewById(R.id.scan_content_layout);


    }

    private static final int REQUEST_CODE_FILE = 985;

    private void chooseFile() {
        //如果使用系统文件选择器，可以实现文件类型过滤，三方的文件选择器不可用。腾讯，es等
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = new String[]{"text/plain"};
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_CODE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_FILE) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path;
                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        path = uri.getPath();
                    } else {
                        //4.4以后
                        path = FileUtil.getPath(this, uri);
                    }

                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);

                        importBookAdapter.addData(file);

                        searchFinish();
                    }
                }
            }
        }
    }

    @Override
    protected void bindEvent() {
        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Acp.getInstance(ImportBookActivity.this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        manualSearch=false;
                        mPresenter.searchLocationBook();
                        tvScan.setVisibility(View.INVISIBLE);
                        rlLoading.start();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(ImportBookActivity.this, "读写权限被权限被拒绝,请到设置界面允许被拒绝权限", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ImportBookActivity.super.finish();
                overridePendingTransition(0, 0);
                isExiting = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvAddshelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加书籍
                moProgressHUD.showLoading("放入书架中...");
                mPresenter.importBooks(importBookAdapter.getSelectDatas());
            }
        });
        scan_tv_addshelf_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
                manualSearch=true;
            }
        });
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    private Boolean isExiting = false;

    @Override
    public void finish() {
        if (!isExiting) {
            if (moProgressHUD.isShow()) {
                moProgressHUD.dismiss();
            }
            isExiting = true;
            llContent.startAnimation(animOut);
        }
    }

    @Override
    public void setSystemBooks(List<File> files) {
        if (!manualSearch){
            importBookAdapter.addAllDatas(files);
        }

        tvCount.setText(String.format(getString(R.string.tv_importbook_count), String.valueOf(importBookAdapter.getItemCount())));
    }

    @Override
    public void searchFinish() {
        rlLoading.stop();
        rlLoading.setVisibility(View.INVISIBLE);
        importBookAdapter.setCanCheck(true);
        tvScan.setVisibility(View.GONE);
        scanContentLayout.setVisibility(View.GONE);

    }

    @Override
    public void addSuccess() {
        moProgressHUD.dismiss();
        Toast.makeText(this, "添加书籍成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addError() {
        moProgressHUD.showInfo("放入书架失败!");
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean a = moProgressHUD.onKeyDown(keyCode, event);
        if (a)
            return a;
        return super.onKeyDown(keyCode, event);
    }
}