package com.mp.android.apps.livevblank.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

/**
 * 为imageview生成的带犹豫倒影的bitmap
 */
public class ImageUtil {
    public static Bitmap getReverseBitmapById(int resId, Context context) {
        Bitmap sourceBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        //绘制原图的下一半图片
        Matrix matrix = new Matrix();
        //倒影翻转
        matrix.setScale(1, -1);

        Bitmap inverseBitmap = Bitmap.createBitmap(sourceBitmap, 0, sourceBitmap.getHeight() / 2, sourceBitmap.getWidth(), sourceBitmap.getHeight() / 3, matrix, false);
        //合成图片
        Bitmap groupbBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight() + sourceBitmap.getHeight() / 3 + 60, sourceBitmap.getConfig());
        //以合成图片为画布
        Canvas gCanvas = new Canvas(groupbBitmap);
        //将原图和倒影图片画在合成图片上
        gCanvas.drawBitmap(sourceBitmap, 0, 0, null);
        gCanvas.drawBitmap(inverseBitmap, 0, sourceBitmap.getHeight() + 50, null);
        //添加遮罩
        Paint paint = new Paint();
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        LinearGradient shader = new LinearGradient(0, sourceBitmap.getHeight() + 50, 0,
                groupbBitmap.getHeight(), Color.BLACK, Color.TRANSPARENT, tileMode);
        paint.setShader(shader);
        //这里取矩形渐变区和图片的交集
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        gCanvas.drawRect(0, sourceBitmap.getHeight() + 50, sourceBitmap.getWidth(), groupbBitmap.getHeight(), paint);
        return groupbBitmap;
    }
}
