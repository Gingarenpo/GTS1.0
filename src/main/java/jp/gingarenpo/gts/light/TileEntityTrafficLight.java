package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.BlockTrafficController;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.data.DummyConfig;
import jp.gingarenpo.gts.data.Model;
import jp.gingarenpo.gts.data.Pack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;

/**
 * 交通信号機（受信機）のTileEntity情報。
 * 動的ﾃｸｽﾁｬをとにかく大量に生成するためメモリ不足で落ちるかもしれない
 */
public class TileEntityTrafficLight extends GTSTileEntity implements ITickable {
	
	private Pack pack = null; // ダミーの場合以外は使用するPackが格納される
	private Model addon = null; // この信号機が一体どの種類のモデルを使うのか。基本的に必ず何かしらが入るはず
	private TrafficLight data = null; // この信号機のデータ
	
	/**
	 * デフォルトコンストラクタは基本呼び出さない。
	 */
	public TileEntityTrafficLight() {
		setData(new TrafficLight(1));
	}
	
	/**
	 * true入れようがfalse入れようがダミーが入る
	 * @param dummy
	 */
	public TileEntityTrafficLight(boolean dummy) {
		setDummyModel();
		setData(new TrafficLight(1));
	}
	
	/**
	 * TileEntityがWorldに読み込まれたときに実行すべきこと。
	 * 初期状態ではNoopなのでスーパーメソッドを呼び出す必要はない。
	 */
	@Override
	public void onLoad() {
		attach(); // ここじゃないとWorldインスタンスが出来上がっていない
	}
	
