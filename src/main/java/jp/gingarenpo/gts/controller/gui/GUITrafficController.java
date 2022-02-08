package jp.gingarenpo.gts.controller.gui;

import net.minecraft.client.gui.GuiScreen;

/**
 * Swingに同名のクラスがあるが、いずれはどちらかを廃止する予定なので気にしない。
 * パッケージを分けているのはそれが理由。多分Swingでこのまま続行…となる気がするけども。
 */
public class GUITrafficController extends GuiScreen {
	
	/**
	 * GUIの表示中にゲームを一時停止するか。
	 * @return true
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	/**
	 * スクリーンを描画する。
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawDefaultBackground(); // 現在は使用しないので半透明の黒い背景を敷く
	}
}
