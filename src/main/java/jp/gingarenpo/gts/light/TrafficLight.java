package jp.gingarenpo.gts.light;

import jp.gingarenpo.gts.controller.TrafficController;

import java.io.Serializable;

/**
 * 交通信号機自体の情報。交通信号機としての情報を保持する。
 */
public class TrafficLight implements Serializable {
	
	private int signal; // 何番の信号を受信するか（デフォルト0）
	private TrafficController parent; // どの制御機にアタッチしているかの情報
	
	public TrafficLight(int signal) {
		this.signal = signal;
	}
	
	public TrafficController getParent() {
		return parent;
	}
	
	public void setParent(TrafficController parent) {
		this.parent = parent;
	}
	
	public int getSignal() {
		return signal;
	}
	
	public void setSignal(int signal) {
		this.signal = signal;
	}
}
