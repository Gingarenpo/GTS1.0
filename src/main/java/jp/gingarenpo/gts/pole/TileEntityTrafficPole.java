package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;

/**
 * 電柱（ポール）のTileEntity。Version1現在ではあまり意味をなさないが、
 * 将来的に可変的な対応をできるようにする予定。
 *
 * 現在はポールのモデルのみを格納する。
 */
public class TileEntityTrafficPole extends GTSTileEntity {
	
	/**
	 * ポールのアドオンクラス。
	 */
	private ModelTrafficPole addon;
	
	/**
	 * このポールは先頭かどうか
	 */
	private boolean top = false;
	
	/**
	 * このポールは土台かどうか
	 */
	private boolean bottom = false;
	
	/**
	 * このポールのダイナミックテクスチャ格納場所
	 */
	private ResourceLocation texture;
	
	/**
	 * パックのある場所（ロケーション）。これがないとパックを読み出せない
	 */
	private File packLocation;
	
	public ResourceLocation getTexture() {
		return texture;
	}
	
	public void setTexture(ResourceLocation texture) {
		this.texture = texture;
	}
	
	public TileEntityTrafficPole() {
		// ダミーのコンフィグを入れる
		setDummyModel();
	}
	
	public void setDummyModel() {
		try {
			this.addon = (new ModelTrafficPole(new DummyConfigTrafficPole(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_tp.mqo")).getInputStream())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.markDirty();
	}
	
	public ModelTrafficPole getAddon() {
		return addon;
	}
	
	public void setAddon(ModelTrafficPole addon) {
		this.addon = addon;
	}
	
	public boolean isTop() {
		return top;
	}
	
	public File getPackLocation() {
		return packLocation;
	}
	
	public void setPackLocation(File packLocation) {
		this.packLocation = packLocation;
	}
	
	/**
	 * bottomと排他的となるため、ここで指定されたものがtrueで一緒になる場合
	 * 自動的にbottomはfalseになる。
	 * @param top 上部かどうか。
	 */
	public void setTop(boolean top) {
		this.top = top;
		if (top && bottom) {
			bottom = false;
		}
	}
	
	public boolean isBottom() {
		return bottom;
	}
	
	/**
	 * topと排他的となるため、ここで指定されたものがtrueで一緒になる場合
	 * 自動的にtopはfalseになる。
	 * @param bottom 下部かどうか。
	 */
	public void setBottom(boolean bottom) {
		this.bottom = bottom;
		if (bottom && top) {
			top = false;
		}
	}
	
	/**
	 * NBTタグから情報を取り出し、このTileEntityに保管する。
	 * @param compound
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(compound.getByteArray("gts_tp_addon"))) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.addon = (ModelTrafficPole) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
			GTS.GTSLog.log(Level.DEBUG, "Can't load addon object (Maybe out of memory or data == null) -> " + e.getMessage());
		}
		top = compound.getBoolean("gts_tp_top");
		bottom = compound.getBoolean("gts_tp_bottom");
		packLocation = compound.getString("gts_tp_pl").isEmpty() ? null : new File(compound.getString("gts_tp_pl"));
		
		
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound c = super.writeToNBT(compound); // デフォルトの情報（XYZとか）を書き込んでもらう
		c.setBoolean("gts_tp_top", top);
		c.setBoolean("gts_tp_bottom", bottom);
		c.setString("gts_tp_pl", (packLocation == null) ? "" : packLocation.getAbsolutePath());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.addon); // アドオンの情報を書き込む
				c.setByteArray("gts_tp_addon", baos.toByteArray()); // バイト列にしてタグに書き込み
			}
		} catch (IOException e) {
			// メモリ不足などでストリームを確保できなかった場合
			GTS.GTSLog.log(Level.ERROR, "Can't write addon object(Maybe out of memory) -> " + e.getMessage());
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("addon = ").append(addon).append(". ");
		sb.append(top ? "It's top." : (bottom ? "It's bottom." : "It's base."));
		return sb.toString();
	}
}
