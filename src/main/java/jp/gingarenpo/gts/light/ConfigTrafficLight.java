package jp.gingarenpo.gts.light;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jp.gingarenpo.gts.core.ConfigBase;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * RTMに近い形で、JSON形式のオブジェクトとしてマッピングするためのクラス。
 * コンフィグとして扱うことになる。（将来的に増えたときの為にベースクラスを作り予約済み）
 *
 * 存在しないプロパティが出てきても無視！
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigTrafficLight extends ConfigBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	private TexturePath textures; // テクスチャをオブジェクト形式で代入する。
	private ArrayList<String> body; // 無点灯パーツ
	private ArrayList<String> light; // 点灯パーツ
	private ArrayList<LightObject> patterns = new ArrayList<LightObject>(); // 光るパターンを列挙する。ここを動的にすると存在しないパターン名が出てきたとき困るけどそこは考える。RYRとかどうするんだって話ありますし
	private boolean showBoth = true; // これをfalseにすると、「_back」とつけられたオブジェクトを背面灯器と認識し描画しなくなる（ようにしたいために予約）。指定しないとtrue
	
	private double[] centerPosition = new double[3]; // モデルの中心ずらし位置。初期値0。XYZで指定、ブロックの1辺＝1とする
	
	public ConfigTrafficLight() {
		// JSONデシリアライズの為に必要
	}
	
	
	
	public TexturePath getTextures() {
		return textures;
	}
	
	public void setTextures(TexturePath textures) {
		this.textures = textures;
	}
	
	public ArrayList<LightObject> getPatterns() {
		return patterns;
	}
	
	public void setPatterns(ArrayList<LightObject> patterns) {
		this.patterns = patterns;
	}
	
	public boolean isShowBoth() {
		return showBoth;
	}
	
	public void setShowBoth(boolean showBoth) {
		this.showBoth = showBoth;
	}
	
	public ArrayList<String> getBody() {
		return body;
	}
	
	public void setBody(ArrayList<String> body) {
		this.body = body;
	}
	
	public ArrayList<String> getLight() {
		return light;
	}
	
	public void setLight(ArrayList<String> light) {
		this.light = light;
	}
	
	public double[] getCenterPosition() {
		return centerPosition;
	}
	
	public void setCenterPosition(double[] centerPosition) {
		this.centerPosition = centerPosition;
	}
	
	public void setCenterPositionX(double x) {
		this.centerPosition[0] = x;
	}
	
	public void setCenterPositionY(double y) {
		this.centerPosition[1] = y;
	}
	
	public void setCenterPositionZ(double z) {
		this.centerPosition[2] = z;
	}
	
	public double getCenterPositionX() {
		return this.centerPosition[0];
	}
	
	public double getCenterPositionY() {
		return this.centerPosition[1];
	}
	
	public double getCenterPositionZ() {
		return this.centerPosition[2];
	}
	
	/**
	 * テクスチャのパスを格納するところ。テクスチャの種類がRTMとは違い3種類に増えているので要注意。（2種類でも動きますが）
	 */
	public static class TexturePath implements Serializable {
		
		private String base; // ベースのテクスチャ。信号機で言えば灯体とか取付金具とか
		private String light; // 発光部分のテクスチャで、点灯した際のもの。未指定の場合はベースのテクスチャが使われる。
		private String noLight; // 未点灯状態の発光部分テクスチャ。こちらは任意で、指定がない場合はlightと同じものが使われる。
		
		@JsonIgnore
		private transient BufferedImage baseTex; // ベースのテクスチャの画像（ゲーム中には変更不可！）
		@JsonIgnore
		private transient BufferedImage lightTex; // 発光部分のテクスチャ画像
		@JsonIgnore
		private transient BufferedImage noLightTex; // 未発光部分のテクスチャ画像
		
		private byte[] baseTexByte;
		private byte[] lightTexByte;
		private byte[] noLightTexByte;
		
		public TexturePath() {
			// こちらもJSONのデシリアライズに必要
		}
		
		public String getBase() {
			return base;
		}
		
		public void setBase(String base) {
			this.base = base;
		}
		
		public String getLight() {
			return light;
		}
		
		public void setLight(String light) {
			this.light = light;
		}
		
		public String getNoLight() {
			return noLight;
		}
		
		public void setNoLight(String noLight) {
			this.noLight = noLight;
		}
		
		public BufferedImage getBaseTex() {
			return baseTex;
		}
		
		public void setBaseTex(BufferedImage baseTex) {
			this.baseTex = baseTex;
		}
		
		public BufferedImage getLightTex() {
			return lightTex;
		}
		
		public void setLightTex(BufferedImage lightTex) {
			this.lightTex = lightTex;
		}
		
		public BufferedImage getNoLightTex() {
			return noLightTex;
		}
		
		public void setNoLightTex(BufferedImage noLightTex) {
			this.noLightTex = noLightTex;
		}
		
		
	}
	
	/**
	 * サイクル名称とそのサイクルの現示の時に光る対象
	 */
	public static class LightObject implements Serializable {
		
		private String name; // 現示名。青とか黄色とかを表すものをつければいいと思う。
		private int tick = 0; // 点滅周期。Tickで指定する。指定がない場合や0の場合は点滅を行わない
		private ArrayList<String> objects; // その現示の際に光らせるオブジェクト名を指定する。配列で指定すると全部光るようになる。
		
		public LightObject() {
			// もうコメントいいよね
		}
		
		public String getName() {
			return name;
		}
		
		public LightObject setName(String name) {
			this.name = name;
			return this;
		}
		
		public ArrayList<String> getObjects() {
			return objects;
		}
		
		public LightObject setObjects(ArrayList<String> objects) {
			this.objects = objects;
			return this;
		}

		
		public int getTick() {
			return tick;
		}
		
		/**
		 * このオブジェクトが指定したTickにおいて消灯状態の場合はtrue
		 * @return
		 */
		public boolean isNoLight(long tick) {
			if (this.tick == 0) return false;
			return Math.floorDiv(tick, this.tick) % 2 == 1; // 点滅は点灯状態から始まり指定したTickの時消灯している場合は結果が奇数となる
		}
		
		@Override
		public String toString() {
			return objects.toString();
		}
		
		/**
		 * インスタンスを複数生成する場合があるので名前の一致とオブジェクトの一致で判断する
		 * @param obj
		 * @return
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof LightObject)) return false;
			LightObject other = (LightObject) obj;
			if (!other.getName().equals(this.name)) return false;
			return Objects.equals(other.objects, this.objects);
		}
	}
}
