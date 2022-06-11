package jp.gingarenpo.gts.controller;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.cycle.Cycle;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 交通信号制御機のデータを保持するクラス。シリアライズ可能だが行わない。
 * 基本的に1つの制御機は1つのサイクルを実行するものとするが、夜間点滅やプログラム多段制御などを実現するために
 * 仕組みとしては複数のサイクル登録を取り入れている。
 * このインスタンスが登録されている場合に、サイクル終了後このインスタンスを経由してサイクルの継続条件判定などを行う。
 * このインスタンス自体は処理を直接実行することはないためTickableは実装していないが、外部から呼び出しを受けたときの為の
 * メソッド自体は用意してある。
 */
public class TrafficController {
	
	/**
	 * この制御機に登録されているサイクルを格納する。キーはサイクル名とし、ハッシュマップで格納する。順番は問わない（つもり）。
	 */
	private HashMap<String, Cycle> cycles = new HashMap<String, Cycle>();
	
	/**
	 * 制御機自体の固有の名称。日本語OK
	 */
	private String name;
	
	/**
	 * 制御機のベース色を指定する。指定するColorクラスはAWTのColorクラス。BufferedTextureで作成するために使用させる。
	 * デフォルトは真っ白。
	 */
	private Color color = Color.WHITE;
	
	/**
	 * 制御機のテクスチャを保持しておく場所。このインスタンス自体ではGetterとSetterしか提供せず、
	 * Render側でこの入れ物を適宜使用していくことにする。
	 */
	private BufferedImage texture;
	
	/**
	 * 検知信号を受信したかどうかを格納する変数。感知器や押ボタン箱の動作によってこの値が変化する。
	 * サイクルを開始する条件の一つとして利用される。
	 */
	private boolean detected = false; // 初期値はfalse
	
	/**
	 * 検知ティック。検知信号がtrueになった瞬間を0とし、そこからの経過Tickを格納する。longとする。
	 */
	private long detectedTick = 0;
	
	/**
	 * あるサイクルが動作を開始した際を0とし、そこからの経過Tickを格納する。基本的にスピリットとオフセットを決めるために使用する。
	 */
	private long ticks = 0;
	
	/**
	 * 現在起動しているサイクルの名前が入る。サイクルが何一つ起動していない場合はnullが格納される。
	 * 内部で使用しているだけなのでこの中身を取得するGetterなどは存在しない。
	 */
	private String now;
	
	
	/**
	 * 交通信号制御機を初期化する。名前が必須案件だが省略して生成するとランダムで64文字のIDが渡される。適宜変更すること。
	 * なお日付と一緒に格納してある為天文学的な確率に引っかからない限りユニークな文字列となるはず。
	 */
	public TrafficController() {
	}
	
	/**
	 * 指定したIDで交通信号制御機を初期化する。デフォルトはこちら。
	 * @param name 制御機の名前。
	 */
	public TrafficController(String name) {
		this.name = name;
	}
	
	/**
	 * サイクルのすべてを取得する。通常使用しない。
	 * @return サイクルすべてが格納されたHashMap
	 */
	public HashMap<String, Cycle> getCycles() {
		return cycles;
	}
	
	/**
	 * サイクルを一括登録する。通常使用しない。
	 * @param cycles 登録したいサイクル。
	 */
	public void setCycles(HashMap<String, Cycle> cycles) {
		this.cycles = cycles;
	}
	
	/**
	 * 現在起動しているサイクルの情報を返す。
	 * 起動していない場合はnullが返る。
	 * @return サイクル
	 */
	public Cycle getNowCycle() {
		if (this.now == null) return null;
		return this.cycles.get(now);
	}
	
	/**
	 * 制御機の名前を取得する。
	 * @return 名前
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 制御機の名前を指定する。
	 * @param name 名前
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 制御機の色を取得する。
	 * @return 色
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * 制御機の色を指定する。
	 * @param color 色
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * 制御機のテクスチャを取得する。
	 * @return テクスチャ。Nullが返ってくる場合あり
	 */
	public BufferedImage getTexture() {
		return texture;
	}
	
	/**
	 * 制御機のテクスチャを指定する。
	 * @param texture テクスチャ
	 */
	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}
	
	/**
	 * 検知信号を受信しているかどうかを返す。
	 * @return 受信していればtrue、していなければfalse
	 */
	public boolean isDetected() {
		return detected;
	}
	
	/**
	 * 検知信号の状態を強制的に変化させる。
	 * 主に押ボタン箱などが使用するためのメソッド。
	 * @param detected 検知信号状態。
	 */
	public void setDetected(boolean detected) {
		this.detected = detected;
	}
	
	/**
	 * 検知信号受信後のTick経過数を取得する。検知信号を受信していない場合は常に0が返る。
	 * @return 検知Tick経過数
	 */
	public long getDetectedTick() {
		return detectedTick;
	}
	
	/**
	 * 検知信号Tickを強制的に変更する。サイクルバランスが乱れる恐れがある為通常使用は禁止。
	 * @param detectedTick 負の数を指定すると例外を出す
	 */
	public void setDetectedTick(long detectedTick) throws IllegalArgumentException {
		if (detectedTick < 0) throw new IllegalArgumentException("DetectTick must be greater than 0.");
		this.detectedTick = detectedTick;
	}
	
	/**
	 * サイクル開始後のTicksを取得する。
	 * @return Ticks
	 */
	public long getTicks() {
		return ticks;
	}
	
	/**
	 * Tickを強制的に変更する。サイクルバランスが乱れる恐れがある為通常使用は禁止。
	 * @param ticks 負の数を指定すると例外を出す
	 */
	public void setTicks(long ticks) throws IllegalArgumentException {
		if (detectedTick < 0) throw new IllegalArgumentException("DetectTick must be greater than 0.");
		this.ticks = ticks;
	}
	
	/**
	 * 外部から呼び出されることを前提としている。Tickableを実装したクラス（デフォルトではTileEntity）から呼び出されることを想定。
	 * 外部からWorldインスタンスを受け取り、サイクルの起動チェックと終了チェックを行う。戻り値は現在指定していないが将来的に
	 * 変更される可能性もある。
	 *
	 * @param world この制御機が設置されているワールドのインスタンス。
	 */
	public void checkCycle(World world) {
		if (now != null) {
			// 現在サイクルが起動している場合、まず終了条件を確かめる
			if (!getNowCycle().nextOrResetPhase(this, world)) {
				// サイクルを終了できない場合（まだこのサイクルが起動中である場合）
				GTS.GTSLog.debug("Cycle " + now + " is need to continue.");
				return; // 処理を中止する
			}
			// サイクルの終了が確認でき、上記メソッドでサイクルのリセットを行ったため起動サイクル状態を初期化
			now = null;
		}
		
		// 起動条件を確かめる（HashMapで順繰りにイテレータを回す
		for (Map.Entry<String, Cycle> cycle: cycles.entrySet()) {
			if (cycle.getValue().canStart(world, this, this.detected)) {
				// サイクルの起動可能な場合はサイクルを起動する
				now = cycle.getKey();
				GTS.GTSLog.debug("Cycle " + now + " is now started.");
				break;
			}
		}
		
		// 起動条件に一致したサイクルが存在しない場合はnowはnullのままとなり起動せずに次回持越し
		if (now == null) GTS.GTSLog.debug("No cycle can be ready.");
		return;
	}
	
	

}
