package com.gongzetao.loop.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/10/6.
 */
public class ApplicationAttestationActivity extends BaseActivity {
    ImageView iv_last;
    TextView tv_title;
    EditText et_attestation;
    EditText et_name;
    EditText et_certificate;
    EditText et_phoneNumber;
    ImageView iv_pictureLeft;
    ImageView iv_pictureRight;
    TextView tv_ok;

    String attestation;
    String name;
    String certificateNumber;
    String phoneNumber;
    List<String> pictureLocalUrls;
    List<String> pictureServerUrls;

    int leftPicture = 0;
    int rightPicture = 1;
    int clickPictureState = leftPicture;

    List<PhotoInfo> mPhotoList;
    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                mPhotoList = new ArrayList<PhotoInfo>();
                mPhotoList.addAll(resultList);
                pictureLocalUrls = new ArrayList<String>();
                for (int i = 0; i < mPhotoList.size(); i++) {
                    pictureLocalUrls.add(mPhotoList.get(i).getPhotoPath());
                }
                if (clickPictureState == leftPicture)
                    Glide.with(ApplicationAttestationActivity.this).
                            load(mPhotoList.get(0).getPhotoPath()).into(iv_pictureLeft);
                if (clickPictureState == rightPicture)
                    Glide.with(ApplicationAttestationActivity.this).
                            load(mPhotoList.get(0).getPhotoPath()).into(iv_pictureRight);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            ToastUtils.showToast(ApplicationAttestationActivity.this, "选择图片资源失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_attestation);
        initUI();
        initListener();
    }

    private void initListener() {
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeInfo()) {
                    return;
                }
                attestation = et_attestation.getText().toString().trim();
                name = et_name.getText().toString().trim();
                certificateNumber = et_certificate.getText().toString().trim();
                phoneNumber = et_phoneNumber.getText().toString().trim();
                if (pictureLocalUrls != null && pictureLocalUrls.size() > 0) {
                    uploadPicture();
                } else {
                    uploadAttestation();
                }
            }
        });

        iv_pictureLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPictureState = leftPicture;
                MediumUtils.openPhoto(ApplicationAttestationActivity.this,
                        mOnHanlderResultCallback, mPhotoList, 1);
            }
        });
        iv_pictureRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPictureState = rightPicture;

                MediumUtils.openPhoto(ApplicationAttestationActivity.this,
                        mOnHanlderResultCallback, mPhotoList, 1);
            }
        });
    }

    private void uploadAttestation() {
            AVUser user = MyApplication.me;
            user.put(User.str_attestation, attestation);
            user.setUsername(name);
            user.put(User.str_certificate_number, certificateNumber);
            user.put(User.str_phone_number, phoneNumber);
            if (pictureServerUrls != null)
                user.put(User.str_certificate_picture, pictureServerUrls);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        ToastUtils.showToast(ApplicationAttestationActivity.this, "申请提交成功，请耐心等待");
                    }
                }
            });

    }

    private boolean judgeInfo() {
        if (TextUtils.isEmpty(attestation)) {
            ToastUtils.showToast(this, "请填写认证头衔");
            return true;
        }
        if (name == null || "".equals(name)) {
            ToastUtils.showToast(this, "请填写真实姓名");
            return true;
        }
        if (certificateNumber == null || "".equals(certificateNumber)) {
            ToastUtils.showToast(this, "请填写证件号码");
            return true;
        }
        if (phoneNumber == null || "".equals(phoneNumber)) {
            ToastUtils.showToast(this, "请填写电话号码");
            return true;
        }
        return false;
    }

    private void uploadPicture() {
        if (pictureServerUrls == null)
            pictureServerUrls = new ArrayList<>();
        if (pictureLocalUrls != null) {
            for (int i = 0; i < pictureLocalUrls.size(); i++) {
                AVFile file = null;
                try {
                    File localFile = new File(pictureLocalUrls.get(i));
                    file = AVFile.withFile(localFile.getName(), localFile);
                } catch (FileNotFoundException e) {
                    ToastUtils.showToast(this, "图片资源错误，请重新选择");
                    return;
                }
                if (file != null) {
                    final AVFile finalFile = file;
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            pictureServerUrls.add(finalFile.getUrl());//将上传图片后返回的URL地址进行保存
                            if (pictureServerUrls.size() == pictureLocalUrls.size()) {
                                uploadAttestation();
                            }
                        }
                    });
                }
            }
        }

    }

    private void initUI() {
        iv_last = (ImageView) findViewById(R.id.title_icon_last);
        tv_title = (TextView) findViewById(R.id.title_tv_title);
        tv_title.setText("个人认证");
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        et_attestation = (EditText) findViewById(R.id.application_attestation_et_attestation);
        et_name = (EditText) findViewById(R.id.application_attestation_et_name);
        et_certificate = (EditText) findViewById(R.id.application_attestation_et_certificate_number);
        et_phoneNumber = (EditText) findViewById(R.id.application_attestation_et_phone_number);
        iv_pictureLeft = (ImageView) findViewById(R.id.application_attestation_iv_picture_left);
        iv_pictureRight = (ImageView) findViewById(R.id.application_attestation_iv_picture_right);
        tv_ok = (TextView) findViewById(R.id.application_attestation_tv_ok);
    }

}
