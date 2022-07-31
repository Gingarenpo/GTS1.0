package jp.gingarenpo.gts;

import jp.gingarenpo.gts.arm.ItemTrafficArm;
import jp.gingarenpo.gts.arm.gui.SwingGUITrafficArm;
import jp.gingarenpo.gts.button.BlockTrafficButton;
import jp.gingarenpo.gts.button.PacketTrafficButton;
import jp.gingarenpo.gts.button.TileEntityTrafficButton;
import jp.gingarenpo.gts.button.gui.SwingGUITrafficButton;
import jp.gingarenpo.gts.controller.BlockTrafficController;
import jp.gingarenpo.gts.controller.PacketTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.core.gui.GTSGUIHandler;
import jp.gingarenpo.gts.core.network.PacketItemStack;
import jp.gingarenpo.gts.event.GTSWorldEvent;
import jp.gingarenpo.gts.light.BlockTrafficLight;
import jp.gingarenpo.gts.light.PacketTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import jp.gingarenpo.gts.minecraft.GTSSavedData;
import jp.gingarenpo.gts.pack.Loader;
import jp.gingarenpo.gts.pole.BlockTrafficPole;
import jp.gingarenpo.gts.pole.PacketTrafficPole;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
import jp.gingarenpo.gts.pole.gui.SwingGUITrafficPole;
import jp.gingarenpo.gts.proxy.GTSProxy;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;


/**
 * This Java File created by Minecraft Develop Plugin.
 * このJavaファイルはMinecraft Developによって作成されました。
 * （著者注：英文が一緒に入っているコメントは自動生成された英文を日本語に訳してあります）
 */
@Mod(
		modid = GTS.MOD_ID,
		name = GTS.MOD_NAME,
		version = GTS.VERSION
)
public class GTS {
	
