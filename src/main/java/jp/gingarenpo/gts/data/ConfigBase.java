package jp.gingarenpo.gts.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * RTMに近い形で、JSON形式のオブジェクトとしてマッピングするためのクラス。
 * コンフィグとして扱うことになる。（将来的に増えたときの為にベースクラスを作り予約済み）
 *
 * 存在しないプロパティが出てきても無視！
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigBase implements Serializable {
	
	private String id; // コンフィグ同士で重複してはならない固有のID。基本的に全角バイトは含めないこと。
	private String model; // モデルパスを代入する。パックのZIPファイルからの相対パスで指定すること。
	private TexturePath textures; // テクスチャをオブジェクト形式で代入する。
	private ArrayList<LightObject> patterns = new ArrayList<LightObject>(); // 光るパターンを列挙する。ここを動的にすると存在しないパターン名が出てきたとき困るけどそこは考える。RYRとかどうするんだって話ありますし
	private boolean showBoth = true; // これをfalseにすると、「_back」とつけられたオブジェクトを背面灯器と認識し描画しなくなる（ようにしたいために予約）。指定しないとtrue
	private float size = 1; // このモデルを表示するためのサイズ。ブロック単位で指定する。モデルの大きさがこのサイズに収まるように縮小されて描画される。
	private ArrayList<String> baseObject; // 発光しない部分のオブジェクト
	
	public ConfigBase() {
		// JSONデシリアライズの為に必要
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
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
	
	public float getSize() {
		return size;
	}
	
	public void setSize(float size) {
		this.size = size;
	}
	
	public ArrayList<String> getBaseObject() {
		return baseObject;
	}
	
	public void setBaseObject(ArrayList<String> baseObject) {
		this.baseObject = baseObject;
	}
	
	/**
	 * テクスチャのパスを格納するところ。テクスチャの種類がRTMとは違い3種類に増えているので要注意。（2種類でも動きますが）
	 */
	public static class TexturePath implements Serializable {
		
		private String base; // ベースのテクスチャ。信号機で言えば灯体とか取付金具とか
		private String light; // 発光部分のテクスチャで、点灯した際のもの。未指定の場合はベースのテクスチャが使われる。
		private String noLight; // 未点灯状態の発光部分テクスチャ。こちらは任意で、指定がない場合はlightと同じものが使われる。
		
		@JsonIgnore
		private BufferedImage baseTex; // ベースのテクスチャの画像（ゲーム中には変更不可！）
		@JsonIgnore
		private BufferedImage lightTex; // 発光部分のテクスチャ画像
		@JsonIgnore
		private BufferedImage noLightTex; // 未発光部分のテクスチャ画像
		
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
		
		public void setName(String name) {
			this.name = name;
		}
		
		public ArrayList<String> getObjects() {
			return objects;
		}
		
		public void setObjects(ArrayList<String> objects) {
			this.objects = objects;
		}
		
		public boolean equals(LightObject l) {
			return this.objects.equals(l.objects);
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
	}
}
