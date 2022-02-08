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
	
	/**
	 * @deprecated GTC時代の遺産となっている
	 *
	 * 制御機の土台をレンダリングする。向きは特にない。MQOモデルのレンダーに置き換え予定。っていうかOpenGLエグイ
	 */
	private void renderBase() {
		// ベース部分を描く（向きにかかわらず固定です）
		// ベース部分は1x1x0.4となる
		// テクスチャは上の適当なところを使うので固定
		
		// Zは自分のほうを向く（南側）
		
		// 南側の面（Z=1固定）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(1.0, 0.0, 1.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(1.0, 0.2, 1.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.0, 0.2, 1.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.0, 0.0, 1.0);
		GL11.glEnd();
		
		// 北側の面（Z=0固定でX軸が南側と入れ替わる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(1.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(1.0, 0.0, 0.0);
		GL11.glEnd();
		
		// 西側の面（X=0固定でZが横になる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.0, 0.0, 1.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.0, 0.2, 1.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glEnd();
		
		// 東側の面（X=1固定でZ軸が西側と入れ替わる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(1.0, 0.0, 0.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(1.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(1.0, 0.2, 1.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(1.0, 0.0, 1.0);
		GL11.glEnd();
		
		// 上側の面（Y=0.2固定でUVと同じ面になる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(1.0, 0.2, 1.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(1.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.0, 0.2, 0.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.0, 0.2, 1.0);
		GL11.glEnd();
		
		// 下側の面（Y=0固定で残りが全部北側と入れ替わる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.0, 0.0, 1.0);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(1.0, 0.0, 1.0);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(1.0, 0.0, 0.0);
		GL11.glEnd();
	}
	
	
	/**
	 * @deprecated そもそもfacingにTEが対応していないので無理やり呼び出すにしても非効率。遺産。
	 * @param facing 向いている方向。
	 */
	private void renderBody(EnumFacing facing) {
		// ボディ部分のレンダーを行う（回転処理を行わなければならない）
		// 回転処理
		GL11.glTranslated(0.5, 0.5, 0.5); // 回転中心を移動させて
		switch (facing) {
			case DOWN:
			case UP:
			case NORTH:
				break;
			case EAST:
				GL11.glRotated(-90, 0, 1, 0);
				break;
			case SOUTH:
				GL11.glRotated(180, 0, 1, 0);
				break;
			
			case WEST:
				GL11.glRotated(90, 0, 1, 0);
				break;
			default:
				break;
			
		}
		GL11.glTranslated(-0.5, -0.5, -0.5); // 戻す
		
		// レンダー開始
		// 基本はbaseと同じだがテクスチャが異なる面がある
		// 北を向いて設置した際に南側（一番プレイヤーに近い場所）がメインかつラベル。
		
		// 南側の面（Z=1固定）テクスチャは長方形になります（0.8:1.6）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.5, 1.0);
		GL11.glVertex3d(0.9, 0.2, 0.9);
		GL11.glTexCoord2d(0.5, 0.0);
		GL11.glVertex3d(0.9, 1.8, 0.9);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.1, 1.8, 0.9);
		GL11.glTexCoord2d(0.0, 1.0);
		GL11.glVertex3d(0.1, 0.2, 0.9);
		GL11.glEnd();
		
		
		// 北側の面（Z=0固定でX軸が南側と入れ替わる）、テクスチャは右半分を使用します（ちょっと変えている）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(1.0, 1.0);
		GL11.glVertex3d(0.1, 0.2, 0.1);
		GL11.glTexCoord2d(1.0, 0.0);
		GL11.glVertex3d(0.1, 1.8, 0.1);
		GL11.glTexCoord2d(0.5, 0.0);
		GL11.glVertex3d(0.9, 1.8, 0.1);
		GL11.glTexCoord2d(0.5, 1.0);
		GL11.glVertex3d(0.9, 0.2, 0.1);
		GL11.glEnd();
		
		// 西側の面（X=0固定でZが横になる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.1, 0.2, 0.9);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.1, 1.8, 0.9);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.1, 1.8, 0.1);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.1, 0.2, 0.1);
		GL11.glEnd();
		
		// 東側の面（X=1固定でZ軸が西側と入れ替わる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.9, 0.2, 0.1);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.9, 1.8, 0.1);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.9, 1.8, 0.9);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.9, 0.2, 0.9);
		GL11.glEnd();
		
		// 上側の面（Y=1.8固定でUVと同じ面になる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.9, 1.8, 0.9);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.9, 1.8, 0.1);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.1, 1.8, 0.1);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.1, 1.8, 0.9);
		GL11.glEnd();
		
		// 下側の面（Y=0.2固定で残りが全部北側と入れ替わる）
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2d(0.1, 0.1);
		GL11.glVertex3d(0.1, 0.2, 0.1);
		GL11.glTexCoord2d(0.1, 0.0);
		GL11.glVertex3d(0.1, 0.2, 0.9);
		GL11.glTexCoord2d(0.0, 0.0);
		GL11.glVertex3d(0.9, 0.2, 0.9);
		GL11.glTexCoord2d(0.0, 0.1);
		GL11.glVertex3d(0.9, 0.2, 0.1);
		GL11.glEnd();
	}
}
