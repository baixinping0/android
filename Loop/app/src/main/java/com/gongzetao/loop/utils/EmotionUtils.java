package com.gongzetao.loop.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by baixinping on 2016/8/24.
 */
public class EmotionUtils {

    public static SpannableString getEmotionContent( final Context context, final TextView tv, String source) {

        //传入的字符串资源
        SpannableString spannableString = new SpannableString(source);
        //获取资源
        Resources res = context.getResources();

        //匹配表情的正则
        String regexEmotion = "\\[([\u4e00-\u9fa5\\w])+\\]" ;
//        String regexEmotion1 ="\\[\u4e00-\u9fa5\\w+\\]";
        Pattern patternEmotion = Pattern. compile(regexEmotion);
        //用正则匹配传入的文字
        Matcher matcherEmotion = patternEmotion.matcher(spannableString);

        while (matcherEmotion.find()) {
            // 获取匹配到的具体字符（表情的名字）
            String key = matcherEmotion.group();
            // 匹配字符串的开始位置
            int start = matcherEmotion.start();
            // 利用表情名字获取到对应的图片
            Integer imgRes = getResource(context,key);
            if (imgRes != null) {
                // 压缩表情图片
                int size = ( int) tv.getTextSize();
                Bitmap bitmap = BitmapFactory.decodeResource(res, imgRes);
                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);

                ImageSpan span = new ImageSpan(context, scaleBitmap);
                spannableString.setSpan(span, start, start + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
            }
        }
        return spannableString;
    }

    /**
     * 获取图片名称获取图片的资源id的方法
     * @param imageName
     * @return
     */
    public static int  getResource(Context context, String imageName) {
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        return resId;
    }

//    public static void weiboEmoji() {
//        StringBuilder sb = new StringBuilder();
//
//        String names = "[羞羞哒甜馨][萌神奥莉][带着微博去旅行][爱红包][拍照][马到成功]→_→[呵呵][嘻嘻][哈哈][爱你][挖鼻屎][吃惊][晕][泪][馋嘴][抓狂][哼][可爱][怒][汗][害羞][睡觉][钱][偷笑][笑cry][doge][喵喵][酷][衰][闭嘴][鄙视][花心][鼓掌][悲伤][思考][生病][亲亲][怒骂][太开心]" +
//                "[懒得理你][右哼哼][左哼哼][嘘][委屈][吐][可怜][打哈气][挤眼][失望][顶][疑问][困][感冒][拜拜][黑线][阴险][打脸][傻眼][互粉][心][伤心][猪头][熊猫][兔子]" ;
//
//        String regexEmoji = "\\[([\u4e00-\u9fa5a-zA-Z0-9])+\\]" ;
//        Pattern patternEmoji = Pattern. compile(regexEmoji);
//        Matcher matcherEmoji = patternEmoji.matcher(names);
//
//        CharacterParser parser = CharacterParser. getInstance();
//        while (matcherEmoji.find()) { // 如果可以匹配到
//            String key = matcherEmoji.group(); // 获取匹配到的具体字符
//
//            String pinyinName = "d_" + parser.getSpelling(key).replace("[" , "" ).replace("]" , "" );
//            sb.append( "emojiMap.put(\"" + key + "\", R.drawable." + pinyinName + ");\n" );
//        }
//        System. out.println(sb.toString());
//    }
}
