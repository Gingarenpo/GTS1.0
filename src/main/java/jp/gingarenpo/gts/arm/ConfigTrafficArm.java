package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gts.core.config.ConfigBase;

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
	
	protected ArrayList<String> endObject; // 任意
	
	protected String texture;
	
	private static final long serialVersionUID = 1L;
	
	private boolean drawStartPrimary = true; // trueにするとdistanceが0の時startのオブジェクトを優先させる
	
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
	
	public ArrayList<String> getEndObject() {
		return endObject;
	}
	
	public void setEndObject(ArrayList<String> endObject) {
		this.endObject = endObject;
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
	
	public boolean isDrawStartPrimary() {
		return drawStartPrimary;
	}
	
	public void setDrawStartPrimary(boolean drawStartPrimary) {
		this.drawStartPrimary = drawStartPrimary;
	}
	
	@Override
	public String toString() {
		return "ConfigTrafficArm{" +
					   "startObject=" + startObject +
					   ", baseObject=" + baseObject +
					   ", endObject=" + endObject +
					   ", texture='" + texture + '\'' +
					   ", texImage=" + texImage +
					   ", id='" + id + '\'' +
					   ", model='" + model + '\'' +
					   ", size=" + size +
					   '}';
	}
}
