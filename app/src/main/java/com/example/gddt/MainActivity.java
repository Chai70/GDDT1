package com.example.gddt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * ?????????
 *
 * @author llw
 */
public class MainActivity extends AppCompatActivity implements
        AMapLocationListener, LocationSource, PoiSearch.OnPoiSearchListener,
        AMap.OnMapClickListener, AMap.OnMapLongClickListener,
        GeocodeSearch.OnGeocodeSearchListener, EditText.OnKeyListener,
        AMap.OnMarkerClickListener, AMap.OnMarkerDragListener,
        AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener {

    //???????????????
    private static final int REQUEST_PERMISSIONS = 9527;
    private static final String TAG = "MainActivity";

    //??????AMapLocationClient?????????
    public AMapLocationClient mLocationClient = null;
    //??????AMapLocationClientOption??????
    public AMapLocationClientOption mLocationOption = null;

    private MapView mapView;

    //???????????????
    private AMap aMap = null;
    //??????????????????
    private OnLocationChangedListener mListener;
    //????????????
    private MyLocationStyle myLocationStyle = new MyLocationStyle();

    //????????????UiSettings??????
    private UiSettings mUiSettings;

    //POI????????????
    private PoiSearch.Query query;
    //POI????????????
    private PoiSearch poiSearch;
    //?????????
    private String cityCode = null;
    //????????????
    private FloatingActionButton fabPOI;

    //??????????????????
    private GeocodeSearch geocodeSearch;
    //?????????????????????
    private static final int PARSE_SUCCESS_CODE = 1000;

    //?????????
    private EditText etAddress;

    //????????????  ??????????????????
    private FloatingActionButton fabClearMarker;

    //????????????
    private List<Marker> markerList = new ArrayList<>();

    private int index = 0;

    private boolean isFirst = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabPOI = findViewById(R.id.fab_poi);
        fabClearMarker = findViewById(R.id.fab_clear_marker);

        etAddress = findViewById(R.id.et_address);
        //??????????????????
        etAddress.setOnKeyListener(this);

        //???????????????
        initLocation();

        //???????????????
        initMap(savedInstanceState);

        //??????Android??????
        checkingAndroidVersion();
    }

    /**
     * ???????????????
     */
    private void initLocation() {

        //???????????????
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //????????????????????????
        mLocationClient.setLocationListener(this);
        //?????????AMapLocationClientOption??????
        mLocationOption = new AMapLocationClientOption();
        //?????????????????????AMapLocationMode.Hight_Accuracy?????????????????????
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //???????????????????????????,?????????false
        mLocationOption.setOnceLocation(false);
        //?????????????????????
        mLocationOption.setInterval(1000);

        //????????????????????????????????????????????????????????????
        mLocationOption.setNeedAddress(true);
        //?????????????????????????????????????????????????????????30000???????????????????????????????????????8000?????????
        mLocationOption.setHttpTimeOut(20000);
        //??????????????????????????????????????????????????????
        mLocationOption.setLocationCacheEnable(false);
        //??????????????????????????????????????????
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();//????????????
    }

    /**
     * ???????????????
     *
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        //???activity??????onCreate?????????mMapView.onCreate(savedInstanceState)???????????????
        mapView.onCreate(savedInstanceState);
        //??????????????????????????????
        aMap = mapView.getMap();

        //???????????????????????????16 ????????????????????????[3, 20]
        aMap.setMinZoomLevel(16);
        //??????????????????
        aMap.showIndoorMap(true);
        //?????????UiSettings?????????
        mUiSettings = aMap.getUiSettings();
        //?????????????????? ????????????
        mUiSettings.setZoomControlsEnabled(false);
        //??????????????? ???????????????
        mUiSettings.setScaleControlsEnabled(true);
        // ???????????????????????????
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        // ??????????????????????????????????????????  ??????0?????????
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // ??????????????????????????????????????????  0 ?????????
        myLocationStyle.strokeWidth(0);
        // ???????????????????????????  ??????0?????????
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //?????????????????????Style
        aMap.setMyLocationStyle(myLocationStyle);
        // ??????????????????
        aMap.setLocationSource(this);
        // ?????????true??????????????????????????????????????????false??????????????????????????????????????????????????????false
        aMap.setMyLocationEnabled(true);
        //????????????????????????
        aMap.setOnMapClickListener(this);
        //????????????????????????
        aMap.setOnMapLongClickListener(this);
        //????????????Marker????????????
        aMap.setOnMarkerClickListener(this);
        //????????????Marker????????????
        aMap.setOnMarkerDragListener(this);
        //??????InfoWindowAdapter??????
        aMap.setInfoWindowAdapter(this);
        //??????InfoWindow????????????
        aMap.setOnInfoWindowClickListener(this);
        //?????? GeocodeSearch ??????
        geocodeSearch = new GeocodeSearch(this);
        //????????????
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * ??????Android??????
     */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android6.0?????????????????????????????????
            requestPermission();
        } else {
            //Android6.0??????????????????
            //????????????
            mLocationClient.startLocation();
        }
    }

    /**
     * ??????????????????
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
            //true ????????? ????????????
            //showMsg("????????????????????????????????????");
            Log.d(TAG, "????????????????????????????????????");
            //????????????
            mLocationClient.startLocation();
        } else {
            //false ?????????
            EasyPermissions.requestPermissions(this, "????????????", REQUEST_PERMISSIONS, permissions);
        }
    }

    /**
     * ??????????????????
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //????????????????????????
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Toast??????
     *
     * @param msg ????????????
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * ?????????????????????????????????
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);
                //??????
                String address = aMapLocation.getAddress();
                //????????????
                double latitude = aMapLocation.getLatitude();
                //????????????
                double longitude = aMapLocation.getLongitude();

                showMsg("?????????"+latitude+",?????????"+longitude);


                Log.d("MainActivity", aMapLocation.getCity());
                showMsg(address);

                //??????????????????????????????????????????????????????
                mLocationClient.stopLocation();

                //????????????????????????
                if (mListener != null) {
                    // ??????????????????
                    mListener.onLocationChanged(aMapLocation);
                }

                //??????????????????
                fabPOI.show();
                //??????
                cityCode = aMapLocation.getCityCode();
            } else {
                //???????????????????????????ErrCode????????????????????????????????????????????????errInfo???????????????????????????????????????
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
        }

    /**
     * ????????????
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            //?????????????????????
            mLocationOption = new AMapLocationClientOption();
            //????????????????????????
            mLocationClient.setLocationListener(this);
            //??????????????????????????????
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //??????????????????,????????????,?????????2000ms
            mLocationOption.setInterval(2000);
            //????????????????????????????????????????????????????????????
            mLocationOption.setNeedAddress(true);
            //??????????????????
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();//????????????
        }
    }

    /**
     * ????????????
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //?????????????????????????????????????????????????????????
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        //???activity??????onDestroy?????????mMapView.onDestroy()???????????????
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //???activity??????onResume?????????mMapView.onResume ()???????????????????????????
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //???activity??????onPause?????????mMapView.onPause ()????????????????????????
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //???activity??????onSaveInstanceState?????????mMapView.onSaveInstanceState (outState)??????????????????????????????
        mapView.onSaveInstanceState(outState);
    }

    /**
     * POI????????????
     *
     * @param poiResult POI????????????
     * @param i
     */
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //??????result??????POI??????

        //??????POI????????????
        ArrayList<PoiItem> poiItems = poiResult.getPois();
        for (PoiItem poiItem : poiItems) {
            Log.d("MainActivity", " Title???" + poiItem.getTitle() + " Snippet???" + poiItem.getSnippet());
        }
    }

    /**
     * POI????????????????????????
     *
     * @param poiItem ??????POI item
     * @param i
     */
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * ??????????????????????????????POI
     *
     * @param view
     */
    public void queryPOI(View view) {
        //??????query??????
        query = new PoiSearch.Query("??????", "", cityCode);
        // ?????????????????????????????????poiitem
        query.setPageSize(10);
        //??????????????????
        query.setPageNum(1);
        //?????? PoiSearch ??????
        poiSearch = new PoiSearch(this, query);
        //????????????????????????
        poiSearch.setOnPoiSearchListener(this);
        //??????????????????POI????????????
        poiSearch.searchPOIAsyn();
    }

    /**
     * ??????????????????
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //???????????????????????????
        //latlonToAddress(latLng);

        //????????????
        addMarker(latLng);
        //?????????????????????
        updateMapCenter(latLng);
    }

    /**
     * ????????????????????????
     * @param latLng ??????
     */
    private void updateMapCenter(LatLng latLng) {
        // CameraPosition ?????????????????? ????????????????????????????????????????????????
        // CameraPosition ?????????????????? ?????????????????????????????????
        // CameraPosition ?????????????????? ??????????????????????????????????????????????????????
        // CameraPosition ?????????????????? ??????????????????????????????????????????????????????????????????????????????????????????0??????360???
        CameraPosition cameraPosition = new CameraPosition(latLng, 16, 30, 0);
        //????????????
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        //????????????
        //aMap.moveCamera(cameraUpdate);
        //??????????????????
        aMap.animateCamera(cameraUpdate);

    }





    /**
     * ??????????????????
     *
     * @param latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        //???????????????????????????
        latlonToAddress(latLng);
    }

    /**
     * ??????????????????
     *
     * @param latLng
     */
    private void addMarker(LatLng latLng) {
        //??????????????????
        fabClearMarker.show();
        //????????????
        Marker marker = aMap.addMarker(new MarkerOptions()
                .draggable(true)//?????????
                .position(latLng)
                .title("??????")
                .snippet("????????????"));

        //??????Marker?????????InfoWindow
        //marker.showInfoWindow();

        //?????????????????????????????????
        Animation animation = new RotateAnimation(marker.getRotateAngle(), marker.getRotateAngle() + 180, 0, 0, 0);
        long duration = 1000L;
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());

        marker.setAnimation(animation);
        marker.startAnimation();

        markerList.add(marker);
    }

    /**
     * ???????????????????????????
     *
     * @param latLng
     */
    private void latlonToAddress(LatLng latLng) {
        //?????????  ???????????????????????????
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        //???????????????  ???????????????????????????Latlng????????????????????????????????????????????????????????????????????????????????????GPS???????????????
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
        //????????????????????????
        geocodeSearch.getFromLocationAsyn(query);
    }

    /**
     * ???????????????
     *
     * @param regeocodeResult
     * @param rCode
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        //??????result????????????????????????
        if (rCode == PARSE_SUCCESS_CODE) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            //????????????????????????
            Log.d("MainActivity", regeocodeAddress.getFormatAddress());
            //showMsg("?????????" + regeocodeAddress.getFormatAddress());

            LatLonPoint latLonPoint = regeocodeResult.getRegeocodeQuery().getPoint();
            LatLng latLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
            addMarker(latLng);
        } else {
            showMsg("??????????????????");
        }

    }

    /**
     * ???????????????
     *
     * @param geocodeResult
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode == PARSE_SUCCESS_CODE) {
            List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
            if (geocodeAddressList != null && geocodeAddressList.size() > 0) {
                LatLonPoint latLonPoint = geocodeAddressList.get(0).getLatLonPoint();
                //????????????????????????
                showMsg("?????????" + latLonPoint.getLongitude() + "???" + latLonPoint.getLatitude());
            }

        } else {
            showMsg("??????????????????");
        }
    }

    /**
     * ????????????
     *
     * @param v
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            //?????????????????????
            String address = etAddress.getText().toString().trim();
            if (address == null || address.isEmpty()) {
                showMsg("???????????????");
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //???????????????
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                // name??????????????????????????????????????????????????????????????????????????????citycode???adcode
                GeocodeQuery query = new GeocodeQuery(address, "??????");
                geocodeSearch.getFromLocationNameAsyn(query);
            }
            return true;
        }
        return false;
    }

    /**
     * ????????????Marker
     *
     * @param view
     */
    public void clearAllMarker(View view) {
        if (markerList != null && markerList.size() > 0) {
            for (Marker markerItem : markerList) {
                markerItem.remove();
            }
        }
        fabClearMarker.hide();
    }

    /**
     * Marker????????????
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        //showMsg("???????????????");
        //??????InfoWindow
        if (!marker.isInfoWindowShown()) {
            //??????
            marker.showInfoWindow();
        } else {
            //??????
            marker.hideInfoWindow();
        }
        return true;
    }

    /**
     * ????????????
     *
     * @param marker
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "????????????");
    }

    /**
     * ?????????
     *
     * @param marker
     */
    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "?????????");
    }

    /**
     * ????????????
     *
     * @param marker
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "????????????");
    }

    /**
     * ????????????
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = getLayoutInflater().inflate(
                R.layout.custom_info_contents, null);
        render(marker, infoContent);
        return infoContent;
    }

    /**
     * ????????????
     *
     * @param marker
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getLayoutInflater().inflate(
                R.layout.custom_info_window, null);

        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * ??????
     *
     * @param marker
     * @param view
     */
    private void render(Marker marker, View view) {
        ((ImageView) view.findViewById(R.id.badge))
                .setImageResource(R.drawable.icon_yuan);

        //??????InfoWindow??????????????????
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    titleText.length(), 0);
            titleUi.setTextSize(15);
            titleUi.setText(titleText);

        } else {
            titleUi.setText("");
        }
        //??????InfoWindow??????????????????
        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
                    snippetText.length(), 0);
            snippetUi.setTextSize(20);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
    }

    /**
     * InfoWindow????????????
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        showMsg("????????????????????????" + marker.getTitle() + "\n?????????" + marker.getSnippet());
    }

    /**
     * ??????????????????
     * @param view
     */
    public void jumpRouteActivity(View view) {
        startActivity(new Intent(this,RouteActivity.class));
    }
}
