package jp.gingarenpo.gts.controller;


import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 制御機が保持するべきデータを集めたもの。
 * クラスとして作成することで、サーバークライアント間の同期をしやすくする目的がある。
 * シリアライズ可能。
 */
public class TrafficController implements Serializable {

	private ArrayList<Cycle> statuses = new ArrayList<Cycle>(); // 別途作成するクラスにて、ステータスを管理する（制御機の現示と時間をまとめている）
	private String id = ""; // ワールド内で被らない、制御機固有のIDを指定する。警察署番号とか管理番号に相当する。入るのは英数字のみ！
	private Color color = Color.WHITE; // デフォルトは白。制御機の色。DynamicTextureで使用する。
	private boolean active = false; // この制御機が動くかどうか。動かさない場合はfalseにすることでとりあえずリソース節約可能
	private int phase = 0; // サイクルのステータスで現在表示すべきもの。
	private boolean detected = false; // この制御機に対して感知機能が働いているかどうか。
	private long tick = 0; // サイクルフェーズが切り替わってからの経過秒数。
	
	public TrafficController() {
		// empty
	}
	
	public TrafficController(String id) {
		this(); // 将来予約用
		this.id = id; // IDを代入
	}
	
	public TrafficController(String id, Color color) {
		// 全部指定する場合
		this(id);
		this.color = color;
	}
	
	/**
	 * 現在登録されているステータスを返す。
	 * @return
	 */
	public ArrayList<Cycle> getStatuses() {
		return statuses;
	}
	
	/**
	 * 現在の表示サイクルを返す。サイクルが存在しない場合はnullを返す。
	 * @return
	 */
	public Cycle getNowCycle() {
		if (statuses.size() == 0) return null; // サイクルが存在しない場合はnull
		return statuses.get(phase);
	}
	
	/**
	 * ステータスを一気に更新する。一括で切り替える場合に使おう。
	 * @param statuses
	 */
	public void setStatuses(ArrayList<Cycle> statuses) {
		this.statuses = statuses;
	}
	
	/**
	 * IDを返す。
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * IDを指定する。ただし英数字のみ受け付け。それ以外の場合は例外が発生する。
	 * @return
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * この制御機が現在アクティブかどうかを返す。
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * この制御機のアクティブ状態を設定する。夜間点滅も一つのアクティブなので注意！
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * 感知信号が受信されているかどうか、返す。
	 * @return
	 */
	public boolean isDetected() {
		return this.detected;
	}
	
	/**
	 * 感知状態を切り替える。通常はオンにするだけだろうけど。
	 * @param detected
	 */
	public void setDetected(boolean detected) {
		this.detected = detected;
	}
	
	/**
	 * この制御機の色を返す。
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * この制御機の色を設定する。ただし、自動でテクスチャを更新はしないので手動で再生成しないと変更は反映されない。
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * 現在のサイクル状態番号を返す。これ単体で使うことはあまりないと思うが一応。
	 * @return
	 */
	public int getPhase() {
		return phase;
	}
	
	/**
	 * 指定したphaseに強制セットする。
	 * @deprecated このメソッドはあまり役に立ちません。通常は「nextPhase」もしくは「resetPhase」を使うべきです。
	 * @param phase
	 */
	public void setPhase(int phase) {
		if (statuses.size() <= phase) {
			throw new IllegalArgumentException("Phase Out of bound");
		}
		this.phase = phase;
	}
	
	/**
	 * サイクルを1段階進める。もし最後のサイクルの場合は、最初のサイクルに戻る。
	 */
	public void nextPhase() {
		if (phase == statuses.size()-1) {
			// 既に最後のサイクルである場合は
			this.phase = 0;
		}
		else {
			// 最後ではないので
			this.phase++;
		}
		this.tick = 0;
	}
	
	/**
	 * 強制的にサイクルを最初からやり直す。
	 */
	public void resetPhase() {
		this.phase = 0;
		this.tick = 0;
	}
	
	/**
	 * 現在のサイクルが開始してからのTick数を取得する。
	 * @return
	 */
	public long getTick() {
		return this.tick;
	}
	
	/**
	 * Tickを指定したものに変更する。特例として、マイナスを指定すると通常より若干延長することができる。
	 * @param tick
	 */
	public void setTick(long tick) {
		this.tick = tick;
	}
	
	/**
	 * サイクルの調整、パラメーターの初期化などを全部自動でやってくれる優れもの。
	 * 1Tick毎に呼び出すことで、setterなどを呼ばなくても一連の処理を全てやってくれる。
	 * 通常はこちらを呼び出すべき。TileEntityなどで呼び出そう。
	 *
	 * なお、現示の反映に関してはデータの呼び出しもとに責任がある！
	 */
	public void onUpdateTick() {
		// 上記メソッドを実行していく
		this.tick++; // tickに1秒足す
		// 現在のサイクルを取得する
		Cycle cycle = this.getNowCycle(); // 現在のサイクルを取得する
		if (cycle == null) return; // そもそもサイクルが存在しない
		// このサイクルが終了しているか確認する
		if (cycle.isFinished(this)) {
			// 終了していたら
			this.nextPhase(); // 次のフェーズへ移行
		}
	}
	
	/**
	 * このクラスの文字列表現を返す。
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[TrafficController:" + id + "] ");
		sb.append("color = " + color.toString());
		sb.append(" tick = " + tick);
		sb.append(" detected = " + detected);
		return sb.toString();
	}
}
