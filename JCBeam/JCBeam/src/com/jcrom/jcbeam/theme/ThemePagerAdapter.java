package com.jcrom.jcbeam.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcrom.jcbeam.R;

public class ThemePagerAdapter  extends PagerAdapter {
    private Theme mTheme;
    private Context mContext;

    public ThemePagerAdapter(Context context,Theme theme) {
        mTheme = theme;
        mContext = context;
    }


    @Override
    public int getCount() {
        // Pagerに登録したビューの数を返却。サンプルは固定なのでNUM_OF_VIEWS
        return mTheme.length();
    }

    /**
     * ページを生成する position番目のViewを生成し返却するために利用
     *
     * @param container: 表示するViewのコンテナ
     * @param position : インスタンス生成位置
     * @return ページを格納しているコンテナを返却すること。サンプルのようにViewである必要は無い。
     */
    @Override
    public Object instantiateItem(View collection, int position) {

        // レイアウト作成
        LayoutInflater LayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = LayoutInflater.inflate(R.layout.theme, null);

        TextView tv = (TextView) v.findViewById(R.id.ThemeText);
        tv.setText(mTheme.name(position));

        final ImageView iv = (ImageView) v.findViewById(R.id.ThemeImage);
        final String path = mTheme.getThemeImage(position);

        // 画像読み込み
        final Handler h = new Handler();
        new Thread(new Runnable() {
            public void run() {
                final Bitmap b;
                if( path != null){
                    b = BitmapFactory.decodeFile(path);
                }else{
                    b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.archive);
                }
                h.post(new Runnable() {
                    public void run() {
                        iv.setImageBitmap(b);
                    }
                });
            }
        }).start();

        ((ViewPager) collection).addView(v, 0);

        return v;
    }

    /**
     * ページを破棄する。 postion番目のViweを削除するために利用
     *
     * @param container: 削除するViewのコンテナ
     * @param position : インスタンス削除位置
     * @param object : instantiateItemメソッドで返却したオブジェクト
     */
    @Override
    public void destroyItem(View collection, int position, Object view) {
        // ViewPagerに登録していたTextViewを削除する
        ((ViewPager) collection).removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // 表示するViewがコンテナに含まれているか判定する(表示処理のため)
        // objecthainstantiateItemメソッドで返却したオブジェクト。
        // 今回はTextViewなので以下の通りオブジェクト比較
        return view == ((View) object);
    }

    @Override
    public void startUpdate(View arg0) {
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}