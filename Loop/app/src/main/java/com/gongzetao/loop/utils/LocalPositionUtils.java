package com.gongzetao.loop.utils;

import com.gongzetao.loop.bean.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/9/19.
 */
public class LocalPositionUtils {
    //范围
    public static int scope = 1;
    //西安石油大学坐标
    public static Position universityXiAnOil = new Position(108.654353,34.102355, "西安石油大学");
    //西安邮电大学坐标
    public static Position universityXiAnPost = new Position(108.903384,34.149514 , "西安邮电大学");

    public static List<Position> getPositions(){
        List<Position> positions = new ArrayList<>();
        positions.add(LocalPositionUtils.universityXiAnOil);
        positions.add(LocalPositionUtils.universityXiAnPost);
        return positions;
    }

    public static Position getCurrentPosition(double lon, double lat){
        List<Position> positions = getPositions();
        for (int i = 0; i < positions.size(); i++){
            Position position = positions.get(i);
            if (LocationUtils.getDistance(lon, lat, position.getLon(),
                    position.getLat()) < LocalPositionUtils.scope){
                return position;
            }
        }
        return null;
    }

}
