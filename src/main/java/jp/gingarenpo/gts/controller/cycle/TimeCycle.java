package jp.gingarenpo.gts.controller.cycle;

import jp.gingarenpo.gts.controller.TrafficController;
import net.minecraft.world.World;

/**
 * 組み込みサイクルの1つ。指定した時間帯に実行を行わせることができる。
 * ここで指定する時間帯はMinecraft内の時間帯（0～23999）となるが、一応変換メソッドも簡易的に用意している。
 * 基本的に「夜間～」のサイクルを作成するために設定している。
 */
public class TimeCycle extends Cycle {
	
	private static final long serialVersionUID = 1L;
	
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
	
	public TimeCycle(int from, int to) {
		super();
		this.from = from;
		this.to = to;
	}
	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) throws IllegalArgumentException {
		if (from < 0) {
			throw new IllegalArgumentException("From cannot negative");
		}
		else if (from > to) {
			throw new IllegalArgumentException("From value is greater than To");
		}
		this.from = from;
	}
	
	public int getTo() {
		return to;
	}
	
	public void setTo(int to) throws IllegalArgumentException {
		if (to < 0) {
			throw new IllegalArgumentException("To cannot negative");
		}
		else if (from > to) {
			throw new IllegalArgumentException("From value is greater than To");
		}
		this.to = to;
	}
	
	@Override
	public boolean canStart(World world, TrafficController controller, boolean detected) {
		
		
		// 正規化とかは今後対応予定
		if (from < world.getWorldTime() % 24000 && world.getWorldTime() % 24000 < to) {
			// 問答無用でtrue
			return true;
		}
		
		// Toに30000とか入れると24000+6000で翌日扱いする（fromはムリ）
		if (to % 24000 > 0 && to % 24000 > world.getWorldTime() % 24000) {
			return true;
		}
		
		return false;
	}
}
