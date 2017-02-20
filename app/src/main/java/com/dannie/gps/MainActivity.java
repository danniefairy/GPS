package com.dannie.gps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public TextView output;
    public LocationManager lc;
    public Location currentLocation = null;
    public LocationListener ll;
    //***下面按鈕跟EditText是我加的
    public Button button;
    //***這兩個是讓你放經緯度要用的EditText
    public EditText Latitude,Longitude;
    //首先 小公主加油
    //如果註解前有打上***就代表是我新加的，書上沒有
    //然後註解下面就是接著註解解釋的東西
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.location);

        //取定訂位服務LocationManager物件
        lc = (LocationManager) getSystemService(LOCATION_SERVICE);

        //檢查是否有GPS
        if(!lc.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態是尚未啟用.\n請問您是否現在就設定啟用GPS?")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //使用Intent 物件啟用設定程式來改GPS設定
                            Intent i = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("不啟用",null).create().show();
        }

        //***開啟ACCESS_FINE_LOCATION以及ACCESS_COARSE_LOCATION的permission(權限)
        //***因為在marshmallow的版本在manifest中permission要在程式裡面加上下面這些才能打開permission
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        //***開啟google地圖按鈕
        //***宣告按鈕
        button=(Button)findViewById(R.id.button);
        //***按按鈕後我就可以執行onClick內的動作
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //***而onClick內的動作就是去呼叫button_Click()這個函數
                //***這個函數是用來開啟google map的
                button_Click();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        ll=new MyLocationListener();
        int minTime=1000;//1000毫秒
        float minDistance=1;//1公尺
        //***註冊更新的傾聽者物件
        //***原本書上是打下面這兩行，但因為LocalManager有permission(權限)問題,所以用if過濾掉,看不懂沒關係只要知道我仍然有打書上的那兩行只是放在if內
        //lc.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, ll);
        //lc.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, ll);
        if (lc != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //***小公主我放在這裡喔
                lc.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, ll);
                lc.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, ll);
            }
            else
                Log.d("permission","permission error");
        }
        else
            Log.d("lc","lc=null");
    }

    @Override
    protected void onPause(){
        super.onPause();
        //取消更新的傾聽者物件
        //lc.removeUpdates(ll);
        if (lc != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lc.removeUpdates(ll);
            }
        }
    }

    class MyLocationListener implements LocationListener{
        public void onLocationChanged(Location current){
            double lat,lng;
            if(current != null){
                currentLocation=current;
                //取得經緯度
                lat=current.getLatitude();
                lng=current.getLongitude();
                Toast.makeText(MainActivity.this,"經緯度座標變更......",Toast.LENGTH_SHORT).show();
                output.setText("緯度: "+lat+".\n經度: "+lng);
            }
        }
        public void onProviderDisabled(String provider){}
        public void onProviderEnabled(String provider){}
        public void onStatusChanged(String provider,int status,Bundle extras){}
    }

    //啟動Google地圖
    public void button_Click(){
        //***大概說一下下面這些在幹嘛
        //***一開始先令float型態的變數讓我們可以存經緯度的值
        float latitude;
        float longitude;
        //***先把EditText在layout那邊的經緯度的欄位先指定到下面這兩個東西內(還記得之前舉的兩邊總統的例子嗎)
        Latitude=(EditText)findViewById(R.id.latitude);
        Longitude=(EditText)findViewById(R.id.longitude);
        //***下面就是說如果那些空格內有東西我就取出來,不然就抓現在位置
        //***要取出EditText的方法就是Latitude.getText()然後為了要變成字串String 所以後面加上.toString()
        //***而要辨認是不是空的就用.equals("")
        //***.equals()這個東西就是接在你要的字串後面,如果String s="123", 然後你打s.equals("123")他就會回傳true如果不是就回傳false
        //***所以我裡面什麼都沒打("")那就代表我要辨別是不是空的
        if(Latitude.getText().toString().equals("")||Longitude.getText().toString().equals(""))
        {
            //取得目前經緯度座標
            //***這應該是書上有寫喔
            //***只是我只有當EditText兩個有沒輸入時才抓現在位置,不然都會把上面先假設好的float型態變數輸入這些值喔
            latitude=(float)currentLocation.getLatitude();
            longitude=(float)currentLocation.getLongitude();
        }
        else
        {
            latitude=Float.valueOf(Latitude.getText().toString());
            longitude=Float.valueOf(Longitude.getText().toString());
        }
        //建立URI字串
        //***用問號?隔開z代表zoom也就是放大多少的參數,z不一樣一開始開地圖的大小比例尺也會不一樣喔,小公主可以把18改成別的試試看,如果不要就把?z=18都去掉只剩下geo:%f,%f也是可以喔
        //***下面這個%f %f只是把後面latitude longitude的值取代這些%f %f的位置
        //***例如latitude的值是1.23 longitude的值是4.56 uri就會是String的型態,並且是"geo:1.23,4.56?z=18"
        String uri=String.format("geo:%f,%f?z=18",latitude,longitude);
        //建立Intent物件
        //***這裡應該就是小公主上刻有教過的了
        Intent geoMap=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(geoMap);//***啟動活動,
        ///***小公主晚安 我愛你
    }

}
