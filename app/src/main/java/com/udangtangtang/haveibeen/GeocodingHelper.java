package com.udangtangtang.haveibeen;

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
            if(address[i]!=null){
                result+=address[i];
                if(i!=3){ result+=" "; }
            }
        }
        return result;
    }
}

