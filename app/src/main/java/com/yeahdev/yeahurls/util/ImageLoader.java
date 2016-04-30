package com.yeahdev.yeahurls.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.net.URL;

public class ImageLoader extends AsyncTask<String, String, Bitmap> {

    private CircularImageView imageView;

    public ImageLoader(CircularImageView circularImageView) {
        this.imageView = circularImageView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        try {
            return BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        if (image != null) {
            if (this.imageView != null) {
                this.imageView.setImageBitmap(image);
            }
        }
    }
}
