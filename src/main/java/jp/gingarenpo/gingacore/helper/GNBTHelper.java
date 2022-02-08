package jp.gingarenpo.gingacore.helper;

import net.minecraft.nbt.NBTTagCompound;

/**
 * NBTタグ（NBTCompound）クラスにおけるヘルパーメソッドです。インスタンスを生成することはできません。
 * 全て静的なアクセスが必要です。また、必要最低限しか書いていないのでほしけりゃアルゴリズム利用して追記なり
 * なんなりしてください。
 *
 * @author 銀河連邦
 *
 */
public class GNBTHelper {

	private GNBTHelper() {}; // コンストラクタ生成不可能

	/**
	 * 指定したNBTタグの中から、指定したキーに含まれる整数値を取り出します。その値が存在しない場合にデフォルトは
	 * 0を返しますが、ここでは数を指定することができます。
	 * @param nbt 取り出したいNBTタグ。
	 * @param key 取り出すキー。
	 * @param value 取り出すキーが割り当てられていなかった時の代わりの値。
	 * @return
	 */
	public static int getIntegerWithValue(NBTTagCompound nbt, String key, int value) {
		if (nbt.hasKey(key)) {
			return nbt.getInteger(key);
		}
		else {
			return value;
		}
	}

	/**
	 * 指定したNBTタグの中から、指定したキーに含まれる文字列を取り出します。その値が存在しない場合にデフォルトは
	 * 空文字を返しますが、ここでは文字列を指定することができます。
	 * @param nbt 取り出したいNBTタグ。
	 * @param key 取り出すキー。
	 * @param value 取り出すキーが割り当てられていなかった時の代わりの値。
	 * @return
	 */
	public static String getStringWithValue(NBTTagCompound nbt, String key, String value) {
		if (nbt.hasKey(key)) {
			return nbt.getString(key);
		}
		else {
			return value;
		}
	}

	/**
	 * 指定したNBTタグの中から、指定したキーに含まれるfloat値を取り出します。その値が存在しない場合にデフォルトは
	 * 0を返しますが、ここでは数を指定することができます。
	 * @param nbt 取り出したいNBTタグ。
	 * @param key 取り出すキー。
	 * @param value 取り出すキーが割り当てられていなかった時の代わりの値。
	 * @return
	 */
	public static float getFloatWithValue(NBTTagCompound nbt, String key, float value) {
		if (nbt.hasKey(key)) {
			return nbt.getFloat(key);
		}
		else {
			return value;
		}
	}

	/**
	 * 指定したNBTタグの中から、指定したキーに含まれるdouble値を取り出します。その値が存在しない場合にデフォルトは
	 * 0を返しますが、ここでは数を指定することができます。
	 * @param nbt 取り出したいNBTタグ。
	 * @param key 取り出すキー。
	 * @param value 取り出すキーが割り当てられていなかった時の代わりの値。
	 * @return
	 */
	public static double getIntegerWithValue(NBTTagCompound nbt, String key, double value) {
		if (nbt.hasKey(key)) {
			return nbt.getDouble(key);
		}
		else {
			return value;
		}
	}

	/**
	 * 指定したNBTタグの中から、指定したキーに含まれる論理値を取り出します。その値が存在しない場合にデフォルトは
	 * falseを返しますが、ここではtrueも指定することができます。
	 * @param nbt 取り出したいNBTタグ。
	 * @param key 取り出すキー。
	 * @param value 取り出すキーが割り当てられていなかった時の代わりの値。
	 * @return
	 */
	public static boolean getBooleanWithValue(NBTTagCompound nbt, String key, boolean value) {
		if (nbt.hasKey(key)) {
			return nbt.getBoolean(key);
		}
		else {
			return value;
		}
	}
}
