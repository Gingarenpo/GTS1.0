package jp.gingarenpo.gts.event;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.minecraft.GTSSavedData;
import net.minecraft.world.DimensionType;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * ワールドの保存や読み込み等を行うイベントを集めたものとなる。
 * 実際にこのイベントはGTS本体でイベントを登録しないと使用できない。
 * GTS用のセーブデータを読み込ませる
 */
public class GTSWorldEvent {
	
	public GTSWorldEvent() {
	
	}
	
	/**
	 * ワールドが読み込まれたときに発動するイベント。
	 * @param e
	 */
	@SubscribeEvent
	public void onWorldLoaded(WorldEvent.Load e) {
		// 各ディメンションごとに呼び出されてしまうので、地上世界の場合にのみ読み込む。
		// まさかネザーで信号機を稼働させる人なんていないでしょ。
		if (e.getWorld().provider.getDimensionType() != DimensionType.OVERWORLD || e.getWorld().isRemote) {
			return; // ちなみにサーバークライアントでもここは何回も着火されてしまうので1回に絞る。
		}
		
		GTS.data = GTSSavedData.get(e.getWorld()); // 代入する
	}
	
	/**
	 * ワールドを保存するときに読み込まれるイベント。
	 * @param e
	 */
	@SubscribeEvent
	public void onWorldSaved(WorldEvent.Save e) {
		// こちらも多重に呼ばれるので
		if (e.getWorld().provider.getDimensionType() != DimensionType.OVERWORLD || e.getWorld().isRemote) {
			return; // ちなみにサーバークライアントでもここは何回も着火されてしまうので1回に絞る。
		}
		
		// データを強制保存する
		GTS.INSTANCE.data.setDirty(true); // 強制保存
	}
	
	/**
	 * ワールドが読み込まれなくなった時に廃棄するためのものだが特に現在処理はない
	 * @param e
	 */
	@SubscribeEvent
	public void onWorldUnloaded(WorldEvent.Unload e) {}
	
	/**
	 * チャンクが読み込まれたときに実行されるものだがこちらも特に処理は存在しない。
	 * GTC時代はここでチャンク内にある制御機を調べ上げてリストの追加削除をしていたが、
	 * 非常に重たい処理なので考えどころ。不都合が生じないレベルになったらとりあえず
	 * ここはなしでいいかと考えている。
	 * @param e
	 */
	@SubscribeEvent
	public void onChunkLoaded(ChunkEvent.Load e) {}
}
