package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
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
		
		// アームの描画を行う
		if (te.getArm() != null && te.getArm().getAddon() != null) {
			if (te.getArm().getAddon().getConfig().getTexImage() == null) {
				// テクスチャがない場合は読み込む
				BufferedImage tex = GTS.loader.getTexture(te.getPackLocation(), te.getArm().getAddon().getConfig().getTexture());
				if (tex != null) {
					te.getArm().getAddon().getConfig().setTexImage(tex);
					
				}
				else {
					// 存在しないパックなのでダミーに差し替え
					te.getArm().setDummyModel();
					GTS.GTSLog.warn("Warning. Arm model missing.");
				}
			}
			
			if (te.getArm().getTexture() == null) {
				te.getArm().setTexture(Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ta_" + te.getArm().getAddon().getConfig().getId(), new DynamicTexture(te.getArm().getAddon().getConfig().getTexImage())));
			}
			
			this.bindTexture(te.getArm().getTexture());
			for (double[] pos : te.getArm().getTo()) {
				BlockPos pos2 = new BlockPos(pos[0], pos[1], pos[2]);
				// まず距離を測る
				double distance = te.getPos().distanceSqToCenter(pos2.getX(), pos2.getY(), pos2.getZ());
				
				// XYZそれぞれに対してatan2で回転角を算出する
				double xy = Math.atan2(pos2.getY() - te.getPos().getY(), pos2.getX() - te.getPos().getX()); // Z回転
				double xz = Math.atan2(pos2.getZ() - te.getPos().getZ(), pos2.getX() - te.getPos().getX()); // Y回転
				double yz = Math.atan2(pos2.getZ() - te.getPos().getZ(), pos2.getY() - te.getPos().getY()); // X回転
				System.out.printf("%f %f %f%n", xy, xz, yz);
				
				// 回転する
				GL11.glRotated(Math.toDegrees(xy), 0, 0, 1);
				GL11.glRotated(Math.toDegrees(xz), 0, 1, 0);
				GL11.glRotated(Math.toDegrees(yz), 1, 0, 0);
				
				// とりあえず描画だけさせてみる
				t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
				for (MQOObject o: te.getArm().getAddon().getModel().rescale(0, 0, 0, 1.00001, 1, 1).getObjects4Loop()) {
					o.draw(t.getBuffer(), 0);
				}
				t.draw();
			}
		}
		
		GL11.glPopMatrix();
	}
}
