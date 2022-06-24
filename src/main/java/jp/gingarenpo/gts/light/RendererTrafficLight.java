package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQOFace;
import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.data.ConfigBase;
import jp.gingarenpo.gts.data.Model;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 信号機を実際に描画するレンダラー
 */
public class RendererTrafficLight extends TileEntitySpecialRenderer<TileEntityTrafficLight> {
	
	private ResourceLocation baseTex;
	private ResourceLocation lightTex;
	private ResourceLocation noLightTex; // それぞれテクスチャ
	
	
	/**
	 * 信号機を実際に描画するためのレンダー。ここでOpenGLに関する描画を呼び出すことができる。
	 * パラメーターは制御機参照
	 * @param te
	 * @param x
	 * @param y
	 * @param z
	 * @param partialTicks
	 * @param destroyStage
	 * @param alpha
	 */
	@Override
	public void render(TileEntityTrafficLight te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha); // 一応
		long time = System.currentTimeMillis();

		
		if (te.getAddon() == null) return; // アドオンがまだ読み込まれていない場合（ダミーでも）は抜ける
		Model addon = te.getAddon(); // Nullでないことが保証される
		ConfigBase config = addon.getConfig(); // これがnullになることはまずない
		
		
		// リソースチェック
		if (baseTex == null) {
			// 存在しない場合のみ追加（IDでテクスチャを管理）
			baseTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("tl_base_" + config.getId(), new DynamicTexture(config.getTextures().getBaseTex()));
		}
		if (lightTex == null) {
			// 存在しない場合のみ追加（IDでテクスチャを管理）
			lightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("tl_light_" + config.getId(), new DynamicTexture(config.getTextures().getLightTex()));
		}
		if (noLightTex == null) {
			// 存在しない場合のみ追加（IDでテクスチャを管理）
			noLightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("tl_noLight_" + config.getId(), new DynamicTexture(config.getTextures().getNoLightTex()));
		}
		
		// サイクルチェック
		ConfigBase.LightObject lightObject = null; // 現在光っているオブジェクトを格納
		if (te.getData().getLight() != null) {
			// 制御機の情報がまだ入っていない場合や入っていてもサイクルが設定されていない場合はとりあえず何もしない
			// つまりここに来たら必ずサイクルがあり、今光っているものがあるはず
			lightObject = te.getData().getLight();
		}
		
		
		
		
		// OpenGL準備
		GL11.glPushMatrix(); // 現在の行列情報をスタックに押し込む。これで自由に弄ってもここから戻せば元通り！
		GL11.glTranslated(x, y, z);
		GL11.glRotated(te.getAngle(), 0, 1, 0); // 回転させる
		GL11.glTranslated(0.5, 0.5, 0); // ブロックの原点を描画対象の座標に移動させる（ただしMQOの性質上原点を中心に移動させる）
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		
		// Tessellator 用意
		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		
		// オブジェクト毎にループ
		for (MQOObject o: addon.getModel().getObjects4Loop()) {
			boolean render = false;
			boolean light = false; // 光るかどうか
			boolean nolight = false; // 光らないかどうか
			// オブジェクト毎に繰り返す
			if (config.getBody().contains(o.getName())) {
				// このオブジェクトは無発光オブジェクトとして描画する
				this.bindTexture(baseTex);
				render = true;
			}
			else {
				// ライティングが必要な場合はちょっと変わる
				for (ConfigBase.LightObject l : config.getPatterns()) {
					// 一致しない場合はスルー
					if (!l.equals(lightObject)) continue;
					// 発光するかしないかを指定（存在するかどうかで決める）
					if (Objects.equals(l, lightObject) && l.getObjects().contains(o.getName())) {
						// 発光オブジェクト確定
						this.bindTexture(lightTex);
						light = true;
						render = true;
					}
					else if (te.getAddon().getConfig().getLight().contains(o.getName())) {
						// 未発光オブジェクト確定
						this.bindTexture(noLightTex);
						nolight = true;
						render = true;
					}
					
					
					
				}
			}
			
			if (!render) {
				// 描画の必要性がない場合
				continue;
			}
			
			
			// 実際に描画
			float color = nolight ? 0.1f : (light ? 1.0f : 0.0f);
			o.draw(t.getBuffer(), color);
			
			
		}
		
		t.draw(); // 描画
		
		// 後片付け
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
		
		System.out.println((System.currentTimeMillis() - time) + "ms");
	}
	
	
}
