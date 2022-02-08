package jp.gingarenpo.gingacore.helper;

/**
 * レンダリングに関係するお役立ちメソッドを集めている所となります。
 * というか車輪の再発明の気しかしないので、有識者はここを使わずにOpenGLごり押ししてください。
 */
public class GRenderHelper {
	
	private GRenderHelper() {} // コンストラクターは隠蔽
	
	/**
	 * レンダリングをする際に、影の値を計算して返してくれます。
	 * 具体的には、頂点カラーの黒成分を増やすことで擬似的な影を表現することができます。
	 * スムージングは面の細かさによって左右されますが、独自シェーダーとして使用するために残しています。
	 *
	 * 計算方法としては、ある面に対する法線ベクトルを求め、その法線ベクトルと日照角の差から
	 * 求めています。何言っているかわかんない場合はもう素直にこのメソッドの答えを受け入れるか、
	 * 自力で頑張って実装するか、あるいは無視してください。製作者もよくわかっていないので。
	 *
	 * @param face 法線ベクトルの角度。radで指定すること。
	 * @param sun 日照角。worldインスタンスのメソッドでこれを入手することができるが、光源を指定する場合はその光源がある位置と地面からの入社角を指定する。
	 * @return 0.0～1.0の間の数値。この値をglColor関連のメソッドで指定することができる。
	 */
	public static double getLightValue(double face, double sun) {
		sun = (sun + 0.5 * Math.PI) % (1.0 * Math.PI);
		if (sun - face > 2 * Math.PI) {
			// 差が90度を超えた場合はそもそも光らない
			return 0;
		}
		//System.out.println((sun / Math.PI) + ", " + (face / Math.PI));
		return Math.sin(Math.abs(sun - face)); // この計算式で求まるはず
	}
}
