package jp.gingarenpo.gts.controller.cycle;

import jp.gingarenpo.gts.controller.TrafficController;
import net.minecraft.world.World;

/**
 * 組み込みサイクルの1つ。指定した時間帯に実行を行わせることができる。
 * ここで指定する時間帯はMinecraft内の時間帯（0～23999）となるが、一応変換メソッドも簡易的に用意している。
 * 基本的に「夜間～」のサイクルを作成するために設定している。
 */
public class TimeCycle extends Cycle {
	
	/**
	 * このサイクルを始めるべき時間帯（Minecraft基準）
	 */
	private int from;
	
	/**
	 * このサイクルを終えるべき時間帯（Minecraft基準）
	 */
	private int to;
	
	/**
	 * 指定した名前で指定した時間帯においてサイクルを作成する。
	 * fromとtoの数値関係がおかしい場合（from>to）は例外を発生させる。
	 * Minecraft時間を超えても構わない。
	 * @param name
	 * @param from
	 * @param to
	 */
	public TimeCycle(String name, int from, int to) {
		super(name);
		this.from = from;
		this.to = to;
	}
	
	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getTo() {
		return to;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	
	@Override
	public boolean canStart(World world, TrafficController controller, boolean detected) {
		// 正規化とかは今後対応予定
		if (from < world.getTotalWorldTime() % 24000) {
			// 開始時刻より前の場合
			if (to / 24000 > 1 && to % 24000 < world.getTotalWorldTime() % 24000) {
				// 終了時刻が翌日に跨る場合で現在時刻がその終了時刻を超えている場合
				return false;
			}
		}
		return true;
	}
}
