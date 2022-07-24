package jp.gingarenpo.gts.controller.phase;

import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.exception.DataExistException;
import jp.gingarenpo.gts.light.ConfigTrafficLight;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * サイクルに登録すべき信号の状態を格納したフェーズという状態を保持するクラス。
 * 各状態における信号の色（チャンネル）を保持することになる。
 * このクラス自体は抽象クラスの為、そのまま使用することは不可能。アドオンなどで拡張する際は
 * このクラスを継承し、新たなフェーズを作成すること。
 *
 * 継続条件を主に変えることになる。
 * ※PhaseBaseというクラスがあるがこちらは組み込みクラスでありこのクラスを継承することはあまりお勧めしない
 */
public abstract class Phase implements Serializable {
	
	/**
	 * このフェーズの名称。
	 */
	protected String name;
	
	/**
	 * 各チャンネル毎にどの信号を光らせるかを指定するハッシュマップ。
	 * チャンネル番号は1から指定可能。それ以外は登録不可。ArrayListだとlong使えないみたいなので
	 */
	protected LinkedHashMap<Long, ConfigTrafficLight.LightObject> channels = new LinkedHashMap<Long, ConfigTrafficLight.LightObject>();
	
	/**
	 * このフェーズが開始してからの総Tick数を保持する。
	 */
	protected long ticks = 0;
	
	/**
	 * 指定した名称でフェーズを作成する。
	 * @param name 名前
	 */
	public Phase(String name) {
		this.name = name;
	}
	
	/**
	 * ランダムな文字列を使用してフェーズを作成する.
	 */
	public Phase() {
		this(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()) + RandomStringUtils.randomAlphanumeric(24));
	}
	
	/**
	 * 名前を返す。
	 * @return 名前
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 名前を指定する。
	 * @param name 名前
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 指定した番号のチャンネルを新規登録する。番号が0以下の場合は例外を返す。
	 * 既に登録されているチャンネルの場合、上書きされる。
	 * @param key チャンネル番号。
	 * @param lo 指定したい信号色の状態。
	 * @throws IllegalArgumentException 番号が0以下の場合
	 */
	public Phase addChannel(long key, ConfigTrafficLight.LightObject lo) throws IllegalArgumentException {
		if (key <= 0) throw new IllegalArgumentException("Key must be greater than 0");
		this.channels.put(key, lo);
		return this;
	}
	
	/**
	 * 指定した番号のチャンネルを登録するが、既にそのチャンネルが存在する場合は例外を出す。
	 * @param key チャンネル番号。
	 * @param lo 指定したい信号色の状態。
	 * @throws IllegalArgumentException 番号が0以下の場合
	 * @throws DataExistException 指定したチャンネルに既に値が入っている場合
	 */
	public Phase addChannelTry(long key, ConfigTrafficLight.LightObject lo) throws IllegalArgumentException, DataExistException {
		if (channels.containsKey(key)) throw new DataExistException("Key" + key + " exist");
		return this.addChannel(key, lo);
		
	}
	
	/**
	 * 指定した番号のチャンネルを取得する。存在しない場合はnullとなる。
	 * @param key チャンネル番号。
	 * @return 登録されていればそのチャンネルの状態、されていなければnull
	 */
	public ConfigTrafficLight.LightObject getChannel(long key) {
		return channels.get(key);
	}
	
	/**
	 * 指定したチャンネルのデータをコピーする。あまり使い道はなさそう。
	 * コピー元が存在しない場合やキーが不正な場合は例外。
	 * @param before コピー元番号
	 * @param after コピー先番号。存在する場合は上書きされる。
	 * @throws IllegalArgumentException キーが0以下だったりコピー元が存在しない場合
	 */
	public Phase copyChannel(long before, long after) throws IllegalArgumentException {
		if (before <= 0 || after <= 0) throw new IllegalArgumentException("Key must be greater than 0");
		if (!channels.containsKey(before)) throw new IllegalArgumentException("Key" + before + "does not exist");
		channels.put(after, channels.get(before));
		return this;
	}
	
	/**
	 * Tickを1個進める。
	 * @return
	 */
	public Phase addTick() {
		this.ticks++;
		return this;
	}
	
	/**
	 * Tickを0に戻す。
	 * @return
	 */
	public Phase resetTick() {
		this.ticks = 0;
		return this;
	}
	
	public long getTick() {
		return this.ticks;
	}
	
	/**
	 * このフェーズの状態を維持するかどうかを返すメソッド。必ず実装する必要がある。
	 * このメソッドは制御機にサイクルが登録されていて、そのサイクルが起動している場合に1Tick毎に呼び出されるため、
	 * できる限り重たい処理は入れずに返すべきである。（I/O処理とか入れるとレスポンスが悪くなる）
	 * trueを返すと、このフェーズは引き続き状態を継続する。falseを返した時点でこのフェーズは終了と判断され、
	 * サイクル側では次のフェーズに移行する。注意点として、永遠にtrueを返せば永遠に切り替わらなくなるので注意。
	 * TickがLongの最大値を超えた場合（だいたい数億年単位なのでそうそう大丈夫だと思うけど）はオーバーフローして負の数に
	 * なるため注意。
	 *
	 * @param controller サイクルが登録されている制御機
	 * @param totalTicks サイクルが起動してからのトータルTick
	 * @param detected 検知信号が受信されたかどうか。
	 * @param world 制御機がある場所のワールドインスタンス。
	 * @return このフェーズを続行するべきならばtrue、終了ならばfalse
	 */
	public abstract boolean shouldContinue(TrafficController controller, long totalTicks, boolean detected, World world);
	
	public HashMap<Long, ConfigTrafficLight.LightObject> getChannels() {
		return channels;
	}
}
