package com.gongzetao.loop.view.photoBrowser.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.gongzetao.loop.R;

import java.util.ArrayList;
import java.util.List;


public class PhotoViewActivity extends FragmentActivity {


    private ViewPager vp;
    private TextView indexTV;
    int currentPager = 0;


    /**
     * url 地址列表
     */
    public static String  URL_LIST="url_list";

    public static String position = "position";

    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity_photo_view);


        vp = (ViewPager) findViewById(R.id.viewPager);
        indexTV= (TextView) findViewById(R.id.indexTV);
//        indexTitleTV= (TextView) findViewById(R.id.indexTitleTV);

        //获取URL
        imageUrls = getIntent().getStringArrayListExtra(URL_LIST);
        //获取当前页数
        currentPager = getIntent().getIntExtra(position,0);
        Intent intent=getIntent();

        List<Fragment> list=new ArrayList<Fragment>();
        for(int i=0;i<imageUrls.size();i++) {
            list.add(new PhotoViewFragment(imageUrls.get(i)));
        }

        ViewpagerAdapter vpAdapter=new ViewpagerAdapter(getSupportFragmentManager(),list,null);



        vp.setAdapter(vpAdapter);

        vp.setCurrentItem(currentPager);
        updateStatus(currentPager);

        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateStatus(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }


    public void updateStatus(int position){

        this.indexTV.setText(position+1+"/"+imageUrls.size());

//        if(imageTitles!=null){
//
//        	if (position<imageTitles.size()) {
//
//        		this.indexTitleTV.setText(imageTitles.get(position));
//			}else{
//				this.indexTitleTV.setText(imageTitles.get(imageTitles.size()-1));
//			}
//        }
    }



}
