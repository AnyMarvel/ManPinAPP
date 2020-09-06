package com.mp.android.apps.livevblank;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mp.android.apps.BuildConfig;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.livevblank.bean.ImageDesBean;
import com.mp.android.apps.livevblank.util.FileUtils;
import com.mp.android.apps.livevblank.view.EditCardTextView;
import com.mp.android.apps.livevblank.view.SelectCustomDialog;
import com.mp.android.apps.livevblank.view.TimerTextView;
import com.mp.android.apps.utils.ManBitmapUtils;
import com.mp.android.apps.utils.SoftKeyboardUtils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.victor.loading.rotate.RotateLoading;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.picchooser.SelectPictureActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 编辑页Activity
 * 启动->相机->图片剪裁->图片滤镜->返回到编辑页
 * 启动->相册->图片剪裁->图片滤镜->返回到编辑页面
 */
public class EditCardActivity extends StoryboardActivity implements View.OnClickListener {
    private String currentTemplate;
    private ImageView ivAdd;
    private LinearLayout llKeyboard;
    private EditText etContent;
    private TextView tvTitle;
    private TextView tvRightBtn;
    private ImageView edit_photoView;
    private LinearLayout contentView;
    private ImageView ivBack;
    private EditCardTextView editcardTextview;
    private TextView toPeople;
    private TextView byPeople;
    private TimerTextView timerTextview;
    private String path;
    private File tempFile;
    private RotateLoading rotateloading;
    private ImageDesBean imageDesBean;
    //照片uri
    private Uri uri;
    //基于相机拍照获取图片
    public static final int LIVEBLANK_REQUEST_CAMERA = 1;
    //图片剪裁完成后获得完成后的bitmap
    public static final int LIVEBLANK_REQUEST_EDIT_BITMAP = 2;
    //基于相册获取图片
    public static final int LIVEBLANK_REQUEST_ALBUM = 3;

