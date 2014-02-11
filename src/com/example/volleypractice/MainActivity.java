package com.example.volleypractice;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferencesFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

    /** タグ名 */
    private final String TAG = getClass().getSimpleName();

    /** リクエストキュー */
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString("test", "test");
        editor.commit();

        // 画像取得できるURLのリスト
        final ArrayList<String> urlList = new ArrayList<String>();
        for (int i = 0 ; i < 100 ; i++) {
            urlList.add("http://techbooster.org/wp-content/uploads/2013/08/densi.png");
        }

        final ListView listView = (ListView) findViewById(R.id.listView);

        // ImageLoaderを持っているアダプターを設定
        mQueue = Volley.newRequestQueue(this);
        final ImageLoaderAdapter adapter = new ImageLoaderAdapter(this, urlList, mQueue);
        listView.setAdapter(adapter);
    }

    private void doNetworkImageView() {
        mQueue = Volley.newRequestQueue(this);
        String url = "http://techbooster.org/wp-content/uploads/2013/08/densi.png";

        final NetworkImageView view = (NetworkImageView) findViewById(R.id.network_image_view);
        view.setImageUrl(url, new ImageLoader(mQueue, new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                Log.i(TAG, "1:" + url);
            }
            @Override
            public Bitmap getBitmap(String url) {
                Log.i(TAG, "2:" + url);
                return null;
            }
        }));
    }

    private void doJson() {
        // 東京都の天気情報
        final String url = "http://weather.livedoor.com/forecast/webservice/json/v1?city=400040";
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Method.GET, url, null,
        new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // JSONObjectのパース、List、Viewの追加等
                try {
                    Log.d(TAG, response.toString());
                    Log.i(TAG, "Public Time = " + response.getString("publicTime"));
                    final JSONArray forecasts = response.getJSONArray("forecasts");
                    for (int i = 0; i < forecasts.length() ;i++) {
                        JSONObject json = forecasts.getJSONObject(i);
                        Log.i(TAG, "Image = " + json.get("image"));
                    }
                    Log.i(TAG, "Title = " + response.getString("title"));
                    Log.i(TAG, "Location = " + response.getString("location"));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
            }
        }));
    }

    private static class ImageLoaderAdapter extends ArrayAdapter<String> {
//        /** リクエストキュー */
//        private RequestQueue mQueue;
        /** ローダー */
        private ImageLoader mImageLoader;
        /** インフレータ */
        private LayoutInflater mInflater;
        /** コンストラクタ */
        private ImageLoaderAdapter(
                final Context context,
                final List<String> urlList,
                final RequestQueue queue) {
            super(context, 0,urlList);
//            mQueue = queue;
            mImageLoader = new ImageLoader(queue, new ImageCacheSample());
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_item, parent, false);
                holder = new ViewHolder();
                holder.body = (TextView) convertView.findViewById(R.id.body);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String url = getItem(position);
            holder.body.setText(url);

            final ImageListener listener = ImageLoader.getImageListener(holder.image,
                    android.R.drawable.spinner_background, /* 表示待ち時の画像 */
                    android.R.drawable.ic_dialog_alert); /* エラー時の画像 */

            mImageLoader.get(url, listener);
            return convertView;
        }


        private static class ViewHolder {
            ImageView image;
            TextView body;
        }
    }

    private static class ImageCacheSample implements ImageLoader.ImageCache {

        private LruCache<String, Bitmap> mMemoryCache;

        private ImageCacheSample() {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
//            final int cacheSize = 5 * 1024 * 1024;

            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // 使用キャッシュサイズ(KB単位)
                    return bitmap.getByteCount() / 1024;
                }
                
            };
        }
        @Override
        public Bitmap getBitmap(String url) {
            return mMemoryCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mMemoryCache.put(url, bitmap);
        }
        
    }
}
