package jp.gingarenpo.gts.light;

import jp.gingarenpo.gts.controller.TrafficController;

import java.io.Serializable;

/**
 * 信号機そのものが持つ情報を格納するインスタンス。
 * この信号機の受信チャンネルと、アタッチしている制御機のインスタンスを格納するものとなっている。
 *
 * ここでアタッチする制御機のインスタンスは同一のものを想定している。
 * インスタンスを使いまわすパターンで行きたいところだが…
 */
public class TrafficLight implements Serializable {
	
	/**
	 * チャンネル。デフォルトは0で受信しない（設定のつもり）
	 */
	private int signal;
	
	/**
	 * アタッチしている制御機。していない場合はNull。
	 */
	private TrafficController parent; // どの制御機にアタッチしているかの情報
	
	public TrafficLight() {}
	
	public TrafficLight(int signal) {
		this.signal = signal;
	}
	
	public TrafficController getParent() {
		return parent;
	}
	
	public void setParent(TrafficController parent) {
		this.parent = parent;
	}
	
	/**
	 * この信号機が制御機をアタッチしていればTrueを返す。
	 * @return
	 */
	public boolean isAttached() {
		return parent != null;
	}
	
	public int getSignal() {
		return signal;
	}
	
	public void setSignal(int signal) {
		this.signal = signal;
	}
	
	@Override
	public String toString() {
		return "TrafficLight{" +
					   "signal=" + signal +
					   ", parent=" + parent +
					   '}';
	}
}