    public static final int LIVEBLANK_REQUEST_SEARCH_TO = 4;
    public static final int LIVEBLANK_REQUEST_SEARCH_By = 5;
    public static final int REQUEST_PERMISSON_SORAGE = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        imageDesBean = new ImageDesBean();
        currentTemplate = Constants.getCurrentTemplate();
        int layoutID = R.layout.editcard_main;
        if (!TextUtils.isEmpty(currentTemplate)) {
            if (currentTemplate.equals(Constants.TEMPLATES[0])) {
                layoutID = R.layout.editcard_main_template_1;
            } else if (currentTemplate.equals(Constants.TEMPLATES[1])) {
                layoutID = R.layout.editcard_main_template_2;
            } else if (currentTemplate.equals(Constants.TEMPLATES[2])) {
                layoutID = R.layout.editcard_main_template_3;
            } else if (currentTemplate.equals(Constants.TEMPLATES[3])) {
                layoutID = R.layout.editcard_main_template_4;
            } else if (currentTemplate.equals(Constants.TEMPLATES[4])) {
                layoutID = R.layout.editcard_main_template_5;
            } else if (currentTemplate.equals(Constants.TEMPLATES[5])) {
                layoutID = R.layout.editcard_main_template_6;
            } else if (currentTemplate.equals(Constants.TEMPLATES[6])) {
                layoutID = R.layout.editcard_main_template_7;
            } else if (currentTemplate.equals(Constants.TEMPLATES[7])) {
                layoutID = R.layout.editcard_main_template_8;
            } else if (currentTemplate.equals(Constants.TEMPLATES[8])) {
                layoutID = R.layout.editcard_main_template_9;
            } else {
                layoutID = R.layout.editcard_main;
            }
        }
        setContentView(layoutID);
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.color.white);
        this.getWindow().setBackgroundDrawable(drawable);
        initView();
    }

    private void initView() {
        ivAdd = findViewById(R.id.iv_add);
        ivAdd.setOnClickListener(this);
        llKeyboard = findViewById(R.id.ll_keyboard);
        etContent = findViewById(R.id.et_content);
        tvTitle = findViewById(R.id.tv_title);
        tvRightBtn = findViewById(R.id.tv_right_btn);
        edit_photoView = findViewById(R.id.edit_photoView);
        contentView = findViewById(R.id.content_view);
        ivBack = findViewById(R.id.iv_back);
        editcardTextview = findViewById(R.id.editcard_textview);
        editcardTextview.setCutChars("\n");
        editcardTextview.setText("请填写信封内容");
        editcardTextview.setOnClickListener(this);
        toPeople = findViewById(R.id.to_people);
        toPeople.setHintTextColor(getResources().getColor(R.color.black));
        toPeople.setTextColor(getResources().getColor(R.color.black));
        byPeople = findViewById(R.id.by_people);
        byPeople.setTextColor(getResources().getColor(R.color.black));
        byPeople.setHintTextColor(getResources().getColor(R.color.black));
        List<String> bySource = JSON.parseArray(getSharedPreferences("By_People", MODE_PRIVATE).getString("sourceData", ""), String.class);
        if (bySource != null && bySource.size() > 0) {
            byPeople.setText(bySource.get(0));
        }

        ivBack.setOnClickListener(this);
        edit_photoView.setOnClickListener(this);
        tvRightBtn.setOnClickListener(this);
        toPeople.setOnClickListener(this);
        byPeople.setOnClickListener(this);
        tvTitle.setText("编辑卡片");
        tvRightBtn.setText("预览");
        timerTextview = findViewById(R.id.timer_textview);
        if (currentTemplate.equals(Constants.TEMPLATES[6]) || currentTemplate.equals(Constants.TEMPLATES[7])) {
            Date date = new Date();
            timerTextview.setText(timerTextview.setCurrentTime(date));
            imageDesBean.setSelectTime(String.valueOf(date.getTime()));
        }
        timerTextview.setOnClickListener(this);
        setEditOnchangeListener();
        findViewById(R.id.tv_by).setOnClickListener(this);
        findViewById(R.id.tv_date).setOnClickListener(this);
        findViewById(R.id.tv_picture).setOnClickListener(this);
        findViewById(R.id.tv_template).setOnClickListener(this);
        findViewById(R.id.tv_to).setOnClickListener(this);
        findViewById(R.id.iv_confirm).setOnClickListener(this);
        rotateloading = findViewById(R.id.rotateloading);
        rotateloading.setLoadingColor(Color.BLUE);
    }

    private void setEditOnchangeListener() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editcardTextview.setText("请填写信封内容");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    editcardTextview.setText("请填写信封内容");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    editcardTextview.setText("请填写信封内容");
                } else {
                    editcardTextview.setText(s.toString());
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (SoftKeyboardUtils.isSoftShowing(EditCardActivity.this)) {
                SoftKeyboardUtils.hideSystemSoftKeyboard(EditCardActivity.this);
                if (llKeyboard.getVisibility() == View.VISIBLE) {
                    llKeyboard.setVisibility(View.GONE);
                }

                return true;
            }
        }
        return super.dispatchTouchEvent(ev);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_add:
                if (llKeyboard.getVisibility() == View.VISIBLE) {
                    llKeyboard.setVisibility(View.GONE);
                } else {
                    llKeyboard.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.tv_picture:
            case R.id.edit_photoView:
                SelectCustomDialog selectCustomDialog = new SelectCustomDialog(EditCardActivity.this);
                selectCustomDialog.show();
                selectCustomDialog.setAblumSelectClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Acp.getInstance(EditCardActivity.this).request(new AcpOptions.Builder()
                                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                            @Override
                            public void onGranted() {
                                getPicFromAlbum();
                                selectCustomDialog.dismiss();
                            }

                            @Override
                            public void onDenied(List<String> permissions) {
                                Toast.makeText(EditCardActivity.this, "必要权限被拒绝,请到设置界面允许被拒绝权限", Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                });

                selectCustomDialog.setCameraSelectCliclListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Acp.getInstance(EditCardActivity.this).request(new AcpOptions.Builder()
                                .setPermissions(Manifest.permission.CAMERA).build(), new AcpListener() {
                            @Override
                            public void onGranted() {
                                getPicFromCamera(LIVEBLANK_REQUEST_CAMERA);
                                selectCustomDialog.dismiss();
                            }

                            @Override
                            public void onDenied(List<String> permissions) {
                                Toast.makeText(EditCardActivity.this, "必要权限被拒绝,请到设置界面允许被拒绝权限", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });


                break;
            case R.id.tv_right_btn:
                Intent intent = new Intent(this, PreviewActivity.class);

                Bitmap bitmap = ManBitmapUtils.getViewBitmap(contentView);
                if (bitmap != null) {
                    Bundle bundle = new Bundle();
                    imageDesBean.setImageDes(editcardTextview.getText());
                    imageDesBean.setByPeople(byPeople.getText().toString());
                    imageDesBean.setToPeople(toPeople.getText().toString());
                    imageDesBean.setSelectTime(String.valueOf(timerTextview.getTime()));
                    imageDesBean.setTemplateID(currentTemplate);
                    bundle.putString("content_bitmap", ManBitmapUtils.saveBitmap(this, bitmap));
                    bundle.putSerializable("imageDes", imageDesBean);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "对不起,图片引起系统异常,请稍后重试,如有需要请提交反馈,谢谢", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_to:
            case R.id.to_people:
                Intent toPeople = new Intent(EditCardActivity.this, SearchPeopleActivity.class);
                toPeople.putExtra("people", "To_People");
                startActivityForResult(toPeople, LIVEBLANK_REQUEST_SEARCH_TO);
                break;
            case R.id.tv_by:
            case R.id.by_people:
                Intent byPeople = new Intent(EditCardActivity.this, SearchPeopleActivity.class);
                byPeople.putExtra("people", "By_People");
                startActivityForResult(byPeople, LIVEBLANK_REQUEST_SEARCH_By);
                break;
            case R.id.editcard_textview:
                showSoftInputFromWindow(etContent);
                break;
            case R.id.tv_date:
            case R.id.timer_textview:
                showDatePickerDialog(this, Calendar.getInstance(), timerTextview);
                break;
            case R.id.tv_template:
                Intent template = new Intent(this, ChoiceItemActivity.class);
                startActivity(template);
                break;
            case R.id.iv_confirm:
                if (SoftKeyboardUtils.isSoftShowing(EditCardActivity.this)) {
                    SoftKeyboardUtils.hideSystemSoftKeyboard(EditCardActivity.this);
                    if (llKeyboard.getVisibility() == View.VISIBLE) {
                        llKeyboard.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                break;
        }
    }

    public static void showDatePickerDialog(Activity activity, Calendar calendar, TimerTextView timerTextView) {
        new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        timerTextView.setTime(year, monthOfYear, dayOfMonth);
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void showSoftInputFromWindow(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        if (llKeyboard.getVisibility() == View.VISIBLE) {
            llKeyboard.setVisibility(View.GONE);
        }

        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIVEBLANK_REQUEST_CAMERA && resultCode == RESULT_OK) {
            rotateloading.start();
            Glide.with(this).asBitmap().load(uri).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    String filePath = ManBitmapUtils.saveBitmap(getApplicationContext(), resource);
                    File outputFile = FileUtils.genEditFile();
                    if (outputFile != null) {
                        EditImageActivity.start(EditCardActivity.this, filePath, outputFile.getAbsolutePath(), LIVEBLANK_REQUEST_EDIT_BITMAP);
                    }
                    rotateloading.stop();
                }
            });

        } else if (requestCode == LIVEBLANK_REQUEST_ALBUM && resultCode == RESULT_OK) {
            assert data != null;
            path = data.getStringExtra("imgPath");
            File outputFile = FileUtils.genEditFile();
            if (outputFile != null)
                EditImageActivity.start(this, path, outputFile.getAbsolutePath(), LIVEBLANK_REQUEST_EDIT_BITMAP);
        } else if (requestCode == LIVEBLANK_REQUEST_EDIT_BITMAP && resultCode == RESULT_OK) {
            assert data != null;
            handleEditorImage(data);
        } else if (requestCode == LIVEBLANK_REQUEST_SEARCH_TO && resultCode == RESULT_OK) {
            assert data != null;
            String peopleName = data.getStringExtra("peopleName");
            toPeople.setText(peopleName);
        } else if (requestCode == LIVEBLANK_REQUEST_SEARCH_By && resultCode == RESULT_OK) {
            assert data != null;
            String bypeople = data.getStringExtra("peopleName");
            byPeople.setText(bypeople);
        }
    }

    /**
     * 从相册选择编辑图片
     */
    private void selectFromAblum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAblumWithPermissionsCheck();
        } else {
            openAblum();
        }//end if
    }

    private void openAblumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSON_SORAGE);
            return;
        }
        openAblum();
    }

    private void openAblum() {
        EditCardActivity.this.startActivityForResult(new Intent(
                        EditCardActivity.this, SelectPictureActivity.class),
                LIVEBLANK_REQUEST_ALBUM);
    }

    private void handleEditorImage(Intent data) {
        String newFilePath = data.getStringExtra(EditImageActivity.EXTRA_OUTPUT);
        boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false);

        if (!isImageEdit) {
            //未编辑  还是用原来的图片
            newFilePath = data.getStringExtra(EditImageActivity.FILE_PATH);
        }
        Log.d("image is edit", isImageEdit + "");
        imageDesBean.setImageCachePath(newFilePath);
        Glide.with(this).load(newFilePath).dontTransform().into(edit_photoView);
    }

    /**
     * 启动系统相机,保存拍照后的图片为tempFile
     * 拍照后使用
     */
    private void getPicFromCamera(int requestCode) {
        //用于保存调用相机拍照后所生成的文件

        //调用照相机返回图片文件

        tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");//跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            uri = FileProvider.getUriForFile(EditCardActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", tempFile);
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            uri = Uri.fromFile(tempFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, requestCode);
    }


    /**
     * 基于系统相册,获取图片提供
     */
    private void getPicFromAlbum() {
//        selectImage(LIVEBLANK_REQUEST_ALBUM);
        selectFromAblum();
    }


}
