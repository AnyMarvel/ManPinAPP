package com.google.android.apps.photolab.storyboard.pipeline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.activity.ComicActivity;
import com.google.android.apps.photolab.storyboard.activity.ComicPanel;
import com.google.android.apps.photolab.storyboard.activity.ComicPresenter;
//import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ComicIO {
    public static final String LAST_FILTERED_FILENAME = "last_filtered.png";
    private static final String TAG = "ComicIO";
    private static ComicIO instance;
    private String defaultMovieName = "comicImages";
    private boolean saveAppVersionToTitle = false;
    /**
     * 选择分析视频Uri地址
     */
    public Uri selectedVideoUri;
    private int storedFrames = 0;

    /**
     * 获取comicIO单例
     *
     * @return 返回ComicIO单例
     */
    public static ComicIO getInstance() {
        if (instance == null) {
            instance = new ComicIO();
        }
        return instance;
    }

    public String currentMovieName() {
        String movieName = this.defaultMovieName;
        if (this.selectedVideoUri == null) {
            return movieName;
        }
        movieName = this.selectedVideoUri.getPath();
        int cut = movieName.lastIndexOf(47);
        if (cut != -1) {
            return movieName.substring(cut + 1);
        }
        return movieName;
    }

    public int getStoredFrameCount() {
        return this.storedFrames;
    }

    private static File getFile(File dir, String name) {
        dir.mkdirs();
        File f = new File(dir, name);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * 获取图片路径
     * /sdcard/.manpin_temp_/
     * 存放临时文件
     *
     * @return 返回图片临时存储文件夹路径
     */
    public static File getImageFolder() {
        String valueOf = String.valueOf(Environment.getExternalStorageDirectory());
        return new File(new StringBuilder(String.valueOf(valueOf).length() + 19).append(valueOf).append("/.manpin_temp_/").toString());
    }

    public File getPictureFolder() {
        return Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.DIRECTORY_DCIM).concat("/manpin"));
    }

    /**
     * 清空预加载图片目录
     */
    public void clearImageFolder() {
        this.storedFrames = 0;
        File dir = getImageFolder();//获得目录
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                new File(dir, file).delete();
            }
        }
    }

    public void writeImageToPath(Bitmap image, String prefix) {
        if (image != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(CompressFormat.JPEG, 95, bytes);
                File dir = getImageFolder();
                dir.mkdirs();
                File f = getFile(dir, new StringBuilder(String.valueOf(prefix).length() + 16).append(prefix).append("_").append(this.storedFrames).append(".jpg").toString());
                if (f != null) {
                    FileOutputStream fo = new FileOutputStream(f, false);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    this.storedFrames++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveImageToPictureFolder(Bitmap image) {
        if (image != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(CompressFormat.JPEG, 95, bytes);
                File dir = getPictureFolder();
                dir.mkdirs();
                String appVersion = "";
                if (this.saveAppVersionToTitle) {
                    String str = "_";
                    String valueOf = String.valueOf(ComicActivity.getActivity().getAppVersionString());
                    appVersion = (valueOf.length() != 0 ? str.concat(valueOf) : new String(str)).replaceAll("\\.", "_");
                }
                String timestamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                File f = getFile(dir, new StringBuilder((String.valueOf(timestamp).length() + 15) + String.valueOf(appVersion).length()).append("Storyboard_").append(timestamp).append(appVersion).append(".jpg").toString());
                if (f != null) {
                    FileOutputStream fo = new FileOutputStream(f, false);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    Toast.makeText(ComicActivity.getActivity(), "Saved to your gallery", Toast.LENGTH_SHORT).show();
                }
                ComicActivity.getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void zipBitmapPacket() {
        File f = getFile(getImageFolder(), "randomFrames.zip");
        if (f != null) {
            try {
                FileOutputStream fos = new FileOutputStream(f, false);
                ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(fos));
                ArrayList<ComicAsset> assets = MediaManager.instance().getAssets();
                int bmpCount = Math.min(5, assets.size());
                for (int i = 0; i < bmpCount; i++) {
                    addBitmapToZip("img_" + i + ".jpg", ((ComicAsset) assets.get(i)).getBitmap(), zipStream);
                }
                zipStream.close();
                fos.flush();
                fos.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        shareZipFile(f);
    }

    public void addBitmapToZip(String name, Bitmap bmp, ZipOutputStream zipStream) {
        if (bmp != null) {
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.JPEG, 90, bmpStream);
            byte[] byteArray = bmpStream.toByteArray();
            try {
                zipStream.putNextEntry(new ZipEntry(name));
                zipStream.write(byteArray, 0, byteArray.length);
                bmpStream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void shareZipFile(File file) {
        String movieName = this.defaultMovieName;
        if (this.selectedVideoUri != null) {
            movieName = this.selectedVideoUri.getPath();
            int cut = movieName.lastIndexOf(47);
            if (cut != -1) {
                movieName = movieName.substring(cut + 1);
            }
        }
        Log.i(TAG, movieName);
        Uri uri = Uri.fromFile(file);
        Intent sendIntent = new Intent();
        sendIntent.setAction("android.intent.action.SEND");
        sendIntent.putExtra("android.intent.extra.STREAM", uri);
        String str = "android.intent.extra.SUBJECT";
        String str2 = "Random files from ";
        String valueOf = String.valueOf(movieName);
        sendIntent.putExtra(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        sendIntent.setType("application/zip");
        ComicActivity.getActivity().startActivity(Intent.createChooser(sendIntent, "Save files..."));
    }

    public void shareImages(ComicPresenter comicPresenter) {
        String prefix = "img";
        Intent sendIntent = new Intent();
        sendIntent.setAction("android.intent.action.SEND_MULTIPLE");
        sendIntent.putExtra("android.intent.extra.SUBJECT", "Files from Storyboard");
        sendIntent.setType("image/*");
        ArrayList<Uri> uris = new ArrayList();
        for (int i = 0; i < comicPresenter.getComicGenerator().getPanelCount(); i++) {
            ComicPanel cp = comicPresenter.getComicGenerator().getComicPanel(i);
            uris.add(Uri.fromFile(getFile(getImageFolder(), new StringBuilder(String.valueOf(prefix).length() + 16).append(prefix).append("_").append(cp.getImageIndex()).append(".jpg").toString())));
        }
        sendIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
        ComicActivity.getActivity().startActivity(Intent.createChooser(sendIntent, "Save files..."));
    }

    public void shareAllImages() {
        String prefix = "img";
        Intent sendIntent = new Intent();
        sendIntent.setAction("android.intent.action.SEND_MULTIPLE");
        sendIntent.putExtra("android.intent.extra.SUBJECT", "Files from Storyboard");
        sendIntent.setType("image/*");
        ArrayList<Uri> uris = new ArrayList();
        for (int i = 0; i < this.storedFrames; i++) {
            uris.add(Uri.fromFile(getFile(getImageFolder(), new StringBuilder(String.valueOf(prefix).length() + 16).append(prefix).append("_").append(i).append(".jpg").toString())));
        }
        sendIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
        ComicActivity.getActivity().startActivity(Intent.createChooser(sendIntent, "Save files..."));
    }

    public Uri writeComicForShare(Bitmap image, String filename) {
        Uri result = null;
        if (image != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(CompressFormat.JPEG, 95, bytes);
                File dir = getImageFolder();
                dir.mkdirs();
                File f = getFile(dir, filename);
                if (f != null) {
                    FileOutputStream fo = new FileOutputStream(f, false);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    result = Uri.fromFile(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void shareImage(Bitmap bmp) {
        String date = new Date().toString();
        Uri uri = writeComicForShare(bmp, new StringBuilder(String.valueOf(date).length() + 21).append("sharedStoryboard_").append(date).append(".jpg").toString());
        if (uri != null) {
            Intent share = new Intent();
            share.setType("image/jpeg");
            share.setAction("android.intent.action.SEND");
            share.putExtra("android.intent.extra.SUBJECT", "Comic from Storyboard");
            share.putExtra("android.intent.extra.STREAM", uri);
            ComicActivity.getActivity().startActivityForResult(Intent.createChooser(share, "Share storyboard"), ComicActivity.SHARE_IMAGE_REQUEST);
        }
    }

    public static String writeTexture(Bitmap texture, String name) {
        String result = "";
        if (texture != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                texture.compress(CompressFormat.JPEG, 95, bytes);
                File dir = getImageFolder();
                dir.mkdirs();
                File f = getFile(dir, name);
                if (f != null) {
                    FileOutputStream fo = new FileOutputStream(f, false);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    result = f.getAbsolutePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @param name
     * @return
     */
    public static Bitmap readTexture(String name) {
        Bitmap bmp = null;
        try {
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            File f = getFile(getImageFolder(), name);
            if (f != null) {
                try {
                    bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return bmp;
    }

    public void loadExistingImages() {
        String prefix = "img";
        File dir = getImageFolder();
        this.storedFrames = 0;
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        while (true) {
            Bitmap bitmap = null;
            File f = getFile(dir, new StringBuilder(String.valueOf(prefix).length() + 16).append(prefix).append("_").append(this.storedFrames).append(".jpg").toString());
            if (f != null) {
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                MediaManager.instance().addBitmap(bitmap);
                this.storedFrames++;
            } else {
                return;
            }
        }
    }
}
