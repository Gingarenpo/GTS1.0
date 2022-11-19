package jp.gingarenpo.gts.sign;

import jp.gingarenpo.gts.sign.data.TrafficSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class RendererTrafficSign extends TileEntitySpecialRenderer<TileEntityTrafficSign> {
	
	/**
	 * テクスチャ。
	 * 0=表、1=裏（裏は存在しない場合がある）
	 */
	public HashMap<String, ResourceLocation[]> textures = new HashMap<>();
	
	@Override
	public void render(TileEntityTrafficSign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		
		if (te.getData() == null) return; // 描画のしようがない
		
		// テクスチャをバインドする
		if (!textures.containsKey(te.getName()) || te.isTexChange()) {
			// キーが存在しない場合はテクスチャを作ってリソースを格納する
			TrafficSign ts = te.getData();
			BufferedImage main = ts.createMainTexture();
			BufferedImage sub = ts.createBackTexture();
			textures.put(te.getName(), new ResourceLocation[] {
					Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("sign_" + te.getName(), new DynamicTexture(main)),
					Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("sign_" + te.getName(), new DynamicTexture(sub))
			});
			te.setTexChange(false);
		}
		
		// 移動準備
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		// 回転準備
		GL11.glTranslated(0.5, 0.5, 0.5);
		GL11.glRotated(te.getAngle(), 0, 1, 0);
		GL11.glTranslated(-0.5, -0.5, -0.5);
		
		GlStateManager.disableLighting();
		
		
		// 描画を開始する
		Tessellator t = Tessellator.getInstance();
		
		this.bindTexture(textures.get(te.getName())[0]); // 表のテクスチャ
		te.getData().renderMain(t);
		this.bindTexture(textures.get(te.getName())[1]); // 表のテクスチャ
		te.getData().renderBack(t);
		
		// 終わり
		GL11.glPopMatrix();
	}
	
	@Override
	public boolean isGlobalRenderer(TileEntityTrafficSign te) {
		return true;
	}
}
