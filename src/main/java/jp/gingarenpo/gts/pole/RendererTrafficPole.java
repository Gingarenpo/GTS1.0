package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.pack.Pack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * ポールのレンダリングを行う。
 */
public class RendererTrafficPole extends TileEntitySpecialRenderer<TileEntityTrafficPole> {
	
	@Override
	public void render(TileEntityTrafficPole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		
		
		
		if (te.getTexture() == null) {
			// ロケーションを新たに作るが
			if (te.getAddon().getConfig().getTexImage() == null) {
				// テクスチャが存在しない場合は再読み込みが必要
				if (te.getPackLocation() == null) {
					// ダミーなのでもう新たに割り当てる
					te.setDummyModel();
					return;
				}
				
				// ダミーモデルでない場合はLoaderから探す
				BufferedImage tex = GTS.loader.getTexture(te.getPackLocation(), te.getAddon().getConfig().getTexture());
				if (tex != null) {
					te.getAddon().getConfig().setTexImage(tex);
				}
				else {
					// 存在しないパックなのでダミーに差し替え
					te.setDummyModel();
					GTS.GTSLog.warn("Warning. Pole model missing.");
				}
				
			}
			te.setTexture(Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("tp_" + te.getAddon().getConfig().getId(), new DynamicTexture(te.getAddon().getConfig().getTexImage())));
		}
		
		this.bindTexture(te.getTexture());
		
		ArrayList<String> objects = te.isTop() ? te.getAddon().getConfig().getTopObject() : (te.isBottom() ? te.getAddon().getConfig().getBottomObject() : te.getAddon().getConfig().getBaseObject());
		
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		
		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		
		
		for (MQOObject o: te.getAddon().getModel().getObjects4Loop()) {
			if (objects.contains(o.getName())) {
				// あればそれを描画する
				o.draw(t.getBuffer(), 0.0f);
			}
		}
		t.draw();
		
		GL11.glPopMatrix();
	}
}