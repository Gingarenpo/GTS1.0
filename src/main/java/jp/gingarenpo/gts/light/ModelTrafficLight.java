package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.ModelBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * モデルセットの一覧。パックには複数あることがあるよ
 */
public class ModelTrafficLight extends ModelBase<ConfigTrafficLight> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ResourceLocation baseTex;
	public ResourceLocation lightTex;
	public ResourceLocation noLightTex; // それぞれテクスチャ
	
	public ModelTrafficLight(ConfigTrafficLight config, MQO model, File file) {
		super(config, model, file);
	}
	
	/**
	 * 強制的にテクスチャを再読み込みする
	 * Loaderにある必要があり
	 */
	public void redrawTexture() {
		if (config == null) {
			GTS.GTSLog.warn("Warning. config is null. It may be broken packet.");
			return;
		}
		
		HashMap<String, BufferedImage> textures = GTS.loader.getTextures().get(file);
		if (textures == null) {
			GTS.GTSLog.warn("Warning. texture is null. It may be not exist pack.");
			return;
		}
		
		// 全テクスチャを差し替える
		BufferedImage base = textures.get(config.getTextures().getBase());
		BufferedImage light = textures.get(config.getTextures().getLight());
		BufferedImage nolight = textures.get(config.getTextures().getNoLight());
		
		if (base != null) {
			config.getTextures().setBaseTex(base);
			baseTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(config.getId() + "_base", new DynamicTexture(base));
		}
		else {
			GTS.GTSLog.warn("Warning. base texture is null. It may be not exist pack.");
		}
		if (light != null) {
			config.getTextures().setBaseTex(light);
			lightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(config.getId() + "_light", new DynamicTexture(light));
		}
		else {
			GTS.GTSLog.warn("Warning. light texture is null. It may be not exist pack.");
		}
		if (nolight != null) {
			config.getTextures().setBaseTex(nolight);
			noLightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(config.getId() + "_nolight", new DynamicTexture(nolight));
		}
		else {
			GTS.GTSLog.warn("Warning. nolight texture is null. It may be not exist pack.");
		}
		
	}
}
