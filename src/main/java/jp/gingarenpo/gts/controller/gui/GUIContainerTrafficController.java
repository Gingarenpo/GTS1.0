package jp.gingarenpo.gts.controller.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * 交通信号制御機のGUI（サーバー側）をつかさどる。
 * クライアント側と違い、データを格納する側となる。描画などは一切行わない。
 * スロットとかの指定を行うためにあるが今回データは保持しない（SwingGUIアタッチしているので）
 */
public class GUIContainerTrafficController extends Container {
	
	/**
	 * 引数のプレイヤーがこのGUIを開くことができるかどうか。
	 * @param playerIn
	 * @return
	 */
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
}
