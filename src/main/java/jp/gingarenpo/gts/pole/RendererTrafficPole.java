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
			// テクスチャを見つける
			if (!textures.containsKey(te.getArm().getAddon().getConfig().getTexture())) {
				// ない場合は用意する
				if (te.getArm().getAddon().getConfig().getTexImage() == null) {
					// テクスチャイメージも入っていないのでリロードする
					te.getArm().getAddon().reloadTexture();
					if (te.getArm().getAddon().getConfig().getTexImage() == null) {
						// それでもないのでもうダミーを使う
						GTS.GTSLog.warn("Warning. arm model not found. Dummy used.");
						te.getArm().setDummyModel();
						return;
					}

				}
				// あるはずなのでテクスチャをセットする
				textures.put(te.getArm().getAddon().getConfig().getTexture(), Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getArm().getAddon().getConfig().getId(), new DynamicTexture(te.getArm().getAddon().getConfig().getTexImage())));
			}
			
			this.bindTexture(textures.get(te.getArm().getAddon().getConfig().getTexture()));
			
			for (double[] pos : te.getArm().getTo()) {
				// アームの接続だけ繰り返す
				BlockPos pos2 = new BlockPos(pos[0], pos[1], pos[2]); // アームの接続先をBlockPosとする
				
				// まず距離を測る
				double distance = Math.sqrt(Math.pow((te.getPos().getX() - pos2.getX()), 2) + Math.pow((te.getPos().getY() - pos2.getY()), 2) + Math.pow((te.getPos().getZ() - pos2.getZ()), 2));
				
				// 回転角を算出する（座標系が逆なので負の数にしなくてはならない）
				double xz = -Math.atan2(pos2.getZ() - te.getPos().getZ(), pos2.getX() - te.getPos().getX()); // Y回転（ただし逆）
				
				// 回転する
				GL11.glRotated(Math.toDegrees(xz), 0, 1, 0);
				
				
				// 開始座標を取得
				double[][] minmaxStart = te.getArm().getAddon().getModel().getMinMaxPosition(te.getArm().getAddon().getConfig().getStartObject());
				// エンドの位置を取得
				if (te.getArm().getAddon().getConfig().getEndObject() == null) {
					// 後から追加したのでnullの可能性も考慮する
					te.getArm().getAddon().getConfig().setEndObject(new ArrayList<>()); // 仮で無を入れる
				}
				double[][] minmaxEnd = te.getArm().getAddon().getModel().getMinMaxPosition(te.getArm().getAddon().getConfig().getEndObject());
				
				
				// スタートの続きから描きたいのでその分だけ移動する
				GL11.glTranslated((minmaxStart[1][0]-minmaxStart[0][0]), 0, 0);
				
				// 中間地点の描画（エンドとスタートの分を引く）
				t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
				for (MQOObject o: te.getArm().getAddon().getModel().rescale(0, 0, 0, (distance - (minmaxEnd[1][0]-minmaxEnd[0][0])) / te.getArm().getAddon().getConfig().getSize(), 1, 1).getObjects4Loop()) {
					if (te.getArm().getAddon().getConfig().getStartObject().contains(o.getName()) || te.getArm().getAddon().getConfig().getEndObject().contains(o.getName())) {
						continue;
					}
					o.draw(t.getBuffer(), 0);
				}
				t.draw();
				
				// スタートの位置に戻す
				GL11.glTranslated(-(minmaxStart[1][0]-minmaxStart[0][0]), 0, 0);
				
				// 初期地点の描画（ただし、サイズ以下の場合は被るので優先度の高いほうを描画）
				if ((te.getArm().getAddon().getConfig().isDrawStartPrimary() && distance <= te.getArm().getAddon().getConfig().getSize()) || distance > te.getArm().getAddon().getConfig().getSize()) {
					t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
					for (MQOObject o : te.getArm().getAddon().getModel().getObjects4Loop()) {
						if (! te.getArm().getAddon().getConfig().getStartObject().contains(o.getName())) {
							continue;
						}
						o.draw(t.getBuffer(), 0);
					}
					t.draw();
				}
				
				// エンドを描く。
				if (te.getArm().getAddon().getConfig().getEndObject() != null && (distance > te.getArm().getAddon().getConfig().getSize() || (!te.getArm().getAddon().getConfig().isDrawStartPrimary() && distance <= te.getArm().getAddon().getConfig().getSize()))) {
					
					
					// エンドの位置に持っていく
					GL11.glTranslated(distance - (minmaxEnd[1][0]-minmaxEnd[0][0]), 0, 0);
					
					// エンド地点の描画
					t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
					for (MQOObject o: te.getArm().getAddon().getModel().getObjects4Loop()) {
						if (!te.getArm().getAddon().getConfig().getEndObject().contains(o.getName())) {
							continue;
						}
						o.draw(t.getBuffer(), 0);
					}
					t.draw();
					
					// 戻す
					GL11.glTranslated(-(distance - (minmaxEnd[1][0]-minmaxEnd[0][0])), 0, 0);
					
				}
				
				
				
				// 回転を元に戻す
				GL11.glRotated(-Math.toDegrees(xz), 0, 1, 0);
			}
		}
		
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}
}
