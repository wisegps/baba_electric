package com.wise.baba.entity;

import com.wise.baba.ui.widget.DialView;

import android.widget.LinearLayout;
import android.widget.TextView;

public class ElectricCarView {
	
	TextView tv_working_voltage; //工作电压
	TextView tv_remainingcapacity;//剩余电量
	TextView tv_charge_distance;//续航里程
	
	TextView tv_car_name;//汽车名字
	
	LinearLayout ll_adress;
	TextView tv_location;
	
	DialView xuhang_licheng;
	TextView tv_xuhang_l_c;
	TextView tv_title_xhlc;
	
	

	public TextView getTv_title_xhlc() {
		return tv_title_xhlc;
	}

	public void setTv_title_xhlc(TextView tv_title_xhlc) {
		this.tv_title_xhlc = tv_title_xhlc;
	}

	public TextView getTv_xuhang_l_c() {
		return tv_xuhang_l_c;
	}

	public void setTv_xuhang_l_c(TextView tv_xuhang_l_c) {
		this.tv_xuhang_l_c = tv_xuhang_l_c;
	}

	public DialView getXuhang_licheng() {
		return xuhang_licheng;
	}

	public void setXuhang_licheng(DialView xuhang_licheng) {
		this.xuhang_licheng = xuhang_licheng;
	}

	public TextView getTv_location() {
		return tv_location;
	}

	public void setTv_location(TextView tv_location) {
		this.tv_location = tv_location;
	}

	public LinearLayout getLl_adress() {
		return ll_adress;
	}

	public void setLl_adress(LinearLayout ll_adress) {
		this.ll_adress = ll_adress;
	}

	public TextView getTv_working_voltage() {
		return tv_working_voltage;
	}

	public void setTv_working_voltage(TextView tv_working_voltage) {
		this.tv_working_voltage = tv_working_voltage;
	}

	public TextView getTv_remainingcapacity() {
		return tv_remainingcapacity;
	}

	public void setTv_remainingcapacity(TextView tv_remainingcapacity) {
		this.tv_remainingcapacity = tv_remainingcapacity;
	}

	public TextView getTv_charge_distance() {
		return tv_charge_distance;
	}

	public void setTv_charge_distance(TextView tv_charge_distance) {
		this.tv_charge_distance = tv_charge_distance;
	}

	public TextView getTv_car_name() {
		return tv_car_name;
	}

	public void setTv_car_name(TextView tv_car_name) {
		this.tv_car_name = tv_car_name;
	}
	
	
	
	
//	ImageView iv_electric_lock;//锁车
//	ImageView iv_electric_unlock;//解锁
	
}
