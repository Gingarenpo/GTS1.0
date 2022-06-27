package jp.gingarenpo.gts.core;

import net.minecraft.nbt.NBTTagCompound;

/**
 * このインターフェースを実装すると、NBTタグとして現在の内容を書き出せることを明示できる。
 */
public interface INBTWritable {
	
	/**
	 * 引数で渡されたタグに、書き込む。
	 * @param compound タグ
	 * @return 書き込まれたタグ
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound compound);
}
