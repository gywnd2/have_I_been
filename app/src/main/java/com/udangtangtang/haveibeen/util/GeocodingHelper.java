package com.udangtangtang.haveibeen.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class GeocodingHelper {
    private Context context;
    private Geocoder geocoder;
    private List<Address> addressList;

    // AdminArea -> 특별, 광역시/도
    // Locality -> 시
    // SubLocality -> 구 (특별 / 광역시의 구 포함)
    // Thoroughfare -> 읍/면/동/로
    private String[] address;

    public GeocodingHelper(Context mContext, double latitude, double longtitude) {
        context = mContext;

        // 역 지오코딩
        geocoder = new Geocoder(context);
        address=new String[4];
        addressList = null;
        try {
            // Geocoder를 통해 주소 획득
            addressList = geocoder.getFromLocation(latitude, longtitude, 10);
        } catch (
                IOException e) {
        }
        if (addressList != null) {
            if (addressList.size() == 0) {
                Toast.makeText(context, "해당 지역의 주소를 제공할 수 없습니다.", Toast.LENGTH_LONG).show();
            } else {
                address[0]=addressList.get(0).getAdminArea();
                address[1]=addressList.get(0).getLocality();
                address[2]=addressList.get(0).getSubLocality();
                address[3]=addressList.get(0).getThoroughfare();
            }
        }
    }



    public String getAddress() {
        String result="";
        for(int i=0; i<4; i++){
            // null을 건너 뛰고 각 단위를 이어 붙임
            // ex) 경기도 광명시 null 일직동 => 경기도 광명시 일직동
            //     경기도 안양시 만안구 석수2동 과 달리 구 단위가 없기 때문
            if(address[i]!=null){
                result+=address[i];
                if(i!=3){ result+=" "; }
            }
        }
        return result;
    }
}

