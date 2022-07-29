package jp.gingarenpo.gts.button;

import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class RendererTrafficButton extends TileEntitySpecialRenderer<TileEntityTrafficButton> {
	
	private HashMap<String, ResourceLocation> textures = new HashMap<>();
	
	/**
	 * この押ボタン箱を描画する。
	 * @param te TileEntity
	 * @param x 座標
	 * @param y 座標
	 * @param z 座標
	 * @param partialTicks 呼ばれてから最後に経過したTick
	 * @param destroyStage 破壊ステージ
	 * @param alpha ？
	 */
	@Override
	public void render(TileEntityTrafficButton te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
	
		if (te.getAddon() == null) {
			GTS.GTSLog.error("Error: addon is null. cannot render button!");
			return;
		}
		
		if (te.getAddon().getModel() == null) {
			GTS.GTSLog.warn("Warning. " + te.getAddon().getConfig().getId() + "'s Button Model is missing.");
			return;
		}
		
		if (!textures.containsKey(te.getAddon().getConfig().getBaseTexture())) {
			GTS.GTSLog.info(te.getAddon().getConfig().getId() + "'s Button Base Texture is not register. Try to regist.");
			if (te.getAddon().getConfig().getBaseTex() == null) {
				te.getAddon().reloadTexture();
			}
			
			if (te.getAddon().getConfig().getBaseTex() == null || te.getAddon().getConfig().getPushTex() == null) {
				return; // リロードに失敗しているので返す
			}
			
			textures.put(te.getAddon().getConfig().getBaseTexture(), Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId() + "_base", new DynamicTexture(te.getAddon().getConfig().getBaseTex())));
			textures.put(te.getAddon().getConfig().getPushTexture(), Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(te.getAddon().getConfig().getId() + "_push", new DynamicTexture(te.getAddon().getConfig().getPushTex())));
			return;
			
		}
		
		
		ResourceLocation baseTex = textures.get(te.getAddon().getConfig().getBaseTexture());
		ResourceLocation pushTex = textures.get(te.getAddon().getConfig().getPushTexture());
		
		if (baseTex == null || pushTex == null) {
			GTS.GTSLog.error(String.format("%s / %s | baseTex or pushTex is null. Cannot render this button!!", baseTex, pushTex));
			return;
		}
		
		// ここからようやく描画ができる
		GL11.glPushMatrix(); // 後始末できるように戻しておいて
		this.bindTexture(te.getButton().isPushed() ? textures.get(te.getAddon().getConfig().getPushTexture()) : textures.get(te.getAddon().getConfig().getBaseTexture()));
		
		// アングル分だけ回転・移動
		GL11.glTranslated(x + 0.5 + te.getAddon().getConfig().getCenterPositionX() * Math.cos(te.getAngle()) + te.getAddon().getConfig().getCenterPositionZ() * Math.sin(te.getAngle()),
				y + 0.5 + te.getAddon().getConfig().getCenterPositionY(),
				z + 0.5 + te.getAddon().getConfig().getCenterPositionZ() * Math.cos(te.getAngle()) * te.getAddon().getConfig().getCenterPositionX() * Math.sin(te.getAngle()));
		GL11.glRotated(te.getAngle(), 0f, 1f, 0f); // 回転させる
		GL11.glTranslated(0, 0, -0.5); // ブロックに隣接させる
		
		EnumFacing ef = EnumFacing.fromAngle(-180 -te.getAngle()); // 設置依存の向きを取得（この向きに1つ進んだところにポールとかブロックがある）
		BlockPos next = te.getPos().offset(ef); // その隣の座標を取得

		TileEntity nextTe = te.getWorld().getTileEntity(next);

		// 押ボタン箱をY軸に隣接させるように作るとうまくいく
		if (nextTe instanceof TileEntityTrafficPole) {
			// ポールの場合は、最小最大を取得してその分だけさらに近づける（バニラのフェンスとかいい具合にやってくれるので無視）
			double minmax = ((TileEntityTrafficPole) nextTe).getAddon().getModel().getMinMaxPosition(((TileEntityTrafficPole) nextTe).getAddon().getConfig().getBaseObject())[1][2]; // maxZ取得
			// ※ポールは常に中心（0,0,0）に配置されているとする
			GL11.glTranslated(0, 0, -(0.5-minmax) );
		}
		
		
		
		
		
		
		
		
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		RenderHelper.disableStandardItemLighting();
		
		// Tessellator 用意
		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		for (MQOObject o: te.getAddon().getModel().getObjects4Loop()) {
			if (te.getAddon().getConfig().getObjects().size() > 0 && !te.getAddon().getConfig().getObjects().contains(o.getName())) continue;
			o.draw(t.getBuffer(), 0.0f);
		}
		t.draw();
		
		
		// レンダリング終了
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
		
	}
}
