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

import java.util.HashMap;


/**
 * 信号機を実際に描画するレンダラー
 */
public class RendererTrafficLight extends TileEntitySpecialRenderer<TileEntityTrafficLight> {
	
	// ロケーションはサーバーに保持しても意味がない
	private HashMap<String, ResourceLocation> baseTexs = new HashMap<>();
	private HashMap<String, ResourceLocation> lightTexs = new HashMap<>();
	private HashMap<String, ResourceLocation> noLightTexs = new HashMap<>();
	
	
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
		
		ResourceLocation[] textures = this.getModelTextures(te);
		if (textures == null) return; // テクスチャがないと落ちるのでならスキップする
		
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
			this.bindTexture(nolight ? textures[2] : textures[0]);
			
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
			
			this.bindTexture(textures[1]);
			
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
	
	/**
	 * このTileEntityを描画するために必要なテクスチャをバッファから取得する。
	 * もしない場合は作成を試みる。作成ができない場合永遠にレンダリングがされないことになるので注意。
	 * もし作成されない場合はnullのままの可能性があるので気をつける。
	 *
	 * リソースの割り当てに成功すると、「base」「light」「nolight」の順番に3つのリソースロケーションを格納した
	 * リソースロケーションの配列が戻る。
	 *
	 * @return リソースロケーション。ない場合はnull
	 *
	 * @param te 描画したいTileEntity
	 */
	private ResourceLocation[] getModelTextures(TileEntityTrafficLight te) {
		if (te.getAddon() == null) {
			GTS.GTSLog.warn("Warning. addon is null.");
			return null;
		}
		
		if (!te.getAddon().isDummy() && te.getAddon().getFile() == null) {
			// ダミーモデルに差し替えてそれをテクスチャとして返す
			te.setDummyModel();
			
			// ダミーモデルに差し替えたらあとは存在しなければ作成してくれるはず
		}
		else if (te.getAddon().getConfig().getTextures().getBase() == null || te.getAddon().getConfig().getTextures().getBaseTex() == null) {
			// リソースが存在しないので割り当てるが
			te.getAddon().reloadTexture(); // テクスチャを再読み込みする（ロケータを戻す）
		}
		
		// 生成に失敗した場合はそもそもreloadTextureでエラーが出るのでもう返しちゃう
		if (te.getAddon().getConfig().getTextures().getBaseTex() == null || te.getAddon().getConfig().getTextures().getLightTex() == null || te.getAddon().getConfig().getTextures().getNoLightTex() == null) {
			GTS.GTSLog.warn("Warning. dynamic texture is null.");
			return null;
		}
		
		// テクスチャの取得を試みる
		ResourceLocation baseTex = this.baseTexs.get(te.getAddon().getConfig().getTextures().getBase());
		ResourceLocation lightTex = this.lightTexs.get(te.getAddon().getConfig().getTextures().getLight());
		ResourceLocation noLightTex = this.noLightTexs.get(te.getAddon().getConfig().getTextures().getNoLight());
		
		if (baseTex == null) {
			
			// テクスチャを再代入する（この時点では3つともあるはず）
			baseTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
					te.getAddon().getConfig().getId() + "_base",
					new DynamicTexture(te.getAddon().getConfig().getTextures().getBaseTex())
			);
			lightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
					te.getAddon().getConfig().getId() + "_light",
					new DynamicTexture(te.getAddon().getConfig().getTextures().getLightTex())
			);
			noLightTex = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
					te.getAddon().getConfig().getId() + "_nolight",
					new DynamicTexture(te.getAddon().getConfig().getTextures().getNoLightTex())
			);
			
			// 今後使えるようにこのリソースロケーションをHashMapに追加する
			this.baseTexs.put(te.getAddon().getConfig().getTextures().getBase(), baseTex);
			this.lightTexs.put(te.getAddon().getConfig().getTextures().getLight(), lightTex);
			this.noLightTexs.put(te.getAddon().getConfig().getTextures().getNoLight(), noLightTex);
		}
		
		// ここまで来たら3つとも使えるのでそれを返す
		return new ResourceLocation[] {baseTex, lightTex, noLightTex};
	}
	
	
}
