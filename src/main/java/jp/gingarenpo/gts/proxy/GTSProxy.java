package jp.gingarenpo.gts.proxy;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.RendererTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.light.RendererTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

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
		
	}
	
	/**
	 * TESRを登録する
	 */
	public void registerTESRs() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficController.class, new RendererTrafficController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficLight.class, new RendererTrafficLight());
	}
}
