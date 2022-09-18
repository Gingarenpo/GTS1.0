package jp.gingarenpo.gts.sign.data;

import net.minecraft.client.renderer.Tessellator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 所謂地名板、青看板、機種銘板などを作成するためのもの。
 * これ自体にモデルは存在せず、コンフィグのみとなるため他の物とは違い保存方法が異なる。
 * このクラス自体は抽象クラスなので、オーバーライドして使用する必要がある。
 * 将来的に、クラスの動的読み込みでこの部分を拡張できるように対応する予定がある。
 *
 * なおモデルパックからコンフィグファイルなどを読み込む必要がないため
 * このインスタンス自体がコンフィグの役割を果たしている。
 */
public abstract class TrafficSign implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * ハッシュマップとして、コンフィグに必要な要素を読み込ませる。
	 * この中に必要な要素を代入していく（コンストラクタでやるのが望ましい）。
	 * 初期は空。この中身はGUIにより操作することができる。
	 * 中身はシリアライズ可能なものを入れる必要がある！！
	 *
	 * コンフィグのデフォルト値説明
	 * width => 配置されるときの幅（単位はブロック）
	 * height => 配置されるときの高さ（単位はブロック）
	 * color => 基本色（デフォルト白、JavaのAwtのカラーで指定）
	 * font => フォント
	 * x,y,z = ズレ（1ブロックを基準として）
	 */
	protected LinkedHashMap<String, Serializable> config = new LinkedHashMap<>();
	
	public TrafficSign() {
		config.put("width", 1.0f);
		config.put("height", 0.5f);
		config.put("color", Color.WHITE);
		config.put("font", null);
		config.put("X", 0f);
		config.put("Y", 0f);
		config.put("Z", 0f);
	}
	
	/**
	 * 現在このインスタンスが保持しているコンフィグの情報を元にして、
	 * 実際の看板のテクスチャを作成してそのBufferedImageを返す。
	 * このテクスチャサイズは必ずしもコンフィグのサイズ情報と一致していなくても構わないが、
	 * その場合は引き延ばされる。このメソッドは実装必須となっており、ほかのメソッドが
	 * 実装されていない場合はこのメソッドが代わりに使われる。
	 * @return この看板が返すべきテクスチャ（表）。Nullを返してはならない
	 */
	public abstract BufferedImage createMainTexture();
	
	/**
	 * 現在のコンフィグ情報をもとにして、この看板の裏側のテクスチャを作成し、その
	 * BufferedImageを返す。createMainTextureの裏版だと思えばいい。
	 * このメソッドもnullを返すことは許されないが、裏側が存在しない場合もあるため
	 * デフォルトでは背景色で塗りつぶしたものを返す。
	 *
	 * @return この看板の裏側のテクスチャ。
	 */
	public BufferedImage createBackTexture() {
		BufferedImage buf = new BufferedImage(16, 16, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = buf.createGraphics();
		g.setColor((Color) config.get("color"));
		g.fillRect(0, 0, 16, 16);
		return buf;
	}
	
	/**
	 * 表を描画する。
	 *
	 * このメソッドはレンダリングクラスから呼び出され、OpenGLが使用可能。既にtranslateは済んでいるため、
	 * たんに描画するメソッドを使用することが可能。Matrixのリセット処理などはせずに、0-1に正規化された座標系を使用可能。
	 * 引数としてBufferをもつ
	 * なおテクスチャは既にバインドされているため、UVを指定すれば描画可能。
	 */
	public abstract void renderMain(Tessellator t);
	
	/**
	 * 裏面を描画する。renderMain参照
	 */
	public abstract void renderBack(Tessellator t);
	
	/**
	 * ハッシュマップ自体を返却する。通常は使わない。
	 * @return
	 */
	public LinkedHashMap<String, Serializable> getConfig() {
		return config;
	}
	
	/**
	 * コンフィグのキーを元にして値を取得する。ない場合はnullとなる。
	 * @param key コンフィグキー
	 * @return あればその値、なければnull
	 */
	public Serializable getValue(String key) {
		return config.get(key);
	}
	
	/**
	 * コンフィグのキーを基にして値を取得する。ない場合はvで指定したものを返す。
	 * @param key コンフィグキー
	 * @param v ない場合のデフォルト値
	 * @return あればその値、なければデフォルト値
	 */
	public Serializable getValueOrDefault(String key, Serializable v) {
		return config.getOrDefault(key, v);
	}
	
	/**
	 * 指定したキーの情報を入れ替える。存在しないキーを指定した場合は例外を発生させる
	 * @param key 入れ替えたいキー。
	 * @param value 入れ替えたい値。
	 */
	public void setValue(String key, Serializable value) throws IllegalArgumentException {
		if (!setValueTry(key, value)) throw new IllegalArgumentException(String.format("Key %s is not found.", key));
	}
	
	/**
	 * 指定したキーの情報を入れ替えようとする。キーが存在しない場合は入れ替えを行わない。
	 * @param key キー
	 * @param value 値
	 * @return 入れ替えに成功したらtrue、失敗したらfalse
	 */
	public boolean setValueTry(String key, Serializable value) {
		if (!config.containsKey(key)) {
			return false;
		}
		config.replace(key, value);
		return true;
	}
	
	/**
	 * コンフィグをループで回せるような形にして返す。
	 * @return セット
	 */
	public Set<Map.Entry<String, Serializable>> getConfig4Loop() {
		return config.entrySet();
	}
	
	@Override
	public String toString() {
		return "TrafficSign{" +
					   "config=" + config +
					   '}';
	}
}
