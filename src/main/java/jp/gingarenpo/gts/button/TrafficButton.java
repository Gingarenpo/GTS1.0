package jp.gingarenpo.gts.button;

import jp.gingarenpo.gts.GTS;

import java.io.Serializable;

/**
 * 押ボタン箱のデータ。押ボタン箱自体は制御機が勝手に検出して格納することになる。
 * フラグと将来的に鳴らしたいためSoundEventを登録しておく（予約済み）
 */
public class TrafficButton implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * このボタンが押された場合はtrueとなる。制御機側で解除される。
	 */
	private boolean push = false;
	
	public TrafficButton() {
	
	}
	
	/**
	 * ボタンを押す。
	 */
	public void push() {
		this.push = true;
		GTS.GTSLog.debug("Pushed.");
	}
	
	/**
	 * ボタンを押されていない状態にする。
	 */
	public void reset() {
		this.push = false;
	}
	
	/**
	 * このボタンが押されているかどうかを返す。
	 * @return 押されていればtrue
	 */
	public boolean isPushed() {
		return this.push;
	}
}
