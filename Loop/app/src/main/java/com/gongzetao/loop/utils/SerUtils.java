package com.gongzetao.loop.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by baixinping on 2016/8/27.
 */
public class SerUtils {

    public static void writeObject(Context context, String Name, Object object){
        FileOutputStream stream = null ;
        ObjectOutputStream oos = null;
        try {
            stream = context.openFileOutput(Name, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(stream);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }
    public static Object readObject(Context context, String fileName){
        FileInputStream stream = null;
        ObjectInputStream ois = null;
        Object object = null;
        try {
            stream = context.openFileInput(fileName);
            ois = new ObjectInputStream(stream);
            object = ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }
    public static boolean fileIsExists(String fileName){
        try{
            File f=new File(fileName);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            return false;
        }
        return true;
    }
}
