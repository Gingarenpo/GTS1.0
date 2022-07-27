package jp.gingarenpo.gts.button;

import jp.gingarenpo.gts.core.ConfigBase;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * 押ボタン箱用のコンフィグ。テクスチャくらいしかデフォルトから変更したものはないが、
 * サウンドファイルの項目を一つ用意している。
 */
public class ConfigTrafficButton extends ConfigBase {
	
	/**
	 * 音源ファイルが置かれている場所。ogg推奨。
	 */
	private String soundPath;
	
	/**
	 * この押ボタン箱のテクスチャ。「押される前」と「押された後」のテクスチャを順番に指定する。
	 * 3つ以上書いても取り込まれるが無視される。1つしかない場合はどちらも同じテクスチャが使われる（つまり変化しない！）
	 */
	private ArrayList<String> textures = new ArrayList<>();
	
	/**
	 * この押ボタン箱を描画するために必要なオブジェクト。この中のオブジェクトのみが
	 * 描画される。モデルを使いまわしたい時に便利。
	 */
	private ArrayList<String> objects = new ArrayList<>();
	
	/**
	 * テクスチャに使用するイメージ。
	 */
	private transient BufferedImage[] tex = new BufferedImage[2];
	
	/**
	 * モデルの中心ずらし位置。初期値0。XYZで指定、ブロックの1辺＝1とする
	 */
	private double[] centerPosition = new double[3];
	
	/**
	 * デフォルトコンストラクタはもっぱらJSONシリアライズの為に持っているようなもの
	 */
	public ConfigTrafficButton() {
	
	}
	
	public String getSoundPath() {
		return soundPath;
	}
	
	public void setSoundPath(String soundPath) {
		this.soundPath = soundPath;
	}
	
	public ArrayList<String> getTextures() {
		return textures;
	}
	
	public void setTextures(ArrayList<String> textures) {
		this.textures = textures;
	}
	
	public ArrayList<String> getObjects() {
		return objects;
	}
	
	public void setObjects(ArrayList<String> objects) {
		this.objects = objects;
	}
	
	public void setBaseTex(BufferedImage tex) {
		this.tex[0] = tex;
	}
	
	public void setPushTex(BufferedImage tex) {
		this.tex[1] = tex;
	}
	
	public BufferedImage getBaseTex() {
		return this.tex[0];
	}
	
	public BufferedImage getPushTex() {
		return this.tex[1];
	}
	
	public void setBaseTexture(String baseTexture) {
		if (this.getTextures().size() != 0 && this.getTextures().get(0) != null) {
			this.getTextures().remove(0);
		}
		this.getTextures().add(0, baseTexture);
	}
	
	public void setPushTexture(String pushTexture) {
		if (this.getTextures().size() > 1 && this.getTextures().get(1) != null) {
			this.getTextures().remove(1);
		}
		this.getTextures().add(1, pushTexture);
	}
	
	public String getBaseTexture() {
		if (this.textures.size() == 0) return null;
		return this.getTextures().get(0);
	}
	
	public String getPushTexture() {
		if (this.textures.size() <= 1) return null;
		return this.getTextures().get(1);
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
}
