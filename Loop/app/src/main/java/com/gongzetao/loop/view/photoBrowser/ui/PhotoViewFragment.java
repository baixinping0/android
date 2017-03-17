package com.gongzetao.loop.view.photoBrowser.ui;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.gongzetao.loop.R;
import com.gongzetao.loop.progressglide.ProgressModelLoader;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.MotionEventUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.RoundProgressBar;
import com.huawei.android.pushselfshow.richpush.html.a;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class PhotoViewFragment extends Fragment {


    boolean isStartLoading = false;

    private String photoType = null;

    private String url;
    private RoundProgressBar progressBar;

    public PhotoViewFragment() {
        // Required empty public constructor
    }

    public PhotoViewFragment(String url){
        this.url = url;
    }

    @NotNull
    private PhotoView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.photoview_fragment_photo_view, container, false);


        imageView = (PhotoView) view.findViewById(R.id.photoIm);
        RelativeLayout fragmentPhoto = (RelativeLayout)view.findViewById(R.id.fragment_photo);
        progressBar= (RoundProgressBar) view.findViewById(R.id.progressBar);
        imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                getActivity().finish();
            }
        });
        fragmentPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog);

                TextView saveToPhone = (TextView)dialog.findViewById(R.id.save_to_phone);
                TextView sendToFriend = (TextView)dialog.findViewById(R.id.send_to_friend);
                TextView cancelDialog = (TextView)dialog.findViewById(R.id.cancel_dialog);

                saveToPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MotionEventUtils.setMotionEvent(v,getActivity());
                        Date date = new Date(System.currentTimeMillis());
                        String[] s = null;
                        if(photoType!=null) {
                            s = photoType.split("/");
                            if(s[1].length()>5){
                                s[1] = "jpeg";
                            }
                        }
                        String fileName = System.currentTimeMillis()+"."+s[1];
                        LogUtils.MyLog(fileName);
                        LogUtils.MyLog("00000");
//                        saveBitmap(imageView, fileName,"/loopPic");
                        LogUtils.MyLog("44444");
                        Drawable drawable = imageView.getDrawable();
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        saveFile(bitmap,fileName,"/loopPic");
                    }
                });
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
//                lp.x = 120; // X坐标偏移量
//                lp.y = 100; // Y坐标偏移量
//                lp.width = 300; // 宽度
//                lp.height = 300; // 高度
                lp.alpha = 0.93f; // 透明度
                dialogWindow.setAttributes(lp);
                WindowManager m = getActivity().getWindowManager();
                Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
                WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//                p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
                p.width = (int) (d.getWidth() * 0.85); // 宽度设置为屏幕的0.65
                dialogWindow.setAttributes(p);
                dialog.show();
                return false;
            }
        });
        LogUtils.MyLog(url);
        AVQuery<AVObject> fileQuery = new AVQuery<>("_File");
        fileQuery.whereEqualTo("url", url);
        fileQuery.selectKeys(Arrays.asList("url", "mime_type"));
        fileQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list != null && list.size() > 0) {
                    photoType = (String) list.get(0).get("mime_type");

                    if (e == null && photoType.endsWith("gif")) {
                        Glide.with(PhotoViewFragment.this)
                                .using(new ProgressModelLoader(new progressHandler(PhotoViewFragment.this)))
                                .load(url).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).fitCenter().dontAnimate()
                                .error(null).placeholder(R.drawable.picture_load)/*.thumbnail(0.01f)*/
                                .into(imageView);
                    } else {
                        Glide.with(PhotoViewFragment.this)
                                .using(new ProgressModelLoader(new progressHandler(PhotoViewFragment.this)))
                                .load(url).fitCenter().dontAnimate()
                                .error(null).placeholder(R.drawable.picture_load)
                                .into(new GlideDrawableImageViewTarget(imageView) {
                                    @Override
                                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                        super.onResourceReady(resource, animation);
                                    }

                                    @Override
                                    public void onStart() {
                                        super.onStart();
                                    }

                                    @Override
                                    public void onStop() {
                                        super.onStop();
                                    }
                                });
                    }
                }
            }
        });

        return view;
    }

    public  void saveBitmap(final ImageView view,final String fileName, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.MyLog("进入run方法");
                  String SAVE_PIC_PATH= Environment.getExternalStorageState()
                        .equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment
                        .getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
                String filePath =  SAVE_PIC_PATH + path;
                if(path==null)
                    LogUtils.MyLog("path为空");
                else LogUtils.MyLog("path不为空");
                LogUtils.MyLog("path:"+path);
                LogUtils.MyLog("filePath:"+filePath);
                File foder = new File(filePath);
                if (!foder.exists()) {
                    foder.mkdirs();
                }
                Drawable drawable = view.getDrawable();
                if (drawable == null) {
                    return;
                }
                FileOutputStream outStream = null;
                File file = new File(foder,fileName);
                if (file.isDirectory()) {//如果是目录不允许保存
                    LogUtils.MyLog("目录不允许保存");
                    return;
                }
                try {
                    LogUtils.MyLog("11111");
                    outStream = new FileOutputStream(file);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    bitmap.recycle();
                    LogUtils.MyLog("22222");

                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    getActivity().sendBroadcast(intent);

                    ToastUtils.showToast(getActivity(), "保存成功");
                    LogUtils.MyLog("保存成功");
                } catch (IOException e) {
                    LogUtils.MyLog(e.toString());
                } finally {
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }

                    } catch (IOException e) {
                        LogUtils.MyLog(e.toString());

                    }
                }                    LogUtils.MyLog("33333");

            }
        }).start();

    }

    public static void saveFile(Bitmap bm, String fileName, String path)
        {
        String SAVE_PIC_PATH= Environment.getExternalStorageState()
                .equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment
                .getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
        String SAVE_REAL_PATH = SAVE_PIC_PATH + "/good/savePic";//保存的确切位置
        String subForder = SAVE_REAL_PATH + path;
        File foder = new File(subForder);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(subForder, fileName);
                BufferedOutputStream bos = null;
                try {
                    if (!myCaptureFile.exists()) {
                        myCaptureFile.createNewFile();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                }catch (IOException e){
                    LogUtils.MyLog(e.toString());
                }finally {
                    try {
                        if(bos!=null) {
                            bos.flush();
                            bos.close();
                        }
                    }catch(IOException e){
                        LogUtils.MyLog(e.toString());
                    }
                }
        LogUtils.MyLog("baocunchenggong");
    }
    private class progressHandler extends Handler {
        private final WeakReference<PhotoViewFragment> photoViewFragmentWeakReference;

        public progressHandler(PhotoViewFragment photoViewFragment) {
            photoViewFragmentWeakReference = new WeakReference<PhotoViewFragment>(photoViewFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final PhotoViewFragment photoViewFragment = photoViewFragmentWeakReference.get();
            if (photoViewFragment != null) {
                switch (msg.what) {
                    case 1:
                        int percent = msg.arg1*100/msg.arg2;
                        photoViewFragment.progressBar.setProgress(percent);

                        if(progressBar.getVisibility()==View.INVISIBLE)
                            photoViewFragment.progressBar.setVisibility(View.VISIBLE);
                        if (msg.arg1 == msg.arg2) {
                            photoViewFragment.progressBar.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }



}