	public static final String MOD_ID = "gts"; // 単にMODのID
	public static final String MOD_NAME = "GTS - Ginren Traffic System"; // わかりやすいModの名前
	public static final String VERSION = "1.0"; // バージョン
	public static final SimpleNetworkWrapper MOD_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID); // パケット通信に使用するチャンネル
	public static File GTSModDir; // GTSのデータを格納するディレクトリ
	
	public static GTSSavedData data; // 独自の保存データ。
	public static Logger GTSLog; // GTSのログを表示するためのもの
	public static Loader loader; // GTS用のアドオンパックロード
	
	public static JFrame window; // GTS用のウィンドウ。GUIをMinecraftで表現するのがめんどくさいので…
	public static int windowWidth; // GTS用のウィンドウの大きさ。画面解像度を基準として決定する
	public static int windowHeight; // こちらも同じく
	
	public static CreativeTabs gtsTab; // GTCのタブ

	
	@SidedProxy(clientSide = "jp.gingarenpo.gts.proxy.GTSProxy", serverSide = "jp.gingarenpo.gts.proxy.GTSServerProxy")
	public static GTSProxy proxy; // クライアントとサーバーで処理を別にしなくてはならない内容などをここに記載
	
	
	/**
	 * This is the instance of your mod as created by Forge. It will never be null.
	 * これはForgeによってつくられるこのMod自身のインスタンス。ここがnullになることはまずない。
	 */
	@Mod.Instance(MOD_ID)
	public static GTS INSTANCE;
	
	/**
	 * ※後から追加。preInitよりも最初に実行される一番早いところ。
	 * @param event
	 */
	@Mod.EventHandler
	public void construct(FMLConstructionEvent event) {
		// タブの登録
		gtsTab = new CreativeTabs("GTS") {
			@Override
			public ItemStack createIcon() {
				return new ItemStack(Blocks.control); // これでよし
			}
		};
		
		
		// ウィンドウの準備だけする（表示はしない）
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
	}
	
	/**
	 * This is the first initialization event. Register tile entities here.
	 * The registry events below will have fired prior to entry to this method.
	 * 最初に初期化される際のイベント。TileEntityの登録はこちら。
	 */
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws IOException {
		// イベントを登録する
		MinecraftForge.EVENT_BUS.register(new GTSWorldEvent()); // ワールドイベント
		
		
		// ログを登録する
		GTSLog = LogManager.getLogger("GTS");
		
		// パスを登録する（基本的にこの中身を見る）
		GTSModDir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath() + "\\mods\\GTS"); // 代入
		if (!GTSModDir.exists()) {
			// 存在しない場合は作成する
			if (!GTSModDir.mkdir()) {
				throw new IOException("GTS can't create mod directory."); // ディレクトリを作れないとエラー
			}
		}
		
		
		// プロキシ処理
		proxy.registerTESRs();
		
		// TileEntityの登録（非推奨になっているけどこれで登録できるので）
		// 注意：キー変えたので既存のワールドは開けない
		GameRegistry.registerTileEntity(TileEntityTrafficController.class, "gts:control");
		GameRegistry.registerTileEntity(TileEntityTrafficLight.class, "gts:light");
		GameRegistry.registerTileEntity(TileEntityTrafficPole.class, "gts:pole");
		GameRegistry.registerTileEntity(TileEntityTrafficButton.class, "gts:button");
		
		// GUIの登録
		NetworkRegistry.INSTANCE.registerGuiHandler(GTS.INSTANCE, new GTSGUIHandler()); // GUI
		
		// パケットの登録
		GTSPacket.init();
		
		// パックを登録する
		loader = new Loader();
		loader.load(GTSModDir); // 検索をかける
		
	}
	
	/**
	 * This is the second initialization event. Register custom recipes
	 * 二番目の初期化イベント。カスタムレシピなどの登録はこちら。
	 */
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		// MinecraftにGTSのパックからリソースを読み込めるように指示する
		proxy.registerResourcePackLoader();
	}
	
	/**
	 * This is the final initialization event. Register actions from other mods here
	 * ラストの初期化イベント。他のModの初期化が済んでいるのでそれとなんかして使いたい場合にどうぞ。
	 */
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IOException {
	
	}
	
	/**
	 * Forge will automatically look up and bind blocks to the fields in this class
	 * based on their registry name.
	 *
	 * Forgeは自動的にここを参照し、このクラスにあるブロックのフィールドに対してブロックを生成する。
	 * static finalとしておくこと。
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Blocks {
		public static final BlockTrafficController control = null; // 制御機
		public static final BlockTrafficLight light = null; // 信号機
		public static final BlockTrafficPole pole = null; // ポール
		public static final BlockTrafficButton button = null; // 押ボタン箱
	}
	
	
	/**
	 * Forge will automatically look up and bind items to the fields in this class
	 * based on their registry name.
	 *
	 * 上記のアイテム版。アイテムブロックとしてもここで登録すべきである。
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Items {
		@GameRegistry.ObjectHolder("control")
      	public static final ItemBlock control_item = null; // 制御機のドロップ扱い
		
		@GameRegistry.ObjectHolder("light")
		public static final ItemBlock light_item = null; // 信号機のドロップ扱い
		
		@GameRegistry.ObjectHolder("pole")
		public static final ItemBlock pole_item = null; // ポールのドロップ扱い]
		
		@GameRegistry.ObjectHolder("button")
		public static final ItemBlock button_item = null; // 押ボタン箱のドロップ扱い
		
		
		public static final ItemTrafficArm arm = null;
	}
	
	/**
	 * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
	 *
	 * これは特別なクラス。登録イベントを直接呼び出す。カスタムアイテムを登録するときなどに利用する。
	 */
	@Mod.EventBusSubscriber(modid = MOD_ID)
	public static class ObjectRegistryHandler {
		/**
		 * Listen for the register event for creating custom items
		 */
		@SubscribeEvent
		@SuppressWarnings("null")
		public static void addItems(RegistryEvent.Register<Item> event) {
			event.getRegistry().registerAll(
					new ItemBlock(Blocks.control).setRegistryName(Blocks.control.getRegistryName()),
					new ItemBlock(Blocks.light).setRegistryName(Blocks.light.getRegistryName()),
					new ItemBlock(Blocks.pole).setRegistryName(Blocks.pole.getRegistryName()),
					new ItemBlock(Blocks.button).setRegistryName(Blocks.button.getRegistryName()),
					new ItemTrafficArm()
			);
		}
		
		/**
		 * Listen for the register event for creating custom blocks
		 */
		@SubscribeEvent
		public static void addBlocks(RegistryEvent.Register<Block> event) {
			event.getRegistry().registerAll(
					new BlockTrafficController(),
					new BlockTrafficLight(),
					new BlockTrafficPole(),
					new BlockTrafficButton()
			); // ブロックを実際に登録
		}
		
		/**
		 * プレイヤーが何もないところを右クリック（設置）したときに呼び出されるイベント。
		 * クライアント側で呼び出されるのでなんかあればサーバーにパケットを送らねばならない
		 *
		 * @param event
		 */
		@SubscribeEvent
		public static void onPlayerClick(PlayerInteractEvent.RightClickItem event) {
			if (event.getSide().isServer()) return; // サーバー側では実行されないはずだけど
			if (event.getHand() == EnumHand.OFF_HAND) return; // 左手は無視
			ItemStack is = event.getItemStack(); // 持っているアイテムを取得
			if (is.getItem() == ItemBlock.getItemFromBlock(Blocks.pole)) {
				// ポールを持った状態でクリックした
				NBTTagCompound compound = is.getTagCompound(); // 取得
				if (compound == null) {
					compound = new NBTTagCompound(); // 新たにNBTタグを作成
					// モデルのパック名を入れる（getなんとかnameで取れるやつ）
					compound.setString("gts_item_model_pole", ""); // 空文字を入れることでダミーモデルだと判断させる
					is.setTagCompound(compound);
				}
				
				
				// ポールのモデル更新ウィンドウを開く
				if (GTS.window != null) return; // 二重に開かない
				EntityPlayer player = event.getEntityPlayer();
				World world = event.getWorld();
				player.openGui(GTS.INSTANCE, 1, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				GTS.window = new SwingGUITrafficPole(is);
				GTS.window.setVisible(true);
				GTS.window.addWindowListener(new WindowAdapter() {
					
					@Override
					public void windowClosed(WindowEvent e) {
						player.closeScreen();
						GTS.window = null; // 元に戻す
						
						// サーバーにパケットを送信する
						GTS.MOD_NETWORK.sendToServer(new PacketItemStack(is.getTagCompound(), player.getName()));
					}
				});
				return;
			}
			else if (is.getItem() == Items.arm) {
				// アームを持った状態でクリックした
				NBTTagCompound compound = is.getTagCompound(); // 取得
				if (compound == null) {
					compound = new NBTTagCompound(); // 新たにNBTタグを作成
					// モデルのパック名を入れる（getなんとかnameで取れるやつ）
					compound.setString("gts_item_model_arm", ""); // 空文字を入れることでダミーモデルだと判断させる
					is.setTagCompound(compound);
				}
				
				
				// ポールのモデル更新ウィンドウを開く
				if (GTS.window != null) return; // 二重に開かない
				EntityPlayer player = event.getEntityPlayer();
				World world = event.getWorld();
				player.openGui(GTS.INSTANCE, 1, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				GTS.window = new SwingGUITrafficArm(is);
				GTS.window.setVisible(true);
				GTS.window.addWindowListener(new WindowAdapter() {
					
					@Override
					public void windowClosed(WindowEvent e) {
						player.closeScreen();
						GTS.window = null; // 元に戻す
						
						// サーバーにパケットを送信する
						GTS.MOD_NETWORK.sendToServer(new PacketItemStack(is.getTagCompound(), player.getName()));
					}
				});
				return;
			}
			else if (is.getItem() == ItemBlock.getItemFromBlock(Blocks.button)) {
				// 押ボタン箱を持った状態でクリックした
				NBTTagCompound compound = is.getTagCompound(); // 取得
				if (compound == null) {
					compound = new NBTTagCompound(); // 新たにNBTタグを作成
					// モデルのパック名を入れる（getなんとかnameで取れるやつ）
					compound.setString("gts_item_model_button", ""); // 空文字を入れることでダミーモデルだと判断させる
					is.setTagCompound(compound);
				}
				
				
				// ポールのモデル更新ウィンドウを開く
				if (GTS.window != null) return; // 二重に開かない
				EntityPlayer player = event.getEntityPlayer();
				World world = event.getWorld();
				player.openGui(GTS.INSTANCE, 1, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				GTS.window = new SwingGUITrafficButton(is);
				GTS.window.setVisible(true);
				GTS.window.addWindowListener(new WindowAdapter() {
					
					@Override
					public void windowClosed(WindowEvent e) {
						player.closeScreen();
						GTS.window = null; // 元に戻す
						
						// サーバーにパケットを送信する
						GTS.MOD_NETWORK.sendToServer(new PacketItemStack(is.getTagCompound(), player.getName()));
					}
				});
				return;
			}
		}
		
	
		
		
		/**
		 * モデルの登録を行うためのイベントを発火する場所。
		 * ここで登録しないと間に合わずぬるぽが発生する
		 * @param event
		 */
		@SubscribeEvent
		public static void addModels(ModelRegistryEvent event) {
			proxy.registerItemModels(); // ここでモデルの登録を行わないといけない
		}
		
		
	}
	
	public static class GTSPacket {
		
		private static int packetID; // 最大予約パケットID
		
		public static void init() {
			// パケットを登録する
			GTSPacket.registerPacket(PacketTrafficController.class, PacketTrafficController.class, Side.SERVER);
			GTSPacket.registerPacket(PacketTrafficLight.class, PacketTrafficLight.class, Side.SERVER);
			GTSPacket.registerPacket(PacketTrafficPole.class, PacketTrafficPole.class, Side.SERVER);
			GTSPacket.registerPacket(PacketTrafficButton.class, PacketTrafficButton.class, Side.SERVER);
			
			// モデル更新を伝えるためのパケット
			GTSPacket.registerPacket(PacketItemStack.class, PacketItemStack.class, Side.SERVER);
		}
		
		/**
		 * 第1引数と第2引数はともに同じクラスを指定すること。Sideはどっち側か。
		 * @param messageHandler
		 * @param requestMessageType
		 * @param side
		 */
		public static <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
		{
			GTS.MOD_NETWORK.registerMessage(messageHandler, requestMessageType, packetID++, side); // 登録する
		}
	}
	
	/**
	 * ※モデルパックではなく、単にMod自体の設定を行うところである
	 */
	@Config(modid = GTS.MOD_ID, name = "GTSConfig")
	public static class GTSConfig {
		
		@Config.RangeInt(min = 1, max = 256)
		@Config.Comment("Values that are too large can delay the game.")
		public static int detectRange = 8; // 制御機などの検出範囲（いずれは廃止する予定ですが）
	}
}
