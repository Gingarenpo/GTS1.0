package jp.gingarenpo.gts.light;

import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.data.ConfigBase;

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
	 * 現時点での交通信号機の点灯するオブジェクト。レンダーから呼び出す際に使用する。
	 */
	private ConfigBase.LightObject light;
	
	/**
	 * 更新の必要性があるかどうかのフラグ。
	 */
	private boolean update = false;
	
	
	public TrafficLight() {
		light = new ConfigBase.LightObject().setName("DUMMY"); // とりあえずダミーのライトを入れてNullを回避する
	}
	
	public TrafficLight(int signal) {
		this.signal = signal;
	}
	
	public int getSignal() {
		return signal;
	}
	
	public void setSignal(int signal) {
		this.signal = signal;
	}
	
	public void setLight(ConfigBase.LightObject light) {
		this.light = light;
		this.notifyUpdate();
	}
	
	public ConfigBase.LightObject getLight() {
		return this.light;
	}
	
	/**
	 * 信号機の再描画を要求する。TileEntityがこのフラグを監視してupdateを行う。
	 */
	public void notifyUpdate() {
		this.update = true;
	}
	
	/**
	 * 信号機の再描画終了後に呼び出される。変更を終了する。
	 */
	public void doneUpdate() {
		this.update = false;
	}
	
	/**
	 * 再描画が必要かどうかを返す。
	 * @return
	 */
	public boolean isUpdate() {
		return this.update;
	}
}
