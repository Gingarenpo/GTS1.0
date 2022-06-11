package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.BlockTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.data.DummyConfig;
import jp.gingarenpo.gts.data.Model;
import jp.gingarenpo.gts.core.GTSTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Level;

import java.io.IOException;

/**
 * 交通信号機（受信機）のTileEntity情報。
 * 動的ﾃｸｽﾁｬをとにかく大量に生成するためメモリ不足で落ちるかもしれない
 */
public class TileEntityTrafficLight extends GTSTileEntity {
	
	private Model addon = null; // この信号機が一体どの種類のモデルを使うのか。基本的に必ず何かしらが入るはず
	private TrafficLight data = null; // この信号機のデータ
	
	
	public TileEntityTrafficLight() {
		setDummyModel();
		setData(new TrafficLight(0));
	}
	
	/**
	 * このモデルに対してパックが指定されていないときや、何らかのエラーでモデルが表示できない場合は
	 * 代わりに備え付けのダミーモデルを指定する。RTMでも謎の信号機が設置されることがあったと思うが
	 * あれみたいなもの。あくまで設置したんだよっていうのが分かるように。]
	 *
	 * 普通ありえないがファイルが見つからない場合は例外出しまくるので判断してください。
	 */
	private void setDummyModel() {
		try {
			addon = new Model(new DummyConfig(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/test.mqo")).getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 設置されている場所からModの設定で指定した範囲内の制御機を検出して、
	 * 最も近い制御機に対してアタッチする。現在は一番近いもの固定だが、このメソッドは自由選択を可能にする場合
	 * いずれ廃止されるであろう。
	 *
	 * @param player チャット欄を呼び出すのに必要。nullの場合は世界全体に向けて発信する！！！！
	 */
	public void search(EntityPlayer player) {
		// ※Worldインスタンスを使用するのでコンストラクタでは使用できない
		if (world == null) {
			// インスタンスがない場合はログエラーとする
			GTS.GTSLog.log(Level.ERROR, "Cannot call search method before construction!"); // World インスタンス ない
			return;
		}
		if (!world.isRemote) {
			// クライアントでは実行しない
			return;
		}
		// 近い順にみていくためどうしてもこのループは必要になる
		TileEntity te = null; // とりあえずnull
		for (int x = this.pos.getX()-GTS.GTSConfig.detectRange; x <= this.pos.getX()+GTS.GTSConfig.detectRange; x++) {
			for (int y = this.pos.getY()-GTS.GTSConfig.detectRange; y <= this.pos.getY()+GTS.GTSConfig.detectRange; y++) {
				for (int z = this.pos.getZ()-GTS.GTSConfig.detectRange; z <= this.pos.getZ()+GTS.GTSConfig.detectRange; z++) {
					// 3重ループになるけど仕方がない
					Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock(); // そこにあるブロックを取得（TileEntityは読み込めないようです）
					if (b instanceof BlockTrafficController) {
						te = world.getTileEntity(new BlockPos(x, y, z));
					}
				}
			}
		}
		if (te == null) {
			// 見つからなかったということになる
			player.sendMessage(new TextComponentString("Traffic Controller not found."));
			return;
		}
		
		// そのTileEntityは絶対に制御機なので代入する
		TileEntityTrafficController tetc = (TileEntityTrafficController) te;
		this.data.setParent(tetc.getData()); // 制御機のデータを格納する
		player.sendMessage(new TextComponentString("Traffic Controller found(X=" + tetc.getPos().getX() + ", Y=" + tetc.getPos().getY() + ", Z=" + tetc.getPos().getZ() + ")"));
	}
	
	/**
	 * 制御機がアタッチされているかを返す。
	 * @return
	 */
	public boolean isAttached() {
		return this.data.getParent() != null;
	}
	
	public Model getAddon() {
		return addon;
	}
	
	public void setAddon(Model addon) {
		this.addon = addon;
	}
	
	public TrafficLight getData() {
		return data;
	}
	
	public void setData(TrafficLight data) {
		this.data = data;
	}
}
