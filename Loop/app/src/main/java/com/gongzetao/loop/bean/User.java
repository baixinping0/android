package com.gongzetao.loop.bean;

/**
 * Created by baixinping on 2016/8/18.
 */
public class User {
    public static final String user = "user";
    public static final String str_mail = "email";
    public static final String str_accountName = "accountName";
    public static final String str_password = "password";
    public static final String str_table = "table";
    public static final String str_photo = "photo";
    public static final String str_sex = "sex";
    public static final String str_born_time = "bornTime";
    public static final String str_phone_number = "phone";
    public static final String str_main_pager_background = "mainPagerBackground";

    public static final String str_attestation = "attestation";
    public static final String str_attestation_true = "attestationTrue";
    public static final String str_my_attention = "myAttention";
    public static final String str_attention_me = "attentionMe";

    public static final String str_certificate_number = "certificateNumber";
    public static final String str_certificate_picture = "certificatePicture";
    public static final String str_category = "category";

    public  static final String[] labelName = new String[]
            {"其他", "追剧", "动漫", "游戏", "爱豆", "活动", "男神",
                    "体育", "电影", "音乐", "学习", "艺术", "出售", "出租","",""};

    public String mail;
    public String name;
    public String password;
    public String table;
    public String photo;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
