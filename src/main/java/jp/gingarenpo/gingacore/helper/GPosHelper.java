package jp.gingarenpo.gingacore.helper;

import jp.gingarenpo.gingacore.mqo.MQOVertex;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

/**
 * ゲーム内の座標軸や、ゲーム内におけるブロックの位置などに関するお役立ちメソッドです。
 * もちろんインスタンスを生成することはできません。
 *
 * @author 銀河連邦
 *
 */
public class GPosHelper {

	private GPosHelper() {}; // コンストラクターは起動不可能

	/**
	 *	指定された二つの座標の距離を求めます。絶対値で返却されます。BlockPosの指定のため、座標精度は整数値ですが、
	 *	XYZを考慮した最短距離による距離を導き出します。やっていることは三平方の定理を2回使っているだけです。
	 *	辺経由の距離（所謂道のり）ではないのでご注意ください。
	 * @deprecated これ実はBlockPosに同様のメソッドがあるためあまり必要ないと言えば必要ない
	 * @param p1 1つ目の座標。
	 * @param p2 2つ目の座標。
	 * @return 距離。
	 */
	public static double getDistance(BlockPos p1, BlockPos p2) {
		// 3平方の定理を使用する（斜辺^2 = 他の辺の2乗の和）
		// まずそれぞれの長さを出す
		final int x = Math.abs(p1.getX() - p2.getX());
		final int y = Math.abs(p1.getY() - p2.getY());
		final int z = Math.abs(p1.getZ() - p2.getZ());

		// 次に、XとZに対する直線距離を算出する
		final double xz = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // 平面上の長さ

		// その出した座標に対して、Yと直線距離を算出する
		final double xyz = Math.sqrt(Math.pow(xz, 2) + Math.pow(y, 2)); // これが距離

		return xyz;
	}

	/**
	 * 指定された二つの座標の距離を求めます。こちらはBlockPosと座標値の2つを使用できます。
	 * @param p1 1つ目の座標。
	 * @param x2 2つ目の座標のX座標。
	 * @param y2 2つ目の座標のY座標。
	 * @param z2 2つ目の座標のZ座標。
	 * @return 距離。
	 */
	public static double getDistance(BlockPos p1, int x2, int y2, int z2) {
		return getDistance(p1, new BlockPos(x2, y2, z2));
	}

	/**
	 * 指定された二つの座標の距離を求めます。こちらはすべて座標値で指定します。パラメーターはもう自明なので省略。
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return 距離。
	 */
	public static double getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return getDistance(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
	}

	/**
	 * 指定された全数値において、すべてが一致していればtrueを返します。1つでも異なればfalseを返します。
	 * @param pos 調べたい数値。doubleでいくらでも指定できます。
	 * @return 全てが一致していればtrue、1つでも異なればfalse
	 */
	public static boolean areSameNumber(double ...pos) {
		// 全ての数値が一致していたらtrue
		return areNearNumber(0, pos);
	}
	
	/**
	 * 指定された全数値において、allowの範囲内のズレを無視して一致しているとみなせればtrueを返します。
	 * 一つでも異なればfalseを返します。
	 * @param allow 許容するズレ。
	 * @param pos 調べたい数値。
	 * @return 全てがallow以内のズレに収まっていればtrue
	 */
	public static boolean areNearNumber(double allow, double ...pos) {
		// 誤差プラマイallow以内で一致していたらtrue
		for (int i = 0; i < pos.length - 1; i++) {
			if (pos[i] > pos[i+1] + allow || pos[i] < pos[i+1] - allow) return false;
		}
		return true;
	}
	
	
	/**
	 * 地面との角を求めてcosの値を返します。
	 * @deprecated 合っているかどうかわからないので新規ではできるだけ使わないでください
	 */
	public static double getAngle(double x, double y, double z, double x2, double y2, double z2, double x3, double y3, double z3) {
		// 外積
		double gx = (y - y2) * (z2 - z3) - (y2 - y3) * (z - z2);
		double gy = (z - z2) * (x2 - x3) - (z2 - z3) * (x - x2);
		double gz = (x - x2) * (y2 - y3) - (x2 - x3) * (y - y2);
		// これが法線ベクトルとなる↑
		// 地面の法線ベクトルは1, 0, 0となる（単位ベクトル）
		// 単位ベクトル同士での演算が必要なため単位ベクトルに変換する（ベクトルを長さで割ればいい）
		// cosΘ=abの内積/a*b
		// 内積を求める
		// 絶対値abはそれぞれ3平方の定理で求まる（a = x+y+zの累乗根）
		double abs1 = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2) + Math.pow(gz, 2));
		gx /= abs1;
		gy /= abs1;
		gz /= abs1;
		abs1 = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2) + Math.pow(gz, 2));
		double n = gy;
		
		// System.out.println(gx + ", " + gy + ", " + gz + ", " + abs1 + ", " + n);
		double abs2 = Math.sqrt(1);
		
		return n / (abs1 * abs2);

	}

}
