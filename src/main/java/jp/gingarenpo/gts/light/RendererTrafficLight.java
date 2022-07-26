package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;


/**
 * 信号機を実際に描画するレンダラー
 */
public class RendererTrafficLight extends TileEntitySpecialRenderer<TileEntityTrafficLight> {
	
	// ロケーションはサーバーに保持しても意味がない
	private ResourceLocation baseTex;
	private ResourceLocation lightTex;
	private ResourceLocation noLightTex;
	
	
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
		// long time = System.currentTimeMillis();
		
		if (te.getAddon() == null  || te.getAddon().getModel() == null) {
			// モデルがない場合や描画できない場合
			GTS.GTSLog.warn("Warning. Cannot render model because model is null or not ready to render.");
			return;
		}
		

		
		if (te.getAddon().getConfig().getTextures().getBaseTex() == null || te.getAddon().isNeedChangeTex()) {
			// そもそもテクスチャが用意されていない、あるいは変更が要求された
			if (te.getAddon().getFile() == null) {
				// ダミーを代わりに入れる
				te.setDummyModel();
			}
			te.getAddon().reloadTexture(); // リロード
			baseTex = null;
			lightTex = null;
			noLightTex = null;
			te.getAddon().doneChangeTex();
		}
		
		if (baseTex == null) {
			// 新しくDynamicTextureを用意する
			baseTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId() + "_base", new DynamicTexture(te.getAddon().getConfig().getTextures().getBaseTex()));
		}
		if (lightTex == null) {
			// 新しくDynamicTextureを用意する
			lightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId() + "_light", new DynamicTexture(te.getAddon().getConfig().getTextures().getLightTex()));
		}
		if (noLightTex == null) {
			// 新しくDynamicTextureを用意する
			noLightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId() + "_nolight", new DynamicTexture(te.getAddon().getConfig().getTextures().getNoLightTex()));
		}
		
		// サイクルチェック
		ConfigTrafficLight.LightObject lightObject = null; // 現在光っているべきオブジェクトを格納
		if (te.getData().getLight() != null) {
			// 制御機の情報がまだ入っていない場合や入っていてもサイクルが設定されていない場合はとりあえず何もしない
			// つまりここに来たら必ずサイクルがあり、今光っているものがあるはず
			lightObject = te.getData().getLight();
		}
		
		
		
		// OpenGL準備
		GL11.glPushMatrix(); // 現在の行列情報をスタックに押し込む。これで自由に弄ってもここから戻せば元通り！
		
		GL11.glTranslated(x + 0.5 + te.getAddon().getConfig().getCenterPositionX() * Math.cos(te.getAngle()) + te.getAddon().getConfig().getCenterPositionZ() * Math.sin(te.getAngle()),
				y + 0.5 + te.getAddon().getConfig().getCenterPositionY(),
				z + 0.5 + te.getAddon().getConfig().getCenterPositionZ() * Math.cos(te.getAngle()) + te.getAddon().getConfig().getCenterPositionX() * Math.sin(te.getAngle()));
		GL11.glRotated(te.getAngle(), 0f, 1f, 0f); // 回転させる
		
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		
		// Tessellator 用意
		Tessellator t = Tessellator.getInstance();
		
		
		
		// ベースオブジェクトと消灯オブジェクトの描画
		for (MQOObject o: te.getAddon().getModel().getObjects4Loop()) {
			
			boolean render = false;
			boolean nolight = false; // 消灯か
			// オブジェクト毎に繰り返すが
			if (!te.getAddon().getConfig().getBody().contains(o.getName()) && !te.getAddon().getConfig().getLight().contains(o.getName())) {
				// 描画対象ではない場合
				continue;
			}
			if (te.getAddon().getConfig().getBody().contains(o.getName())) {
				// このオブジェクトは無発光オブジェクトとして描画する
				render = true;
			}
			else {
				// つまりgetLightから撮れるもの、あるいはゴミ
				// 消灯しているものだけを取り出す
				for (ConfigTrafficLight.LightObject l : te.getAddon().getConfig().getPatterns()) {
					// 一致しない場合はスルー
					if (!l.equals(lightObject)) continue;
					// 発光するかしないかを指定（存在するかどうかで決める）
					if (l.getObjects().contains(o.getName())) {
						// 発光オブジェクトの場合
						if (l.isNoLight(te.getWorld().getWorldTime())) {
							render = true;
							nolight = true;
							
						}; // 点滅周期の場合はnolightとして描画
						break;
					}
					else {
						// 未発光オブジェクト確定
						nolight = true;
						render = true;
						break;
					}
				}
			}
			
			
			if (!render) {
				// 描画の必要性がない場合
				continue;
			}
			
			t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
			
			// 実際に描画
			float color = nolight ? te.getAddon().getConfig().getOpacity() : 0.0f;
			
			// テクスチャのバインドを決める
			this.bindTexture(nolight ? noLightTex : baseTex);
			
			o.draw(t.getBuffer(), color);
			t.draw(); // ベース部分を描画
			
		}
		
		
		
		
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		for (MQOObject o: te.getAddon().getModel().getObjects4Loop()) {
			boolean render = false;
			// オブジェクト毎に繰り返す
			if (!te.getAddon().getConfig().getBody().contains(o.getName())) {
				// ライティングが必要な場合
				for (ConfigTrafficLight.LightObject l : te.getAddon().getConfig().getPatterns()) {
					// 一致しない場合はスルー
					if (!l.equals(lightObject)) continue;
					// 発光するかしないかを指定（存在するかどうかで決める）
					if (l.getObjects().contains(o.getName())) {
						// 発光オブジェクト確定
						if (l.isNoLight(te.getWorld().getWorldTime())) continue; // 点滅時は無視
						render = true;
					}
					
				}
			}
			
			this.bindTexture(lightTex);
			
			if (!render) {
				// 描画の必要性がない場合
				continue;
			}
			
			
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f); // 最高の明るさ
			
			// 実際に描画
			float color = 1.0f;
			o.draw(t.getBuffer(), color);
			
			
		}
		
		t.draw(); // ライト部分を描画
		
		// 後片付け
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
		
		// System.out.println((System.currentTimeMillis() - time) + "ms"); // レンダリング性能計測用
	}
	
	
}
