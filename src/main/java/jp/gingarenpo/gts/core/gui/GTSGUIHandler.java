package jp.gingarenpo.gts.core.gui;

import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.controller.gui.GUIContainerTrafficController;
import jp.gingarenpo.gts.controller.gui.GUITrafficController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * GTSで使用するGUIのハンドラーを作成する。
 * Swingでやっているがいろいろあってこっちに移行することを考えて作成。
 */
public class GTSGUIHandler implements IGuiHandler {
	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// サーバー側ではクライアントに向けたGUIは開かず、コンテナのみ開く
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // その場所にあるTileEntityを取得する
		if (te == null) return null; // 何もない場合はNull
		else if (te instanceof TileEntityTrafficController) return new GUIContainerTrafficController(); // 制御機の場合はこれを開く
		return null;
	}
	
	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// クライアント側では描画も含めた本格的なGUIを開く
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // その場所にあるTileEntityを取得する
		if (te == null) return null; // 何もない場合はNull
		else if (te instanceof TileEntityTrafficController) return new GUITrafficController(); // 制御機の場合はこれを開く
		return null;
	}
}
