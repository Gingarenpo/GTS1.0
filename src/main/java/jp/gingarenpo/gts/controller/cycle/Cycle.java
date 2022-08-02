package jp.gingarenpo.gts.controller.cycle;

import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.controller.phase.Phase;
import jp.gingarenpo.gts.core.json.Exclude;
import jp.gingarenpo.gts.light.ConfigTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * サイクルを保持するクラス。
 * サイクルは、1つ以上のフェーズを持つ。制御機に登録できる1パターンの制御を表す。
 * サイクル名などは半角英数字を受け付ける（日本語まで対応するとめんどくさい）
 */
public class Cycle implements Serializable {
	
	@Exclude
	private static final long serialVersionUID = 1L;
	
	/**
	 * このサイクルの名称。サイクルは必ず1つの名前を持つ。
	 */
	protected String name;
	
	/**
	 * このサイクルが持つフェーズの一覧。Phasesクラスをインスタンスとして持つ。
	 */
	protected ArrayList<Phase> phases = new ArrayList<Phase>();
	
	/**
	 * 現在表示中のフェーズ番号
	 */
	protected int nowPhase = 0;
	
	/**
	 * デフォルトコンストラクタは用意するものの、基本的には名称を指定して欲しい。
	 */
	public Cycle() {
		this(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()) + RandomStringUtils.randomAlphanumeric(24));
	}
	
	/**
	 * 指定した名称のサイクルインスタンスを作成する。
	 * 後からPhaseを挿入することができる。
	 * 同時に指定したい時は別のコンストラクタを使用する。
	 * @param name このサイクルの名前。制御機単位で固有である必要がある。
	 */
	public Cycle(String name) {
		this.name = name;
	}
	
	/**
	 * 指定した名称のサイクルインスタンスを作成して、phasesのフェーズを一括で登録する。
	 * 指定されたフェーズをデフォルトで登録状態にするためすぐに起動できる利点がある。
	 * @param name このサイクルの名前。
	 * @param phases 登録したいフェーズ。
	 */
	public Cycle(String name, ArrayList<Phase> phases) {
		this(name);
		this.phases = phases;
	}
	
	/**
	 * このサイクルが開始できるかを返す。このメソッドは継承先のクラスでオーバーライドすべきである。
	 * このメソッドは、登録されている制御機がある場合にその制御機が呼び出す。
	 * 制御機内で動かしているとあるサイクルが終了した場合（もしくは初回起動）、登録されている全サイクルに対して
	 * このメソッドを呼び出す。もし、その中でtrueを返すものがあればそのサイクルを起動する。
	 * 同一の制御機に登録されたサイクルが2つ以上trueを返した際は、最初に登録されたサイクルが起動するが、
	 * この仕組みは変更される可能性があるため通常は各サイクルが排他条件を満たすように改良すべきである。
	 *
	 * 天候や時間などの条件を取りたい場合を考えて、Worldインスタンスを渡すようにしているが、
	 * この引数は場合によってはnullを返すことがあるため要注意。サーバーとクライアントでの実行が
	 * 被ると発生する可能性がある。なお、基本的にこのメソッドはサーバー側のみで実行されることになっている。
	 *
	 * @param world 制御機が設置されているワールドのインスタンス。
	 * @param controller　登録されている制御機。
	 * @param detected 検知信号が送信されたかどうか。押ボタン箱・感知器が信号を送信した場合にtrueとなる。
	 * @return このサイクルを起動可能なのであればtrue、そうでなければfalseを返す。
	 */
	public boolean canStart(World world, TrafficController controller, boolean detected) {
		return true;
	}
	
	/**
	 * このサイクルの名称を返す。
	 * @return サイクルの名称
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * このサイクルに登録されたフェーズたちを返す。
	 * @return フェーズ一覧
	 */
	public ArrayList<Phase> getPhases() {
		return phases;
	}
	
	/**
	 * 指定した名称に変更する。日本語非推奨（ただしチェックしない）。
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 指定したフェーズに登録を上書きする。通常このメソッドは使用すべきではない。
	 * 別に用意してあるaddPhaseなどを使用するべき。
	 * @param phases
	 */
	public void setPhases(ArrayList<Phase> phases) {
		this.phases = phases;
	}
	
	/**
	 * 指定した番号のフェーズを返す。ない場合はnullを返す。
	 * @param key フェーズ番号（登録順に0-1-2…となっている）
	 * @return あればそれ、なければnull
	 */
	public Phase getPhase(int key) {
		try {
			return phases.get(key);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * 指定したフェーズを追加する。
	 * @param phase
	 */
	public void addPhase(Phase phase) {
		this.phases.add(phase);
	}
	
	/**
	 * 現在現示しているフェーズを返す。起動していない場合は0番目（最初）のフェーズが返る。
	 * フェーズが1個も登録されていない場合はnullを返す。
	 * @return
	 */
	public Phase getNowPhase() {
		try {
			if (this.nowPhase > this.phases.size()) {
				this.nowPhase = 0; // 辻褄が合わない場合は強制的に戻す
			}
			return phases.get(this.nowPhase);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 現在のフェーズ番号を返す。
	 * @return
	 */
	public int getNowPhaseNumber() {
		return nowPhase;
	}
	
	/**
	 * 指定したフェーズを現在の現示として強制セットする。
	 * 存在しない（登録していない）フェーズを指定した場合は例外が返る。
	 * 通常はこちらは使用せず、専用メソッドを使うこと。どうしようもなくなった場合のみこちらを使う。
	 * @param nowPhase 指定したい現示番号
	 * @throws IllegalArgumentException 存在しない場合
	 */
	public void setNowPhaseNumber(int nowPhase) throws IllegalArgumentException {
		if (this.nowPhase >= phases.size()) throw new IllegalArgumentException("phase" + nowPhase + " does not exist");
		this.nowPhase = nowPhase;
	}
	
	/**
	 * 現在の現示がこのサイクルの最後のフェーズであればtrueを返す。
	 * この現示がtrueであるときにフェーズを新規追加して再度実行するとfalseになるので注意
	 * @return 最後の現示ならtrue、そうでないならばfalse
	 */
	public boolean isLast() {
		return nowPhase == phases.size()-1;
	}
	
	/**
	 * フェーズを次のものに移行させる。ラストだった場合はこのメソッドは何もしない。
	 * ラストの場合にリセットしたい場合は別メソッドを使う。ただし継続要求をされている場合はfalseを返し変更を行わない。
	 */
	public boolean nextPhase(TrafficController controller, World world) {
		if (getNowPhase() == null) return true;
		if (getNowPhase().shouldContinue(controller, controller.getTicks(), controller.isDetected(), world)) {
			getNowPhase().addTick();
			return false;
		}
		if (!isLast()) {
			getNowPhase().resetTick();
			nowPhase++;
			for (TileEntityTrafficLight tl : controller.getTrafficLights()) {
				// アタッチしている信号機に送信する
				ConfigTrafficLight.LightObject l = getNowPhase().getChannel(tl.getData().getSignal());
				if (l == null) continue;
				tl.getData().setLight(l);
			}
			// GTS.GTSLog.debug(String.format("<%s_%s> Change Phase %s to %s", controller.getName(), this.name, getPhase(nowPhase-1).getName(), getNowPhase().getName()));
			return true;
		}
		return false;
	}
	
	/**
	 * サイクル終了時に呼び出されることを想定している。フェーズを一番最初にリセットする。
	 * どんな状態でも一番最初に戻される。ただし継続要求をされている場合はfalseを返し変更を行わない。
	 */
	public boolean resetPhase(TrafficController controller, World world) {
		if (getNowPhase().shouldContinue(controller, controller.getTicks(), controller.isDetected(), world)) {
			getNowPhase().addTick();
			return false;
		}
		getNowPhase().resetTick();
		nowPhase = 0;
		for (TileEntityTrafficLight tl : controller.getTrafficLights()) {
			// アタッチしている信号機に送信する
			ConfigTrafficLight.LightObject l = getNowPhase().getChannel(tl.getData().getSignal());
			if (l == null) continue;
			tl.getData().setLight(l);
		}
		// GTS.GTSLog.debug(String.format("<%s_%s> Reset Phase %s to 0", controller.getName(), this.name, getNowPhase().getName()));
		return true;
	}
	
	/**
	 * フェーズを次のものに移行させる。ラストだった場合は自動的にリセットする。
	 * こちらはnextが実行されるとtrue、resetだとfalseになる。
	 * フェーズ継続だとしてもこうなるので注意
	 *
	 * @param controller 制御機のインスタンス
	 * @param world ワールドインスタンス
	 * @return 上記参照
	 */
	public boolean nextOrResetPhase(TrafficController controller, World world) {
		if (isLast()) {
			resetPhase(controller, world);
			return false;
		}
		nextPhase(controller, world);
		return true;
	}

}
