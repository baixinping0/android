package com.gongzetao.loop.bean;

import java.util.List;

/**
 * Created by baixinping on 2016/8/12.
 */
public class FriendInfo {

    List<FriendInfoContent> content;

    public List<FriendInfoContent> getContent() {
        return content;
    }

    public class FriendInfoContent{
        String description;
        List<Info> friendsList;

        public String getDescription() {
            return description;
        }

        public List<Info> getFriendsList() {
            return friendsList;
        }
    }

    public class Info{
         String name;
         String mail;
         String photo;

        public String getName() {
            return name;
        }

        public String getMail() {
            return mail;
        }

        public String getPhoto() {
            return photo;
        }
    }
}
