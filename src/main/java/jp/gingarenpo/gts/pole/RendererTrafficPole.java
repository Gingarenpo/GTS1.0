package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ポールのレンダリングを行う。
 */
public class RendererTrafficPole extends TileEntitySpecialRenderer<TileEntityTrafficPole> {
	
	private HashMap<String, ResourceLocation> textures = new HashMap<>();
	
	@Override
	public void render(TileEntityTrafficPole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		
		if (te.getAddon() == null) return;
		
		if (!textures.containsKey(te.getAddon().getConfig().getTexture())) {
			// テクスチャないので作成する
			if (te.getAddon().getConfig().getTexImage() == null) {
				// テクスチャがないのでリロードする
				te.getAddon().reloadTexture();
				if (te.getAddon().getConfig().getTexImage() == null) {
					te.setDummyModel();
					return;
				}
			}
			textures.put(te.getAddon().getConfig().getTexture(), Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId(), new DynamicTexture(te.getAddon().getConfig().getTexImage())));
		}
		
		this.bindTexture(textures.get(te.getAddon().getConfig().getTexture()));
		
		ArrayList<String> objects = te.isTop() ? te.getAddon().getConfig().getTopObject() : (te.isBottom() ? te.getAddon().getConfig().getBottomObject() : te.getAddon().getConfig().getBaseObject());
		
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
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
				// アームの接続だけ繰り返す
				BlockPos pos2 = new BlockPos(pos[0], pos[1], pos[2]); // アームの接続先をBlockPosとする
				
				// まず距離を測る
				double distance = Math.sqrt(Math.pow((te.getPos().getX() - pos2.getX()), 2) + Math.pow((te.getPos().getY() - pos2.getY()), 2) + Math.pow((te.getPos().getZ() - pos2.getZ()), 2));
				
				// 回転角を算出する（座標系が逆なので負の数にしなくてはならない）
				double xz = -Math.atan2(pos2.getZ() - te.getPos().getZ(), pos2.getX() - te.getPos().getX()); // Y回転（ただし逆）
				
				// 回転する
				GL11.glRotated(Math.toDegrees(xz), 0, 1, 0);
				
				// スタートの続きから描きたいのでその分だけ移動する
				double[][] minmax = te.getArm().getAddon().getModel().getMinMaxPosition(te.getArm().getAddon().getConfig().getStartObject());
				GL11.glTranslated(minmax[1][0], 0, 0);
				
				// 中間地点の描画
				t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
				for (MQOObject o: te.getArm().getAddon().getModel().rescale(0, 0, 0, (distance - 1) / te.getArm().getAddon().getConfig().getSize(), 1, 1).getObjects4Loop()) {
					if (te.getArm().getAddon().getConfig().getStartObject().contains(o.getName())) {
						continue;
					}
					o.draw(t.getBuffer(), 0);
				}
				t.draw();
				
				// スタートの位置に戻す
				GL11.glTranslated(-minmax[1][0], 0, 0);
				
				// 初期地点の描画
				t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
				for (MQOObject o: te.getArm().getAddon().getModel().getObjects4Loop()) {
					if (!te.getArm().getAddon().getConfig().getStartObject().contains(o.getName())) {
						continue;
					}
					o.draw(t.getBuffer(), 0);
				}
				t.draw();
				
				// 回転を元に戻す
				GL11.glRotated(-Math.toDegrees(xz), 0, 1, 0);
			}
		}
		
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}
}
