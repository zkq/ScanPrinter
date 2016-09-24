package com.example.zkq.scanprinter.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.smartdevicesdk.printer.PrinterLib;

import java.util.HashMap;

/**
 * Created by zkq on 2016/9/23.
 */

public class ImageUtil {
    //创建图片
    public static Bitmap createBitmap(Context context, String barcode, int w, int h)
    {
        Bitmap numBitmap = createNumBitmap(context, barcode, w, h);
        Bitmap barBitmap;
        try {
            barBitmap = createBarBitmap(barcode, w, h);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        Bitmap result = mixBitmap(barBitmap, numBitmap, new PointF(0, h));
        return result;
    }

    //创建数字图片
    private static Bitmap createNumBitmap(Context context, String barcode, int w, int h)
    {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                w, h);
        tv.setLayoutParams(layoutParams);
        tv.setText(barcode);
        tv.setTextSize(25);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(w);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(Color.WHITE);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }

    //创建条形码
    private static Bitmap createBarBitmap(String barcode, int w, int h) throws WriterException
    {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        HashMap<EncodeHintType, String> hints = new HashMap<>(2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(barcode, BarcodeFormat.CODE_128, w, h, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    //混合两个图片
    private static Bitmap mixBitmap(Bitmap first, Bitmap second, PointF fromPoint)
    {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }

        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
                first.getHeight() + second.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        return newBitmap;
    }


    //获取图片的字节数组 供打印
    public static byte[] getImageBytes(Bitmap bitmap) {
        //int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        bitmap=resizeImage(bitmap, 48 * 8, h);

        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        int[] mIntArray = new int[mWidth * mHeight];
        bitmap.getPixels(mIntArray, 0, mWidth, 0, 0, mWidth, mHeight);
        byte[]  bt = PrinterLib.getBitmapData(mIntArray, mWidth, mHeight);
        bitmap.recycle();
        return bt;
    }

    private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        if(width>w)
        {
            float scaleWidth = ((float) w) / width;
            float scaleHeight = ((float) h) / height+24;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleWidth);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
            return resizedBitmap;
        }else{
            Bitmap resizedBitmap = Bitmap.createBitmap(w, height+24, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(resizedBitmap);
            Paint paint = new Paint();
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, (w-width)/2, 0, paint);
            return resizedBitmap;
        }
    }




}
