package com.gildStudios.DiTo.androidApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
        private static VolleySingleton mInstance;
        private static Context mContext;

        private RequestQueue mRequestQueue;
        private ImageLoader mImageLoader;

        private VolleySingleton(Context context) {
            mContext = context;
            mRequestQueue = getRequestQueue();

            mImageLoader = new ImageLoader(mRequestQueue,
                    new ImageLoader.ImageCache() {
                        private final LruCache<String, Bitmap>
                                lruCache = new LruCache<String, Bitmap>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return lruCache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            lruCache.put(url, bitmap);
                        }
                    });
        }

        public static synchronized VolleySingleton getInstance(Context context) {
            if(mInstance == null) {
                mInstance = new VolleySingleton(context);
            }
            return mInstance;
        }

        private RequestQueue getRequestQueue() {
            if(mRequestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking.
                mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
            }
            return mRequestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }

        public ImageLoader getImageLoader() {
            return mImageLoader;
        }

}
