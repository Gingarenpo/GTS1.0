package jp.gingarenpo.gingacore.mqo;

/**
 * 面の描画で使用する頂点情報を格納したクラスです。Vectorと同じだけど独自機能追加していくつもりなので一応…
 *
 * @author 銀河連邦
 */
public class MQOVertex {

	private double x;
	private double y;
	private double z; // 以上、座標数値

	private final MQOObject mqo; // 親オブジェクト

	public MQOVertex(MQOObject mqo, String vnum) {
		// MQOの頂点記述方式に従って格納
		// 0.12345 0.23456 0.34567と3つの数値がスペースで区切られている
		// 正規化してあること前提での処理
		this.mqo = mqo; // 代入

		final String[] v = vnum.split(" "); // 分割して…
		//System.out.println("Vertex["+v.length+"]");
		if (v.length != 3) // MQOとして不適切
			throw mqo.getParent().new MQOException("Illegal Vertex Position!!");

		// 代入していく
		x = Double.parseDouble(v[0]);
		y = Double.parseDouble(v[1]);
		z = Double.parseDouble(v[2]);

		// 終わり
	}

	// 正規化に必要なためsetも入れているが外部からの呼び出しは不可能（同一パッケージ内のみ）

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	void setX(double x) {
		this.x = x;
	}

	void setY(double y) {
		this.y = y;
	}

	void setZ(double z) {
		this.z = z;
	}



	/**
	 * この頂点の属するMQOオブジェクトを返します。
	 *
	 * @return MQOオブジェクト
	 */
	public MQOObject getObject() {
		return mqo;
	}
}
