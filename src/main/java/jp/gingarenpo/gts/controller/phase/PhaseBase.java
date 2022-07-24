package jp.gingarenpo.gts.controller.phase;

import jp.gingarenpo.gts.controller.TrafficController;
import net.minecraft.world.World;

/**
 * GUIで指定することができるベースクラス。フェーズの基礎中の基礎で、
 * 指定したチャンネルに対して一定の信号を一定時間送信するだけのものとなる。
 */
public class PhaseBase extends Phase {
	
	/**
	 * どの長さまでTickを実行させるか
	 */
	private int continueTick;
	
	/**
	 * 指定した名称でフェーズを作成する。
	 *
	 * @param name 名前
	 */
	public PhaseBase(String name, int continueTick) {
		super(name);
		this.continueTick = continueTick;
	}
	
	public PhaseBase(int continueTick) {
		super();
		this.continueTick = continueTick;
	}
	
	@Override
	public boolean shouldContinue(TrafficController controller, long totalTicks, boolean detected, World world) {
		if (ticks > continueTick) return false;
		return true;
	}
	
	public int getContinueTick() {
		return continueTick;
	}
	
	public void setContinueTick(int continueTick) {
		this.continueTick = continueTick;
	}
}
