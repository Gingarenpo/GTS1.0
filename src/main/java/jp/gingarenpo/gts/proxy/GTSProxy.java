package jp.gingarenpo.gts.proxy;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.button.RendererTrafficButton;
import jp.gingarenpo.gts.button.TileEntityTrafficButton;
import jp.gingarenpo.gts.controller.RendererTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.light.RendererTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import jp.gingarenpo.gts.pole.RendererTrafficPole;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import static net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation;

/**
 * クライアント上で行う処理。サーバーと別個で行う。サーバーで処理しないものはそっちでオーバーライドして無効化する。
 */
public class GTSProxy {
	
	/**
	 * アイテムのモデルレンダーを登録する
	 */
	public void registerItemModels() {
		setCustomModelResourceLocation(GTS.Items.control, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "control"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.light, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "light"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.pole, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "pole"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.arm, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "arm"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.button, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "button"), "inventory"));
		
	}
	
	/**
	 * TESRを登録する
	 */
	public void registerTESRs() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficController.class, new RendererTrafficController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficLight.class, new RendererTrafficLight());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficPole.class, new RendererTrafficPole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficButton.class, new RendererTrafficButton());
	}
}
