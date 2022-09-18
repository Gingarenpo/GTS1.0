package jp.gingarenpo.gts.proxy;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.button.RendererTrafficButton;
import jp.gingarenpo.gts.button.TileEntityTrafficButton;
import jp.gingarenpo.gts.controller.RendererTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.core.GTSResourcePack;
import jp.gingarenpo.gts.light.RendererTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import jp.gingarenpo.gts.pole.RendererTrafficPole;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
import jp.gingarenpo.gts.sign.RendererTrafficSign;
import jp.gingarenpo.gts.sign.TileEntityTrafficSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

import static net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation;

/**
 * クライアント上で行う処理。サーバーと別個で行う。サーバーで処理しないものはそっちでオーバーライドして無効化する。
 */
public class GTSProxy {
	
	/**
	 * アイテムのモデルレンダーを登録する
	 */
	public void registerItemModels() {
		setCustomModelResourceLocation(GTS.Items.control_item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "control"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.light_item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "light"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.pole_item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "pole"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.arm, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "arm"), "inventory"));
		setCustomModelResourceLocation(GTS.Items.button_item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(new ResourceLocation(GTS.MOD_ID, "button"), "inventory"));
		
	}
	
	/**
	 * TESRを登録する
	 */
	public void registerTESRs() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficController.class, new RendererTrafficController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficLight.class, new RendererTrafficLight());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficPole.class, new RendererTrafficPole());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficButton.class, new RendererTrafficButton());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficSign.class, new RendererTrafficSign());
	}
	
	/**
	 * カスタムのリソースパック読み込み方法を追加する。ようは、getMinecraft()で取得できる
	 * Minecraftインスタンスの中にあるprivateパッケージのdefaultResourcePackに追加することで再現する。
	 * このMinecraftインスタンスはクライアント側にしかないのでサーバーでやると事故る。なので
	 * こちらで実行する。
	 *
	 * メモ：難読化後のフィールド名はfield_110449_ao
	 */
	public void registerResourcePackLoader() {
		List<IResourcePack> l = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
		l.add(new GTSResourcePack()); // デフォルトパックに無理やり詰め込む
		Minecraft.getMinecraft().refreshResources(); // 無理やり読み込ませる
//		System.out.println(Minecraft.getMinecraft().getResourceManager().getResourceDomains());
//		for (IResourcePack i: l) {
//			System.out.println(i);
//		}
	}
}
