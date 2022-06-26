package jp.gingarenpo.gingacore.helper;

import jp.gingarenpo.gingacore.mqo.MQOVertex;

/**
 * 数学的な計算や、算出を必要とする場合のお役立ちメソッド一覧となります。基本的にMinecraftの要素は使用しないので、
 * どのメソッドも独立して使用することができます。
 *
 * なお開発者が数学に疎いので回りくどいことばかりしていますが気になさらずに。
 *
 * @since 2020-12-28
 * @version 2.3～
 */
public class GMathHelper {

	private GMathHelper() {
		// コンストラクタによるインスタンスの生成は禁止します
	}

	/**
	 * 2つの数値の絶対値を加算した結果を返します。ようは、2点間の距離を求めるメソッドです。
	 * @param a 一つ目の値。
	 * @param b 二つ目の値。
	 * @return 2つの値の距離。
	 */
	public static double distance(double a, double b) {
		return Math.abs(a) + Math.abs(b);
	}
	
	/**
	 * 3頂点のXYZ成分それぞれをパラメーターとして受け取り、デフォルトで太陽が真上にあると仮定して（単位ベクトル(0,1,0)で表す）
	 * その単位ベクトルを基準として3頂点により生成できる平面の法線ベクトルを返します。返されるのは配列で、xyzが格納されています。
	 *
	 * 3頂点が同一直線上にある場合は平面を作成できないため例外を投げます。
	 *
	 * @param x1 1つ目の頂点のX座標。
	 * @param y1 1つ目の頂点のY座標。
	 * @param z1 1つ目の頂点のZ座標。
	 * @param x2 2つ目の頂点のX座標。
	 * @param y2 2つ目の頂点のY座標。
	 * @param z2 2つ目の頂点のZ座標。
	 * @param x3 3つ目の頂点のX座標。
	 * @param y3 3つ目の頂点のY座標。
	 * @param z3 3つ目の頂点のZ座標。
	 * @return 指定した3頂点が生成する平面の法線ベクトルとY軸に平行な単位ベクトル
	 * @throws IllegalArgumentException 3頂点が同一直線上にある場合
	 */
	public static double[] getDefaultAreaDirection(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) throws IllegalArgumentException {
		// 各頂点が全て同一直線にないか判断する
		// 同一直線の場合は端点から中点（どれかは不明）の距離が一致することを指す
		double[] distance = new double[] {
				Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2) + Math.pow(z1, 2)),
				Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2) + Math.pow(z2, 2)),
				Math.sqrt(Math.pow(x3, 2) + Math.pow(y3, 2) + Math.pow(z3, 2))
		};
		if ((distance[0] + distance[1] == distance[2]) || (distance[1] + distance[2] == distance[0]) || (distance[2] + distance[0] == distance[1])) {
			throw new IllegalArgumentException("Can't create area from 3 vertex because they are in the same line.");
		}
		
		// ベクトルAB,ACの内積を求める
		double[] ab = new double[] {x2 - x1, y2 - y1, z2 - z1};
		double[] ac = new double[] {x3 - x2, y3 - y2, z3 - z2};
		
		// ABとACの外積を求める
		double[] abc = new double[] {ab[1] * ac[2] - ab[2] * ac[1], ab[2] * ac[0] - ab[0] * ac[2], ab[0] * ac[1] - ab[1] * ac[0]};
		
		// ABCの法線ベクトルを単位ベクトルに修正するために長さを求める
		double abcDistance = Math.sqrt(Math.pow(abc[0], 2) + Math.pow(abc[1], 2) + Math.pow(abc[2], 2));
		
		// Y軸に平行な単位ベクトルは各成分を長さで割ることで求まる
		double[] res = new double[] {abc[0] / abcDistance, abc[1] / abcDistance, abc[2] / abcDistance};
		return res;
		
	}
	
	/**
	 * Minecraft標準のMathHelperは整数にしか対応していないのでdoubleに対応するように自前で作成しました。
	 * 正規化してアングルを0～360の間のどこかで返します。
	 * @param angle 角度
	 * @return 0～360に正規化された値
	 */
	public static double normalizeAngle(double angle) {
		if (angle < 0) {
			angle = Math.abs(angle); // 一旦正の数にして
			return angle > 360 ? (360 - (angle % 360.0)) : 360 - angle;
		}
		else {
			return angle > 360 ? angle % 360.0 : angle;
		}
	}
}