	/**
	 * このモデルに対してパックが指定されていないときや、何らかのエラーでモデルが表示できない場合は
	 * 代わりに備え付けのダミーモデルを指定する。RTMでも謎の信号機が設置されることがあったと思うが
	 * あれみたいなもの。あくまで設置したんだよっていうのが分かるように。
	 *
	 * 普通ありえないがファイルが見つからない場合は例外出しまくるので判断してください。
	 */
	private void setDummyModel() {
		try {
			addon = new Model(new DummyConfig(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_model.mqo")).getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * この信号機がダミーモデルを使用している場合に渡される
	 * @return ダミーならtrue
	 */
	public boolean isDummy() {
		return pack == null;
	}
	
	
	/**
	 * Player関係なしにやりたい場合はこっち。成功すると（実行されると）true
	 */
	public boolean attach() {
		// ※Worldインスタンスを使用するのでコンストラクタでは使用できない
		if (world == null) {
			// インスタンスがない場合はログエラーとする
			GTS.GTSLog.log(Level.ERROR, "Cannot call search method before construction!"); // World インスタンス ない
			return false;
		}
		if (world.isRemote) {
			// クライアントでは実行しない
			GTS.GTSLog.log(Level.INFO, "Client side can't attach"); // World インスタンス ない
			return false;
		}
		// 探す
		TileEntityTrafficController te = this.search();
		if (te != null) {
			data.setParent(te.getData());
			GTS.GTSLog.log(Level.INFO, "Traffic Controller Found. {} {} {}", pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		GTS.GTSLog.log(Level.ERROR, "Can't attach Traffic Controller."); // World インスタンス ない
		return false;
	}
	
	/**
	 * 設置場所から半径nの範囲の制御機を探す。あればそのインスタンス、なければnullを返す
	 * @return
	 */
	private TileEntityTrafficController search() {
		// 近い順にみていくためどうしてもこのループは必要になる
		if (world == null) {
			// 救済策としてgetMinecraftから読み込む
			GTS.GTSLog.log(Level.ERROR, "Can't get world instance from TileEntity."); // World インスタンス ない
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.world == null) {
				// これもnullならもう無理
				GTS.GTSLog.log(Level.ERROR, "Can't get world instance from Minecraft Instance."); // World インスタンス ない
				return null;
			}
			this.world = mc.world;
		}
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
		if (te == null) return null;
		return (TileEntityTrafficController) te;
	}
	
	/**
	 * 制御機がアタッチされているかを返す。
	 * @return
	 */
	public boolean isAttached() {
		return this.data.isAttached();
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
	
	public Pack getPack() {
		return pack;
	}
	
	public void setPack(Pack pack) {
		this.pack = pack;
	}
	
	/**
	 * NBTタグから情報を取り出し、このTileEntityに保管する。
	 * @param compound
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		// Model、Dataの順で格納されている（バイト列）
		byte[] byteModel = compound.getByteArray("gts_tl_model");
		byte[] byteData = compound.getByteArray("gts_tl_data");
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(byteModel)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.addon = (Model) ois.readObject();
				GTS.GTSLog.log(Level.ERROR, "Read NBT(TL). addon -> " + addon);
			}
		} catch (IOException | ClassNotFoundException e) {
			// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
			GTS.GTSLog.log(Level.ERROR, "Can't load data object Phase1[model](Maybe out of memory or data == null) -> " + e.getMessage());
		}
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(byteData)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.data = (TrafficLight) ois.readObject();
				GTS.GTSLog.log(Level.DEBUG, "Read NBT(TL). data -> " + data);
			}
		} catch (IOException | ClassNotFoundException e) {
			// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
			GTS.GTSLog.log(Level.DEBUG, "Can't load data object Phase1[model](Maybe out of memory or data == null) -> " + e.getMessage());
		}
		
		// パックの情報はFile文字列だけで格納するが
		if (!compound.hasKey("gts_tl_pack_path")) {
			this.setDummyModel(); // ダミーモデルを代わりに読み込む
			GTS.GTSLog.info("Can't find pack path(Maybe it is dummy).");
			return; // そもそもパック情報がない場合はダミーモデルを用意する
		}
		this.pack = GTS.loader.getPack(new File(compound.getString("gts_tl_pack_path")));
		if (this.pack == null) {
			// パックの読み込みに失敗した場合
			this.setDummyModel(); // ダミーモデルを代わりに読み込む
			GTS.GTSLog.warn("Can't load pack from NBT. It is now set dummy model.");
			return;
		}
		
		// パックの情報を読み込んだらその中のモデルを読み込む
		this.addon = this.pack.getModel(this.addon.getConfig().getId()); // ないとnull
		if (this.addon == null) {
			// モデルの読み込みに失敗した場合
			this.setDummyModel(); // ダミーモデルを代わりに読み込む
			GTS.GTSLog.warn("Can't load model from pack. It is now set dummy model.");
			return;
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound c = super.writeToNBT(compound); // デフォルトの情報（XYZとか）を書き込んでもらう
		
		// Model、Dataの順で格納する
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.addon); // アドオンの情報を書き込む
				c.setByteArray("gts_tl_model", baos.toByteArray()); // バイト列にしてタグに書き込み
			}
		} catch (IOException e) {
			// メモリ不足などでストリームを確保できなかった場合
			GTS.GTSLog.log(Level.ERROR, "Can't write data object Phase1[Model](Maybe out of memory) -> " + e.getMessage());
		}
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.data); // アドオンの情報を書き込む
				c.setByteArray("gts_tl_data", baos.toByteArray()); // バイト列にしてタグに書き込み
			}
		} catch (IOException e) {
			// メモリ不足などでストリームを確保できなかった場合
			GTS.GTSLog.log(Level.ERROR, "Can't write data object Phase2[Data](Maybe out of memory) -> " + e.getMessage());
		}
		
		GTS.GTSLog.log(Level.DEBUG, "Write NBT(TL). data = " + data + ", addon = " + addon);
		
		// パックの情報を文字列で入れ込む
		if (isDummy()) return c; // ダミーの場合は入れない
		c.setString("gts_tl_pack_path", pack.getLocation().getAbsolutePath());
		
		return c;
		
		
	}
	
	/**
	 * サーバーからクライアントに同期させるために必要な更新パケットを取得する。
	 * ここで返すのは、NBTタグを内包したパケットクラスのオブジェクトとなる。独自拡張は可能（継承さえしていれば）。
	 * 今回はバニラにあらかじめ設定されているTileEntity送信用のパケットクラスを使用する。
	 *
	 * @return
	 */
	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, -1, this.getUpdateTag()); // パケットを送信する
	}
	
	/**
	 * サーバー側で何をするかってところだと思うが正確な説明はできない。
	 * おそらく、パケットを送信する準備を行う際に、ここでパケットのnbtタグを準備しろってことだろう。
	 * つまり、このメソッドが呼び出された時点でのNBTタグを送信することになるので、NBTを更新するべき！
	 * @return
	 */
	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound()); // 更新情報を通知するためのNBTタグを新規に作成し、今の状況を書き込み返す
	}
	
	/**
	 * パケットが受信されてきた際に行う処理。netが何を意味しているのかは不明だが、パケットの中にNBTタグが入っているためそれを読み出す。
	 * @param net チャンネルに関するあれこれを取得できるらしい（外部通信との際に必要？）
	 * @param pkt パケット
	 */
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		// super.onDataPacket(net, pkt); // 中身何もしていないのでいらない
		this.readFromNBT(pkt.getNbtCompound()); // パケットからNBTタグを取り出して、読み込ませる
	}
	
	
	/**
	 * 毎Tickパケットを受け取ることで更新を行うが重くなる可能性があるので要検討
	 */
	@Override
	public void update() {
		if (this.data != null & this.data.getParent() != null && this.data.getParent().isNeedChange()) {
			// 更新の必要性がある場合
			this.data.getParent().notifyDone();
			world.notifyBlockUpdate(this.pos, world.getBlockState(pos), world.getBlockState(pos), 2); // 更新通知
		}
	}
}
