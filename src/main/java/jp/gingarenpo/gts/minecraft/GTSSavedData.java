package jp.gingarenpo.gts.minecraft;

import jp.gingarenpo.gts.GTS;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * ワールドデータとは別に、制御機と信号機を含むあらゆるデータを保持するためのもの（予約済み）。
 * ここにリストアップしておかないと、読み込み時にリストセットアップに時間がかかることが多そう。
 */
public class GTSSavedData extends WorldSavedData {
	
	private static final String DATA_NAME = "_GTS"; // データ保存名
	
	/**
	 * 必要
	 */
	public GTSSavedData() {
		super(DATA_NAME);
	}
	
	public GTSSavedData(String name) {
		super(name);
	}
	
	/**
	 * データを呼び出す
	 * @param nbt
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	
	}
	
	/**
	 * データを書き出す
	 * @param compound
	 * @return
	 */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return compound;
	}
	
	/**
	 * Worldに紐づくこのセーブデータを返す。なければ作る。
	 * @param world
	 * @return
	 */
	public static GTSSavedData get(World world) {
		// データがあればそれを読み出すし、なければ作る
		MapStorage ms = world.getMapStorage();
		GTSSavedData wsd = (GTSSavedData) ms.getOrLoadData(GTSSavedData.class, DATA_NAME); // 取得してみる
		if (wsd == null) {
			wsd = new GTSSavedData(); // ないので作る
			ms.setData(DATA_NAME, wsd); // それを登録する
			wsd.setDirty(true); // markDirtyだといつ保存されるかわからないので強制保存
		}
		return wsd;
	}
}
