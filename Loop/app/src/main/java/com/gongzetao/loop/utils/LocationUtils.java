package com.gongzetao.loop.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.nearby.NearbyInfo;
import com.amap.api.services.nearby.NearbySearch;
import com.amap.api.services.nearby.NearbySearchFunctionType;
import com.amap.api.services.nearby.NearbySearchResult;
import com.amap.api.services.nearby.UploadInfo;
import com.amap.api.services.nearby.UploadInfoCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.bean.UserPosition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LocationUtils implements NearbySearch.NearbyListener, AMapLocationListener {

    public final static int SEARCH_RESULT_NULL = 1000;
    public final static int SEARCH_RESULT_ERROR = 1001;
    public final static double EARTH_RADIUS_KM = 6378.137;//地球半径
    public int SEARCH = 0;
    public int UP_ONCE = 1;
    public int UP_REPEAT = 2;
    public int state = -1;

    double latitude;//纬度
    double longitude;//经度

    //    String mail;
    Context context;
    SearchSucceedListener searchSucceedListener;
    LocationSucceedListener locationSucceedListener;

    NearbySearch mNearbySearch;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    public void setSearchSucceedListener(SearchSucceedListener searchSucceedListener) {
        this.searchSucceedListener = searchSucceedListener;
    }

    public void setLocationSucceedListener(LocationSucceedListener locationSucceedListener) {
        this.locationSucceedListener = locationSucceedListener;
    }

    public LocationUtils(Context context) {
        this.context = context;
//        initocation();
        /**
         * 在使用位置信息上传和周边位置信息检索之前，需要对功能模块进行初始化操作。
         */
        mNearbySearch = NearbySearch.getInstance(context);
    }


    //初始化位置参数
    public void startLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(context);
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        //设置是否强制刷新WIFI，默认为true，强制刷新。
        mLocationOption.setWifiActiveScan(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    /**
     * 获取位置回调监听
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                latitude = aMapLocation.getLatitude();//获取纬度
                longitude = aMapLocation.getLongitude();//获取经度
                LogUtils.MyLog("定位：" + "经度：" + longitude + "纬度：" + latitude);
                LogUtils.MyLog("街道  " + aMapLocation.getStreet());
                LogUtils.MyLog("城区  " + aMapLocation.getDistrict());
                LogUtils.MyLog("街道门牌号  " + aMapLocation.getStreetNum());
                LogUtils.MyLog("getPoiName  " + aMapLocation.getPoiName());
                LogUtils.MyLog("getAoiName  " + aMapLocation.getAoiName());
                if (locationSucceedListener != null)
                    locationSucceedListener.localSucceed(aMapLocation);

//                aMapLocation.getCountry();//国家信息
//                aMapLocation.getProvince();//省信息
//                aMapLocation.getCity();//城市信息
//                aMapLocation.getDistrict();//城区信息
//                aMapLocation.getStreet();//街道信息
//                aMapLocation.getStreetNum();//街道门牌号信息
//                aMapLocation.getCityCode();//城市编码
//                aMapLocation.getAdCode();//地区编码
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                LogUtils.MyLog("location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                locationSucceedListener.localFail();
            }
        }
    }

    //    public static String sHA1(Context context) {
    //        try {
    //            PackageInfo info = context.getPackageManager().getPackageInfo(
    //                    context.getPackageName(), PackageManager.GET_SIGNATURES);
    //            byte[] cert = info.signatures[0].toByteArray();
    //            MessageDigest md = MessageDigest.getInstance("SHA1");
    //            byte[] publicKey = md.digest(cert);
    //            StringBuffer hexString = new StringBuffer();
    //            for (int i = 0; i < publicKey.length; i++) {
    //                String appendString = Integer.toHexString(0xFF & publicKey[i])
    //                        .toUpperCase(Locale.US);
    //                if (appendString.length() == 1)
    //                    hexString.append("0");
    //                hexString.append(appendString);
    //                hexString.append(":");
    //            }
    //            String result = hexString.toString();
    //            return result.substring(0, result.length()-1);
    //        } catch (PackageManager.NameNotFoundException e) {
    //            e.printStackTrace();
    //        } catch (NoSuchAlgorithmException e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }


    /**
     * 连续上传位置信息
     */
    public void repeatUp(final String userId, double lat, double lon) {
        final LatLonPoint latLonPoint = new LatLonPoint(lat, lon);
        mNearbySearch.startUploadNearbyInfoAuto(new UploadInfoCallback() {
            //设置自动上传数据和上传的间隔时间
            @Override
            public UploadInfo OnUploadInfoCallback() {
                UploadInfo loadInfo = new UploadInfo();
                loadInfo.setCoordType(NearbySearch.AMAP);
                //位置信息
                loadInfo.setPoint(latLonPoint);
                //用户id信息
                loadInfo.setUserID(StringUtils.encode(userId));
                return loadInfo;
            }
        }, 1000 * 60 * 10);//每隔十分钟上传一次位置信息
    }

    public void myUpPosition(final AVUser user, final double lat, final double lon) {
        if (user == null)
            return;
        AVQuery<AVObject> query = new AVQuery<>(UserPosition.userPosition);
        query.whereEqualTo(UserPosition.userid, user.getEmail());
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null || list.size() == 0) {
                    AVObject object = new AVObject(UserPosition.userPosition);
                    object.put(UserPosition.userid, user.getEmail());
                    object.put(UserPosition.userLon, lon);
                    object.put(UserPosition.userLat, lat);
                    object.saveInBackground();
                    return;
                }
                list.get(0).put(UserPosition.userLon, (double) lon);
                list.get(0).put(UserPosition.userLat, (double) lat);
                list.get(0).saveInBackground();

            }
        });
    }

    /**
     * 搜索我附近的商家
     * @param lon
     * @param lat
     */
    public void mySearchBusiness(final double lon, final double lat) {
        AVQuery<AVObject> query = new AVQuery(UserPosition.userPosition);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    ArrayList<String> result = new ArrayList<String>();
                    for(int i = 0; i < list.size(); i++) {
                        if (getDistance(lon, lat, ((BigDecimal) list.get(i).get(UserPosition.userLon)).doubleValue(), ((BigDecimal) list.get(i).get(UserPosition.userLat)).doubleValue()) < 2) {
                            result.add((String) list.get(i).get(UserPosition.userid));
                        }
                    }
                    if (searchSucceedListener != null)
                        searchSucceedListener.searchSucceed(result);
                } else {
                    if (searchSucceedListener != null)
                        searchSucceedListener.searchFail(SEARCH_RESULT_ERROR);
                }
            }
        });
    }

    /**
     * 搜索我附近的好友
     * @param lon
     * @param lat
     */
    public void mySearchUser(final double lon, final double lat) {
        AVQuery<AVObject> query = new AVQuery(UserPosition.userPosition);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    ArrayList<String> result = new ArrayList<String>();
                    for(int i = 0; i < list.size(); i++) {
                        if (getDistance(lon, lat, ((BigDecimal) list.get(i).get(UserPosition.userLon)).doubleValue(), ((BigDecimal) list.get(i).get(UserPosition.userLat)).doubleValue()) < 2) {
                            result.add((String) list.get(i).get(UserPosition.userid));
                        }
                    }
                    if (searchSucceedListener != null)
                        searchSucceedListener.searchSucceed(result);
                } else {
                    if (searchSucceedListener != null)
                        searchSucceedListener.searchFail(SEARCH_RESULT_ERROR);
                }
            }
        });
    }

    /**
     * 单次上传位置信息，上传时注明用户Id账号。
     */
    public void oneUp(final String userId, double lat, double lon) {
        LatLonPoint latLonPoint = new LatLonPoint(lat, lon);
        //构造上传位置信息
        UploadInfo loadInfo = new UploadInfo();
        //设置上传位置的坐标系支持AMap坐标数据与GPS数据
        loadInfo.setCoordType(NearbySearch.AMAP);
        //设置上传数据位置,位置的获取推荐使用高德定位sdk进行获取
        loadInfo.setPoint(latLonPoint);
        //设置上传用户id
        loadInfo.setUserID(StringUtils.encode(userId));
        //调用异步上传接口
        mNearbySearch.uploadNearbyInfoAsyn(loadInfo);
        LogUtils.MyLog("单次上传启动");
    }

    /**
     * 开启周边检索
     * 周边检索,此搜索中心是划定范围的指定中心，
     * 当前位置属于那个中心所化范围，则以此为中心
     */
    public void startSearchNearUser(double lon, double lat) {
        //设置搜索条件
        NearbySearch.NearbyQuery query = new NearbySearch.NearbyQuery();
        //设置搜索的中心点
        query.setCenterPoint(new LatLonPoint(lat, lon));
        //设置搜索的坐标体系
        query.setCoordType(NearbySearch.AMAP);
        //设置搜索半径
        query.setRadius(1000);
        //设置查询的时间
        query.setTimeRange(3000);
        //设置查询的方式驾车还是距离
        query.setType(NearbySearchFunctionType.DRIVING_DISTANCE_SEARCH);
        mNearbySearch.addNearbyListener(this);

        //调用异步查询接口，开启周边检索。
        mNearbySearch.searchNearbyInfoAsyn(query);
        LogUtils.MyLog("搜周边方法启动");
    }


    @Override
    public void onUserInfoCleared(int i) {
    }

    /**
     * 周边检索的回调函数，通过搜索得到的好友Id进行查询显示好友所发的说说。
     *
     * @param nearbySearchResult
     * @param resultCode
     */
    @Override
    public void onNearbyInfoSearched(NearbySearchResult nearbySearchResult,
                                     int resultCode) {
        LogUtils.MyLog("搜周边方法回调哦");
        //搜索周边附近用户回调处理
        if (resultCode == 1000) {
            if (nearbySearchResult != null
                    && nearbySearchResult.getNearbyInfoList() != null
                    && nearbySearchResult.getNearbyInfoList().size() > 0) {
                List<NearbyInfo> nearbyInfoList = nearbySearchResult.getNearbyInfoList();

                //得到所有的用户mail
                List<String> userMail = new ArrayList<String>();
                for (int i = 0; i < nearbyInfoList.size(); i++) {
                    userMail.add(StringUtils.unEncode(nearbyInfoList.get(i).getUserID()));
                }
                //调用接口
                if (searchSucceedListener != null)
                    searchSucceedListener.searchSucceed(userMail);

                LogUtils.MyLog("结果个数  ：" + nearbySearchResult.getNearbyInfoList().size());

                LogUtils.MyLog("结果1  ：" + nearbySearchResult.getNearbyInfoList().get(0).getUserID());
                LogUtils.MyLog("结果1距离  ：" + nearbySearchResult.getNearbyInfoList().get(0).getDistance());
                LogUtils.MyLog("结果1坐标  ：" + nearbySearchResult.getNearbyInfoList().get(0).getPoint().toString());

                LogUtils.MyLog("结果2  ：" + nearbySearchResult.getNearbyInfoList().get(1).getUserID());
                LogUtils.MyLog("结果2距离  ：" + nearbySearchResult.getNearbyInfoList().get(1).getDistance());
                LogUtils.MyLog("结果2坐标  ：" + nearbySearchResult.getNearbyInfoList().get(1).getPoint().toString());


//                Log.e("qwer","周边搜索结果为size " + nearbySearchResult.getNearbyInfoList().size() + "first：" +
//                        ""+ nearbyInfo.getUserID() + "     " + nearbyInfo.getDistance()+ "     "
//                        + nearbyInfo.getDrivingDistance() + "  " + nearbyInfo.getTimeStamp() + "  " +
//                        nearbyInfo.getPoint().toString());
            } else {
                LogUtils.MyLog("周边搜索结果为空");
                if (searchSucceedListener != null)
                    searchSucceedListener.searchFail(SEARCH_RESULT_NULL);

            }
        } else {
            LogUtils.MyLog("周边搜索出现异常，异常码为：" + resultCode);
            if (searchSucceedListener != null)
                searchSucceedListener.searchFail(SEARCH_RESULT_ERROR);
        }
    }

    public interface LocationSucceedListener {


        /**
         * 定位成功回调接口
         *
         * @param aMapLocation
         */
        void localSucceed(AMapLocation aMapLocation);

        void localFail();
    }

    public interface SearchSucceedListener {
        /**
         * 周边搜索成功回调接口
         *
         * @param userMail
         */
        void searchSucceed(List<String> userMail);

        void searchFail(int state);

    }

    @Override
    public void onNearbyInfoUploaded(int i) {

    }


    /**
     * 根据经纬度计算地球上任意两点间的距离
     *
     * @param lng1 起点经度
     * @param lat1 起点纬度
     * @param lng2 终点经度
     * @param lat2 终点纬度
     * @return 两点距离（单位：千米）
     */
    public static double getDistance(double lng1, double lat1, double lng2,
                                     double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLng1 = Math.toRadians(lng1);
        double radLng2 = Math.toRadians(lng2);
        double deltaLat = radLat1 - radLat2;
        double deltaLng = radLng1 - radLng2;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(
                Math.sin(deltaLat / 2), 2)
                + Math.cos(radLat1)
                * Math.cos(radLat2)
                * Math.pow(Math.sin(deltaLng / 2), 2)));
        distance = distance * EARTH_RADIUS_KM;
        distance = Math.round(distance * 10000) / 10000;
        return distance;
    }

    /**
     *用户信息清除后，将不会再被检索到，比如接单的美甲师下班后可以清除其位置信息。
     */
//    public void fun3(){
//        //获取附近实例，并设置要清楚用户的id
//        mNearbySearch.setUserID("8888");
//        //调用异步清除用户接口
//        mNearbySearch.clearUserInfoAsyn();
//    }


    /**
     * 在停止使用附近派单功能时，需释放资源。
     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        NearbySearch.destroy();
//    }

}
