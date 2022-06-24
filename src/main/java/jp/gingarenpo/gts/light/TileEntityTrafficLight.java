package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.data.DummyConfig;
import jp.gingarenpo.gts.data.Model;
import jp.gingarenpo.gts.data.Pack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;

/**
 * 交通信号機（受信機）のTileEntity情報。
 * 動的ﾃｸｽﾁｬをとにかく大量に生成するためメモリ不足で落ちるかもしれない
 */
public class TileEntityTrafficLight extends GTSTileEntity implements ITickable, Serializable {
	
	private Model addon = null; // この信号機が一体どの種類のモデルを使うのか。基本的に必ず何かしらが入るはず
	private TrafficLight data = null; // この信号機のデータ
	private double angle = 0.0f; // 設置された向き（角度）
	
	
	/**
	 * true入れようがfalse入れようがダミーが入る
	 *
	 */
	public TileEntityTrafficLight() {
		setDummyModel();
		setData(new TrafficLight(1));
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
	
	
	/**
	 * NBTタグから情報を取り出し、このTileEntityに保管する。
	 * @param compound
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		// Model、Dataの順で格納されている（バイト列）
		byte[] byteData = compound.getByteArray("gts_tl_data");
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(byteData)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.data = (TrafficLight) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
			GTS.GTSLog.log(Level.DEBUG, "Can't load data object Phase1[model](Maybe out of memory or data == null) -> " + e.getMessage());
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound c = super.writeToNBT(compound); // デフォルトの情報（XYZとか）を書き込んでもらう
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.data); // アドオンの情報を書き込む
				c.setByteArray("gts_tl_data", baos.toByteArray()); // バイト列にしてタグに書き込み
			}
		} catch (IOException e) {
			// メモリ不足などでストリームを確保できなかった場合
			GTS.GTSLog.log(Level.ERROR, "Can't write data object Phase2[Data](Maybe out of memory) -> " + e.getMessage());
		}
		
		
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
	 *
	 * なおあくまでこれはパケットで送信するタグであるためわざわざobjストリーム展開させる意味はないと思う
	 * @return
	 */
	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound()); // 更新情報を通知するためのNBTタグを新規に作成し、今の状況を書き込み返す
	}
	
	/**
	 * getUpdateTagで取得した？情報を処理するためのメソッドらしい。よくわからない。
	 * @param tag
	 */
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
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
	 * アタッチ制御機を見に行くのではなく自機の信号機インスタンスを読みだして処理を行う
	 */
	@Override
	public void update() {
		if (this.data.isUpdate()) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			this.data.doneUpdate();
		}
	}
	
	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double angle) {
		this.angle = angle;
	}
}
