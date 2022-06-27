package jp.gingarenpo.gts.core;

import net.minecraft.nbt.NBTTagCompound;

/**
 * このインターフェースを実装すると、TileEntity以外のクラスにおいてその中身を
 * NBTタグから読み込むことができる。INBTWritableと一緒に使うことを想定している。
 */
public interface INBTReadable {
	
	/**
	 * NBTからデータを読み込む。
	 * @param compound タグ
	 */
	public void readFromNBT(NBTTagCompound compound);
	
}
