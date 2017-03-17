package com.gongzetao.loop.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/8/8.
 */
public class MainContentData {
    List<ContentItem> content;

    public List<ContentItem> getContent() {
        return content;
    }

    public class Picture{
        String url;

        public String getUrl() {
            return url;
        }
    }
    public class ContentItem{
        String photo;
        String name;
        String time;
        String text;
        ArrayList<Picture> picture;
        int commentCount;
        int praiseCount;
        int transmitCount;

        public String getPhoto() {
            return photo;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public String getTime() {
            return time;
        }

        public ArrayList<Picture> getPicture() {
            return picture;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public int getPraiseCount() {
            return praiseCount;
        }

        public int getTransmitCount() {
            return transmitCount;
        }
    }
}
