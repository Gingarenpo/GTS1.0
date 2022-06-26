package jp.gingarenpo.gts.pole;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jp.gingarenpo.gts.core.ConfigBase;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * ポールのコンフィグは、主にパーツの名前とテクスチャの場所を指すためのものである。
 * テクスチャは初回ロード時にセットするとする。
 * パーツ欄には、それぞれ上端と下のオブジェクト名、真ん中はそれを別のオブジェクト名として文字列で記す。
 * 流用OK。配列で格納される。
 */
public class ConfigTrafficPole extends ConfigBase implements Serializable {
	
	/**
	 * テクスチャの場所。
	 */
	private String texture;
	
	/**
	 * テクスチャの実際の中身。
	 */
	@JsonIgnore
	private transient BufferedImage texImage;
	
	/**
	 * 通常使用するオブジェクト名の配列。
	 */
	private ArrayList<String> baseObject = new ArrayList<>();
	
	/**
	 * 土台となる部分のオブジェクト名の配列。
	 */
	private ArrayList<String> bottomObject = new ArrayList<>();
	
	/**
	 * 通常使用するオブジェクト名の配列。
	 */
	private ArrayList<String> topObject = new ArrayList<>();
	
	public ConfigTrafficPole() {}
	
	public String getTexture() {
		return texture;
	}
	
	public void setTexture(String texture) {
		this.texture = texture;
	}
	
	public BufferedImage getTexImage() {
		return texImage;
	}
	
	public void setTexImage(BufferedImage texImage) {
		this.texImage = texImage;
	}
	
	public ArrayList<String> getBaseObject() {
		return baseObject;
	}
	
	public void setBaseObject(ArrayList<String> baseObject) {
		this.baseObject = baseObject;
	}
	
	public ArrayList<String> getBottomObject() {
		return bottomObject;
	}
	
	public void setBottomObject(ArrayList<String> bottomObject) {
		this.bottomObject = bottomObject;
	}
	
	public ArrayList<String> getTopObject() {
		return topObject;
	}
	
	public void setTopObject(ArrayList<String> topObject) {
		this.topObject = topObject;
	}
}
