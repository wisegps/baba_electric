package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.Gson;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.biz.HttpCarInfo;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.ElectricCarData;
import com.wise.baba.entity.ElectricCarDetail;
import com.wise.baba.entity.ElectricCarView;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.car.CarLocationActivity;
import com.wise.setting.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Wu
 * 电动车
 */
public class FragmentElectricCarInfo  extends Fragment {
	
	static final String TAG = "FragmentElectricCarInfo";

	private HScrollLayout hs_electric_car;//滑动车辆布局
	private AppApplication app;
	/** 当前车在列表中位置 **/
	public int currentIndex = 0;
	private OnCardMenuListener onCardMenuListener;
	public HttpAir httpAir;
	private Handler uiHander = null;//解锁开锁 指令发送 、反馈信息处理
	private HttpCarInfo httpCarInfo;//刷新汽车的经纬度
	CarData carData;
	private String voltage     = "--";
	private String xhlicheng   = "--";
	private String sydianliang = "--";
	private GeoCoder mGeoCoder = null;
	private GeoCoder mGeoCoder_Phone = null;
	// 定位
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView();");
		return inflater.inflate(R.layout.fragment_electric_car_info, container, false);
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		Log.d(TAG, "onActivityCreated();");
		
		super.onActivityCreated(savedInstanceState);
		uiHander = new Handler(handleCallBack);
		httpAir = new HttpAir(this.getActivity(),uiHander);
		httpCarInfo = new HttpCarInfo(this.getActivity(), mHandler);
		app = (AppApplication) getActivity().getApplication();
		carData = new CarData();
		hs_electric_car = (HScrollLayout) getActivity().findViewById(R.id.hs_electric_car);
		hs_electric_car.setOnViewChangeListener(new OnViewChangeListener() {
			
			@Override
			public void OnViewChange(int changedIndex, int duration) {
				if (currentIndex == changedIndex) {
					return;
				}
				currentIndex = changedIndex;
				app.currentCarIndex = changedIndex;

				carData = app.carDatas.get(currentIndex);
				String device_id = carData.getDevice_id();	
				
				Log.d(TAG, "当前车辆:" + currentIndex + "--" + device_id);
				httpCarInfo.requestGps(device_id);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getElectricCarData();//获取数据
						
					}
				}, 200);
				
			}
		});
		
		initView();
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
		mGeoCoder_Phone = GeoCoder.newInstance();
		mGeoCoder_Phone.setOnGetGeoCodeResultListener(phoneListener);
		
		// 定位初始化
		mLocClient = new LocationClient(getActivity());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(30000);
		mLocClient.setLocOption(option);
		mLocClient.start();	
	}
	
	
	/**
	 * 手机设备地理位置反解析
	 */
	OnGetGeoCoderResultListener phoneListener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			} else {
				try {
					String phone_adress = result.getAddress();
					int startIndex = phone_adress.indexOf("省") + 1;
					int endIndex = phone_adress.indexOf("市");
					
					app.phoneCity = phone_adress.substring(startIndex, endIndex);
					Log.d(TAG, "手机的位置：：" + app.phoneCity);
							
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {
			app.phoneCity = app.City;
			Log.d(TAG, "手机的位置：ERR：" + app.phoneCity);
		}
	};
	
	
	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			} else {
				try {
					String adress = result.getAddress();
					int startIndex = adress.indexOf("省") + 1;
					adress = adress.substring(startIndex, adress.length());
					
					Log.d(TAG, "定位：：" + adress);
					
					app.carDatas.get(currentIndex).setAdress(adress);
					String rcv_time = app.carDatas.get(currentIndex).getRcv_time();
					
					Log.d(TAG, "定位：：" + adress + GetSystem.GetNowDay());
//					String gpsData = rcv_time.substring(0, 10);// 取出日期
//					String nowData = GetSystem.GetNowDay();
//					String showTime = "";
//					if (gpsData.equals(nowData)) {
//						showTime = rcv_time.substring(11, 16);
//					} else if (gpsData.equals(GetSystem
//							.GetNextData(nowData, -1))) {
//						showTime = "昨天" + rcv_time.substring(11, 16);
//					} else if (gpsData.equals(GetSystem
//							.GetNextData(nowData, -2))) {
//						showTime = "前天" + rcv_time.substring(11, 16);
//					} else {
//						showTime = rcv_time.substring(5, 16);
//					}
					
					electric_carViews.get(currentIndex).getTv_location()
							.setText(adress + "  " + GetSystem.GetNowDay() );
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult arg0) {

		}
	};
	
	/**
	 * @author WU 定位手机经纬度
	 *
	 */
	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null )
				return;
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());	
			app.phone_latlng = latLng;
			mGeoCoder_Phone.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		}
	}
	
	/**
	 * 处理锁定和解锁的返回信息处理
	 */
	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Get_Electric_Car_Respon:
				
				Log.e(TAG, msg.obj.toString());
				jsonLockAndUnlock(msg.obj.toString());
				break;
			}
			return true;
		}
	};
	
	boolean isLock = false;
	/**
	 * @param str 判断是否操作成功
	 */
	private void jsonLockAndUnlock(String str) {
		Log.i(TAG , "jsonLockAndUnlock " + str);
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getInt("status_code") == 0) {
				
				if(isLock ){
					isLock = true;
					Toast.makeText(getActivity(), "锁定成功",
							Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "解锁成功",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				if(isLock ){
					isLock = true;
					Toast.makeText(getActivity(), "锁定失败",
							Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getActivity(), "解锁失败",
							Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(isLock ){
				isLock = true;
				Toast.makeText(getActivity(), "锁定失败",
						Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), "解锁失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/**
	 * 初始化view
	 */
	List<ElectricCarView> electric_carViews = new ArrayList<ElectricCarView>();//views
	private void initView(){
		// 删除车辆后重新布局，如果删除的是最后一个车辆，则重置为第一个车
		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		if (currentIndex >= app.carDatas.size()) {
			currentIndex = 0;
		}
		
		hs_electric_car.removeAllViews();
		electric_carViews.clear();
		for (int i = 0; i < app.carDatas.size(); i++) {
			View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.item_electric_car_info, null);

			hs_electric_car.addView(v);
			
			TextView tv_working_voltage   = (TextView)v.findViewById(R.id.tv_working_voltage);//工作电压
			TextView tv_remainingcapacity = (TextView)v.findViewById(R.id.tv_remainingcapacity);//剩余电量
			TextView tv_charge_distance   = (TextView)v.findViewById(R.id.tv_charge_distance);//续航里程
			TextView tv_car_name = (TextView)v.findViewById(R.id.tv_car_name);//汽车名字
			
			ImageView iv_menu_ctrl = (ImageView)v.findViewById(R.id.iv_menu_ctrl);
			iv_menu_ctrl.setOnClickListener(onClickListener);
			
			
			LinearLayout ll_adress = (LinearLayout) v
					.findViewById(R.id.ll_adress);
			ll_adress.setOnClickListener(onClickListener);
			
			TextView textLocation = (TextView) v
					.findViewById(R.id.textLocation);
			
			ImageView iv_lock = (ImageView)v.findViewById(R.id.iv_electric_lock);
			iv_lock.setOnClickListener(onClickListener);
			ImageView iv_unlock = (ImageView)v.findViewById(R.id.iv_electric_unlock);
			iv_unlock.setOnClickListener(onClickListener);
			
			ElectricCarView electricCarView = new ElectricCarView();
			electricCarView.setTv_charge_distance(tv_charge_distance);
			electricCarView.setTv_remainingcapacity(tv_remainingcapacity);
			electricCarView.setTv_working_voltage(tv_working_voltage);
			
			electricCarView.setLl_adress(ll_adress);
			electricCarView.setTv_location(textLocation);
			
			electric_carViews.add(electricCarView);
			tv_car_name.setText(app.carDatas.get(i).getNick_name());//显示昵称
		}

		hs_electric_car.snapToScreen(currentIndex);
		getElectricCarData();//获取数据
	}
	
	
	
	/**
	 * 点击事件
	 */
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_menu_ctrl:
				if (onCardMenuListener != null) {
					onCardMenuListener.showCarMenu(Const.TAG_ELECTIRC);
				}
				break;
				
			case R.id.ll_adress://跳转到地图
				Intent intent = new Intent(getActivity(),
						CarLocationActivity.class);
				intent.putExtra("index", currentIndex);
				intent.putExtra("isHotLocation", true);
				startActivity(intent);
				break;
				
			case R.id.iv_electric_lock:
				Log.e(TAG, "----" + currentIndex);
				httpAir.setPower(app.carDatas.get(currentIndex).getDevice_id(),
						false);
				isLock = true;
				break;
				
			case R.id.iv_electric_unlock:
				Log.e(TAG, "----" + currentIndex);
				httpAir.setPower(app.carDatas.get(currentIndex).getDevice_id(),
						true);
				isLock = false;
				break;
			}
		}
	};
	
	/**
	 * Handler 消息处理
	 */
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (!resumed || electric_carViews.size() == 0) {// 关闭后直接跳出
				return;
			}
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			switch (msg.what) {
			
			case Msg.Get_Car_GPS:
				setGps(bundle);
				Log.d(TAG, "--->setGps");
				break;
			
			case Msg.Get_Electric_Car_Data:
//				Log.d(TAG, "----" + msg.obj.toString() );
				jsonElectricCarData(msg.obj.toString());
				break;
				
			case Msg.Get_Electric_Car_TenSecond:
				Log.d(TAG, "----case Msg.Get_Electric_Car_TenSecond:" );
				updaUI(currentIndex);//更新UI
				break;
			}
		}
	};
	
	
	/** 设置GPS信息 **/
	private void setGps(Bundle bundle) {
		if (bundle == null) {
			return;
		}
		Double lat = bundle.getDouble("lat");
		Double lon = bundle.getDouble("lon");
		app.carDatas.get(currentIndex).setLat(lat);
		app.carDatas.get(currentIndex).setLon(lon);
		LatLng latLng = new LatLng(lat, lon);
		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
	}
	
	/**
	 * @param jsonData 
	 */
	private void jsonElectricCarData(String jsonData){
		
		try {
			JSONObject obj = new JSONObject(jsonData);
			
			if(obj.has("active_obd_data")){
				if(obj.getJSONObject("active_obd_data").has("xhlc")){
					xhlicheng = obj.getJSONObject("active_obd_data").getString("xhlc");
					Log.d(TAG, "===========" + xhlicheng);
				}else {
					xhlicheng   = "--";
					Log.d(TAG, "===========" + "没有 xhlc");
				}if(obj.getJSONObject("active_obd_data").has("sydl")){
					sydianliang = obj.getJSONObject("active_obd_data").getString("sydl");
					Log.d(TAG, "===========" + sydianliang);
				}else {
					sydianliang = "--";
					Log.d(TAG, "===========" + "没有 sydl");
				}if(obj.getJSONObject("active_obd_data").has("dpdy")){
					voltage = obj.getJSONObject("active_obd_data").getString("dpdy");
					
					if(Float.valueOf(voltage) < 25.00){
						voltage     = "--";
						Log.d(TAG, "===========" + "没有 dpdy");
					}else{
						Log.d(TAG, "===========" + voltage);
					}
				}else {
					voltage     = "--";
					Log.d(TAG, "===========" + "没有 dpdy");
				}
				
			}else{
				Log.d(TAG, "===========" + "wuwuwuwuwu");
				
				voltage     = "--";
				xhlicheng   = "--";
				sydianliang = "--";
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		carData = app.carDatas.get(currentIndex);
//		String device_id = carData.getDevice_id();
//		
//		Log.d(TAG, "--->");
//		if(device_id.equals("1592")){
//			Gson gson = new Gson();
//			ElectricCarData electricCarData = gson.fromJson(jsonData, ElectricCarData.class);
//			ElectricCarDetail electricCarDetail = electricCarData.getActive_obd_data();
//			
//			voltage     = electricCarDetail.getDpdy();
//			xhlicheng   = electricCarDetail.getXhlc();
//			sydianliang = electricCarDetail.getSydl();
//		}else{
//			voltage     = "--";
//			xhlicheng   = "--";
//			sydianliang = "--";
//		}
		
		
		mHandler.sendEmptyMessage(Msg.Get_Electric_Car_TenSecond);
	}
	
	
	/**
	 * @param index 更新Ui
	 */
	private void updaUI(int index){
		ElectricCarView electricCarView = electric_carViews.get(index);
		electricCarView.getTv_charge_distance().setText(xhlicheng);
		electricCarView.getTv_remainingcapacity().setText(sydianliang);
		electricCarView.getTv_working_voltage().setText(voltage);
	}
	
	
	/**
	 * 获取数据
	 */
	private void getElectricCarData(){
		
		carData = app.carDatas.get(currentIndex);
		String device_id = carData.getDevice_id();
		
		String electricUrl = "http://api.bibibaba.cn/device/" +
				device_id +
				"?auth_code=" + app.auth_code;
		Log.d(TAG, "--->" + electricUrl);
		new NetThread.GetDataThread(mHandler, electricUrl, Msg.Get_Electric_Car_Data).start();
	}
	
	
	/** 
	 * 未登录显示 
	 */
	public void setLoginView() {
		
		hs_electric_car.removeAllViews();
		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.item_electric_car_info, null);
		hs_electric_car.addView(v);
		
		LinearLayout ll_electric_car = (LinearLayout)v.findViewById(R.id.ll_electric_car);	
		TextView tv_working_voltage   = (TextView)v.findViewById(R.id.tv_working_voltage);//工作电压
		TextView tv_remainingcapacity = (TextView)v.findViewById(R.id.tv_remainingcapacity);//剩余电量
		TextView tv_charge_distance   = (TextView)v.findViewById(R.id.tv_charge_distance);//续航里程
		TextView tv_car_name = (TextView)v.findViewById(R.id.tv_car_name);//汽车名字
		
		TextView textLocation = (TextView) v.findViewById(R.id.textLocation);
		textLocation.setText("");
		tv_working_voltage.setText("--");
		tv_remainingcapacity.setText("--");
		tv_charge_distance.setText("--");
		tv_car_name.setText("绑定叭叭车载智能配件");
		
		ll_electric_car.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
	}
	
	
	/**
	 * 获取电池信息 每隔十秒钟
	 */
	private  void updataElectricCarData() {

		// 刷新车辆信息的线程
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (resumed) {
					try {
						SystemClock.sleep(10000);
						getElectricCarData();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		};
		Thread refresh = new Thread(runnable);
		refresh.start();
	}
	
	/**
	 * @param onCardMenuListener 卡片布局 监听。置顶删除等操作
	 */
	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}

	/**
	 * 获取车辆信息 refreshAllData
	 */
	public void refreshAllData() {
		// 刷新车辆信息的线程
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (resumed) {
					SystemClock.sleep(5 * 60000);
					httpCarInfo.requestAllData();
					Log.i("ThreadTest", "refreshAllData");
				}
			}
		};
		Thread refreshThread = new Thread(runnable);
		refreshThread.start();
	}

	/**
	 * 开启更新位置线程 refreshLoaction
	 */
	public void refreshLoaction() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (resumed) {
					if (app.carDatas == null || app.carDatas.size() == 0) {
						continue;
					}
					if (currentIndex >= app.carDatas.size()) {
						continue;
					}
					carData = app.carDatas.get(currentIndex);
					String device_id = carData.getDevice_id();
					if (device_id == null || device_id.equals("")) {
						continue;
					}
					httpCarInfo.requestGps(device_id);
					Log.d(TAG, "十秒刷新数据requestGps");
					SystemClock.sleep(30000);
				}
			}
		};
		// 30秒定位，显示当前位子
		new Thread(runnable).start();
	}
	
	
	
	boolean resumed = false;

	@Override
	public void onResume() {
		super.onResume();
		resumed = true;
		// 更新车辆位置
		refreshLoaction();
		// 刷新车辆信息
		refreshAllData();
		updataElectricCarData();
		Log.d(TAG, "onResume();");
	}

	@Override
	public void onPause() {
		super.onPause();
		resumed = false;
		Log.d(TAG, "onPause();");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 3) {
			// 修改车辆信息
			initView();
		} 
	}
}
