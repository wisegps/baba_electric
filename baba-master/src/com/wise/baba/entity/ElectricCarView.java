package com.wise.baba.entity;

import android.widget.TextView;

public class ElectricCarView {
	
	TextView tv_working_voltage; //工作电压
	TextView tv_remainingcapacity;//剩余电量
	TextView tv_charge_distance;//续航里程
	
	TextView tv_car_name;//汽车名字
	
	
	

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
