package com.gongzetao.loop.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.listener.GlidePauseOnScrollListener;

import java.util.List;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ImageLoader;
import cn.finalteam.galleryfinal.PauseOnScrollListener;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/8/19.
 */
public class MediumUtils {
    private static final int REQUEST_CODE_CAMERA = 1000;
    private static final int REQUEST_CODE_GALLERY = 1001;
    public static void openPhoto(Context context, final GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback,
                                 List<PhotoInfo> mPhotoList, int count) {
        //配置主题
        ThemeConfig themeConfig = ThemeConfig.DEFAULT;
        //配置图片加载器
        ImageLoader imageLoader = new GlideImageLoader();

        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        PauseOnScrollListener pauseOnScrollListener = new GlidePauseOnScrollListener(false, true);

        functionConfigBuilder.setMutiSelectMaxSize(count);
//        functionConfigBuilder.setEnableEdit(true);
        functionConfigBuilder.setEnableRotate(true);
        functionConfigBuilder.setRotateReplaceSource(true);
        functionConfigBuilder.setEnableCamera(true);

        functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        final FunctionConfig functionConfig = functionConfigBuilder.build();

        CoreConfig coreConfig = new CoreConfig.Builder(context, imageLoader, themeConfig)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .build();
        GalleryFinal.init(coreConfig);


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final AlertDialog dialog = dialogBuilder.create();

        View view = View.inflate(context, R.layout.layout_open_photo_or_cammer, null);
        TextView tv_camera = (TextView) view.findViewById(R.id.chat_open_camera);
        TextView tv_photo = (TextView) view.findViewById(R.id.chat_open_photo);
        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryFinal.openCamera(REQUEST_CODE_CAMERA, functionConfig, mOnHanlderResultCallback);
                dialog.dismiss();
            }
        });
        tv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, mOnHanlderResultCallback);
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
//        initImageLoader(context);
    }

    /**
     * 初始化图片加载器
     * @param context
     */
//    private static void initImageLoader(Context context) {
//        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
//        config.threadPriority(Thread.NORM_PRIORITY - 2);
//        config.denyCacheImageMultipleSizesInMemory();
//        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
//        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//        config.tasksProcessingOrder(QueueProcessingType.LIFO);
//        config.writeDebugLogs(); // Remove for release app
//        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config.build());
//    }
}
