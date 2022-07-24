package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/**
 * 信号機を実際に描画するレンダラー
 */
public class RendererTrafficLight extends TileEntitySpecialRenderer<TileEntityTrafficLight> {
	
	
	
	
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

		
		if (te.getAddon() == null) return; // アドオンがまだ読み込まれていない場合（ダミーでも）は抜ける
		
		if (te.getAddon().baseTex == null) {
			// baseだけ抜け落ちるってことはないので
			if (te.getAddon().getFile() == null) {
				te.setDummyModel();
				te.setDummyTexture();
			}
			else {
				te.getAddon().redrawTexture(); // テクスチャを再生成する
			}
			te.markDirty();
		}
		
		// サイクルチェック
		ConfigTrafficLight.LightObject lightObject = null; // 現在光っているオブジェクトを格納
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
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		
		// オブジェクト毎にループ
		for (MQOObject o: te.getAddon().getModel().getObjects4Loop()) {
			boolean render = false;
			boolean nolight = false; // 光らないかどうか
			// オブジェクト毎に繰り返す
			if (te.getAddon().getConfig().getBody().contains(o.getName())) {
				// このオブジェクトは無発光オブジェクトとして描画する
				this.bindTexture(te.getAddon().baseTex);
				render = true;
			}
			else {
				// ライティングが必要な場合はちょっと変わる
				for (ConfigTrafficLight.LightObject l : te.getAddon().getConfig().getPatterns()) {
					// 一致しない場合はスルー
					if (!l.equals(lightObject)) continue;
					// 発光するかしないかを指定（存在するかどうかで決める）
					if (Objects.equals(l, lightObject) && l.getObjects().contains(o.getName())) {
						continue;
					}
					else if (te.getAddon().getConfig().getLight().contains(o.getName())) {
						// 未発光オブジェクト確定
						this.bindTexture(te.getAddon().noLightTex);
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
			float color = nolight ? 0.1f : 0.0f;
			o.draw(t.getBuffer(), color);
			
		}
		
		t.draw(); // ベース部分を描画
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
					if (Objects.equals(l, lightObject) && l.getObjects().contains(o.getName())) {
						// 発光オブジェクト確定
						this.bindTexture(te.getAddon().lightTex);
						render = true;
					}
					
				}
			}
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f); // 最高の明るさ
			
			if (!render) {
				// 描画の必要性がない場合
				continue;
			}
			
			
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
