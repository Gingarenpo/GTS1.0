package jp.gingarenpo.gts.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

/**
 * TESR。制御機のレンダーとなる。ここで実際にレンダリングを行う。
 */
public class RendererTrafficController extends TileEntitySpecialRenderer<TileEntityTrafficController> {
	
	/**
	 * 実際に制御機をレンダリングする。各種パラメーターが大量に渡されるため、それをもとに、この中でOpenGLを駆使してレンダリングする。
	 * 原点をずらして0,0,0,1,1,1の範囲で描画する。
	 *
	 * @param te 描画する対象となるTileEntity
	 * @param x X座標
	 * @param y Y座標
	 * @param z Z座標
	 * @param partialTicks 1Tickが過ぎてからの経過時間（基本1秒は20Tickなので60fpsの場合3回冗長に呼ばれる。それを回避するためのもの）
	 * @param destroyStage 破壊ステージ。ツルハシとかで壊すとひび割れるあの段階。
	 * @param alpha 透明度。
	 */
	@Override
	public void render(TileEntityTrafficController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		if (!te.isreadyRender()) {
			te.setTextureLocation(Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("control", new DynamicTexture(te.getTexture())));
		}
		if (TileEntityTrafficController.model == null) {
			TileEntityTrafficController.loadModel(); // モデルの読み込みを試みる
			return; // いったん抜ける
		}
		
		// テクスチャをバインドする
		this.bindTexture(te.getTextureLocation());
		
		// レンダリング開始
		GL11.glPushMatrix(); // 現在の行列情報をスタックに押し込む。これで自由に弄ってもここから戻せば元通り！
		GL11.glTranslated(x + 0.5, y, z + 0.5); // ブロックの原点を描画対象の座標に移動させる（ただしMQOの性質上原点を中心に移動させる）
		RenderHelper.disableStandardItemLighting();
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH); // スムージング
		
		TileEntityTrafficController.model.draw(getWorld().getCelestialAngleRadians(partialTicks));
		
		RenderHelper.enableStandardItemLighting();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix(); // 遊んだ後は後始末。後始末とかは任せたぞ～♪
	}
	
	
}
