package jp.gingarenpo.gts.controller.phase;

import jp.gingarenpo.gts.controller.TrafficController;
import net.minecraft.world.World;

import java.io.Serializable;

/**
 * こちらもフェーズの基礎中の基礎。制御機が検知信号を受信するまで待機する。
 * 最低限待ち続けるTick数を指定することができる（例えば、押ボタン式で最低5秒待ちたい時は100Tickとする）
 */
public class UntilDetectPhase extends Phase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long waitTick;
	
	/**
	 * 指定した最低待ち時間をセットしてフェーズを作成する。
	 * @param waitTick
	 */
	public UntilDetectPhase(long waitTick) {
		this.waitTick = waitTick;
	}
	
	/**
	 * このフェーズで特有の保持する内容は存在せず、検知信号がfalseの間は永遠に待ち続ける。
	 * @param controller サイクルが登録されている制御機
	 * @param totalTicks サイクルが起動してからのトータルTick
	 * @param detected 検知信号が受信されたかどうか。
	 * @param world 制御機がある場所のワールドインスタンス。
	 * @return
	 */
	@Override
	public boolean shouldContinue(TrafficController controller, long totalTicks, boolean detected, World world) {
		return detected && ticks > waitTick;
	}
	
	public long getWaitTick() {
		return waitTick;
	}
	
	public void setWaitTick(long waitTick) {
		this.waitTick = waitTick;
	}
}
