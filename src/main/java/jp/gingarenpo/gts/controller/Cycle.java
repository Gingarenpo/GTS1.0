package jp.gingarenpo.gts.controller;

import jp.gingarenpo.gts.data.ConfigBase;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * サイクル状態を表すクラス。シリアライズ可能。
 */
public class Cycle implements Serializable {
	
	private long tick; // このサイクルを表示し続ける時間。ただし「forever」あるいは「detectable」がtrueの場合はこの値は無視される。
	private boolean forever; // このサイクルは永遠に続く。終止夜間点滅とかにする場合に用いる。
	private boolean detectable; // trueに指定すると、このサイクルは感知信号を受信するまで永遠に続く。foreverと一緒に指定できない。
	private boolean tickChangeable; // 予約済み。現在使用されない。青延長のような形での使用を検討している。
	private ArrayList<ConfigBase.LightObject> channels = new ArrayList<>(); // 0始まりで1,2,…とこのサイクルの時の現示のチャンネルを保持する。
	
	
	public Cycle() {
		// 何もしない
	}
	
	/**
	 * 最大現示Tick数を返す。
	 * @return
	 */
	public long getTick() {
		return tick;
	}
	
	/**
	 * 最大現示Tick数を指定する。負の数は指定できない。指定しようとすると例外がスローされる。
	 * @param tick
	 */
	public void setTick(long tick) {
		if (tick < 0) throw new IllegalArgumentException("tick must not be negative.");
		this.tick = tick;
	}
	
	/**
	 * このサイクルが永遠に続くかどうかを返す。
	 * @return
	 */
	public boolean isForever() {
		return forever;
	}
	
	/**
	 * このサイクルが永遠に続くかどうかを指定する。detectableがtrueになっている場合にこちらをtrueにすると、例外がスローされる。
	 * @param forever
	 */
	public void setForever(boolean forever) {
		if (this.detectable && forever) throw new IllegalArgumentException("Detectable is already true.");
		this.forever = forever;
	}
	
	/**
	 * このサイクルは感知信号を受け付けるかを返す。
	 * @return
	 */
	public boolean isDetectable() {
		return detectable;
	}
	
	/**
	 * このサイクルは感知信号を受け付けるか指定する。foreverがtrueになっている場合にこちらをtrueにすると、例外がスローされる。
	 * @param detectable
	 */
	public void setDetectable(boolean detectable) {
		if (this.forever && detectable) throw new IllegalArgumentException("Forever is already true.");
		this.detectable = detectable;
	}
	
	/**
	 * 予約済み
	 * @return
	 */
	public boolean isTickChangeable() {
		return tickChangeable;
	}
	
	/**
	 * 予約済み
	 * @param tickChangeable
	 */
	public void setTickChangeable(boolean tickChangeable) {
		this.tickChangeable = tickChangeable;
	}
	
	/**
	 * このサイクルが指定したtickと感知信号の有無で終了しているかどうか判定する。
	 * trueを返す場合、それはサイクルの終了を示す。
	 * @param tick 状況を確認したい時点でのtick。
	 * @param detect そのtickにおける感知信号の有無。
	 * @return
	 */
	public boolean isFinished(long tick, boolean detect) {
		if (forever) return false; // 永遠の場合は常にfalse
		if (detectable && !detect) return false; // 感知信号待ちで感知信号が受信できない場合はfalse
		return tick >= this.tick; // 超えていたらサイクル終了
	}
	
	/**
	 * 制御機自体のインスタンスを指定することで、同様にサイクルの終了を検知できるようにオーバーライドしただけのメソッド。
	 * @param controller 制御機のデータを格納するインスタンス。
	 * @return
	 */
	public boolean isFinished(TrafficController controller) {
		// ただのラッパー
		return isFinished(controller.getTick(), controller.isDetected());
	}
	
	/**
	 * このサイクルで指示する全チャンネルの現示オブジェクトを返す。
	 * @return
	 */
	public ArrayList<ConfigBase.LightObject> getChannels() {
		return channels;
	}
	
	/**
	 * このサイクルで指示する全チャンネルの現示オブジェクトを指定したものに置き換える。
	 * @param channels
	 */
	public void setChannels(ArrayList<ConfigBase.LightObject> channels) {
		this.channels = channels;
	}
}
