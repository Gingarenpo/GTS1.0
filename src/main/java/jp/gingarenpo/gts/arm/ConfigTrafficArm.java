package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gts.core.ConfigBase;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * アームのコンフィグは、ポールから（X＝正の方向に）伸びるための指定を行う。
 *
 */
public class ConfigTrafficArm extends ConfigBase implements Serializable {
	
	protected ArrayList<String> startObject;
	
	protected ArrayList<String> baseObject;
	
	protected String texture;
	
	/**
	 * テクスチャ格納するところ
	 */
	protected transient BufferedImage texImage;
	
	public ArrayList<String> getStartObject() {
		return startObject;
	}
	
	public void setStartObject(ArrayList<String> startObject) {
		this.startObject = startObject;
	}
	
	public ArrayList<String> getBaseObject() {
		return baseObject;
	}
	
	public void setBaseObject(ArrayList<String> baseObject) {
		this.baseObject = baseObject;
	}
	
	public BufferedImage getTexImage() {
		return texImage;
	}
	
	public void setTexImage(BufferedImage texImage) {
		this.texImage = texImage;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public void setTexture(String texture) {
		this.texture = texture;
	}
}
