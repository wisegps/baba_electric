package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpAir;
import com.wise.baba.biz.HttpGetObdData;
import com.wise.baba.entity.Air;
import com.wise.baba.entity.ElectricCarData;
import com.wise.baba.entity.ElectricCarDetail;
import com.wise.baba.entity.ElectricCarView;
import com.wise.baba.entity.Weather;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.car.CarUpdateActivity;
import com.wise.setting.LoginActivity;
import com.wise.state.ManageActivity;

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
	
	private static final String electricUrl = "http://api.bibibaba.cn/device/1592?auth_code=bba2204bcd4c1f87a19ef792f1f68404";
	
	private HScrollLayout hs_electric_car;
	private AppApplication app;
	/** 当前车在列表中位置 **/
	public int index = 0;
	private OnCardMenuListener onCardMenuListener;
	public HttpAir httpAir;
	private Handler uiHander = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_electric_car_info, container, false);
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		uiHander = new Handler(handleCallBack);
		httpAir = new HttpAir(this.getActivity(),uiHander);
//		httpObd = new HttpGetObdData(this.getActivity(), uiHander);
		app = (AppApplication) getActivity().getApplication();
		hs_electric_car = (HScrollLayout) getActivity().findViewById(R.id.hs_electric_car);
		hs_electric_car.setOnViewChangeListener(new OnViewChangeListener() {
			
			@Override
			public void OnViewChange(int changedIndex, int duration) {
				if (index == changedIndex) {
					return;
				}
				index = changedIndex;
				app.currentCarIndex = changedIndex;
				Log.d("FragmentCarInfo", "当前车辆" + app.currentCarIndex);
				// 等待滚动完毕后查询数据
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getElectricCarData();//获取数据
					}
				}, duration);
				
			}
		});
		initView();
	}
	
	
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
		if (index >= app.carDatas.size()) {
			index = 0;
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
			
			ImageView iv_lock = (ImageView)v.findViewById(R.id.iv_electric_lock);
			iv_lock.setOnClickListener(onClickListener);
			ImageView iv_unlock = (ImageView)v.findViewById(R.id.iv_electric_unlock);
			iv_unlock.setOnClickListener(onClickListener);
			
			ElectricCarView electricCarView = new ElectricCarView();
			electricCarView.setTv_charge_distance(tv_charge_distance);
			electricCarView.setTv_remainingcapacity(tv_remainingcapacity);
			electricCarView.setTv_working_voltage(tv_working_voltage);
			electric_carViews.add(electricCarView);
			tv_car_name.setText(app.carDatas.get(i).getNick_name());//显示昵称
		}

		hs_electric_car.snapToScreen(index);
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
				
			case R.id.iv_electric_lock:
				Log.e(TAG, "----" + app.currentCarIndex);
				httpAir.setPower(app.carDatas.get(app.currentCarIndex).getDevice_id(),
						false);
				isLock = true;
				break;
				
			case R.id.iv_electric_unlock:
				Log.e(TAG, "----" + app.currentCarIndex);
				httpAir.setPower(app.carDatas.get(app.currentCarIndex).getDevice_id(),
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
			
			switch (msg.what) {
			case Msg.Get_Electric_Car_Data:

				jsonElectricCarData(msg.obj.toString());
				break;
			}
		}
	};
	
	
	
	
	/**
	 * @param jsonData 
	 */
	private void jsonElectricCarData(String jsonData){

		Log.d(TAG, "--->");
		
		Gson gson = new Gson();
		ElectricCarData electricCarData = gson.fromJson(jsonData, ElectricCarData.class);
		ElectricCarDetail electricCarDetail = electricCarData.getActive_obd_data();
		
		String voltage     = electricCarDetail.getDpdy();
		String xhlicheng   = electricCarDetail.getXhlc();
		String sydianliang = electricCarDetail.getSydl();
		
		Log.d(TAG, "--->" + voltage + "---->" + xhlicheng  + "---->" + sydianliang);
		
		ElectricCarView electricCarView = electric_carViews.get(app.currentCarIndex);
		electricCarView.getTv_charge_distance().setText(xhlicheng);
		electricCarView.getTv_remainingcapacity().setText(sydianliang);
		electricCarView.getTv_working_voltage().setText(voltage);
	}
	
	
	/**
	 * 获取数据
	 */
	private void getElectricCarData(){
		new NetThread.GetDataThread(mHandler, electricUrl, Msg.Get_Electric_Car_Data).start();
	}
	
	/** 
	 * 未登录显示 
	 */
	public void setLoginView() {
		app.islogin = false;
		hs_electric_car.removeAllViews();
		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.item_electric_car_info, null);
		hs_electric_car.addView(v);
		
		LinearLayout ll_electric_car = (LinearLayout)v.findViewById(R.id.ll_electric_car);	
		TextView tv_working_voltage   = (TextView)v.findViewById(R.id.tv_working_voltage);//工作电压
		TextView tv_remainingcapacity = (TextView)v.findViewById(R.id.tv_remainingcapacity);//剩余电量
		TextView tv_charge_distance   = (TextView)v.findViewById(R.id.tv_charge_distance);//续航里程
		TextView tv_car_name = (TextView)v.findViewById(R.id.tv_car_name);//汽车名字
		
		tv_working_voltage.setText("0");
		tv_remainingcapacity.setText("0");
		tv_charge_distance.setText("0");
		tv_car_name.setText("绑定叭叭车载智能配件");
		
		ll_electric_car.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				getActivity().finish();
				app.islogin = false;
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
	}
	
	
	/**
	 * 获取电池信息 每隔十秒钟
	 */
	private  void refreshAllData() {

		// 刷新车辆信息的线程
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (app.islogin) {
					try {
						Thread.sleep(10000);
						getElectricCarData();
						Log.d(TAG, "getElectricCarData()");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		};
		Thread refresh = new Thread(runnable);
		refresh.start();
	}
	
	
	
	
	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(app.islogin){
			refreshAllData();
			Log.d(TAG, "refreshAllData();");
		}
		
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		app.islogin = false;
	}


	
}
