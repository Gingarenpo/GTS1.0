package jp.gingarenpo.gts.sign;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.sign.data.TrafficSign;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.io.*;

/**
 * 地名板などの看板を作成するためのもの
 * 単にこの中にはTrafficSignが格納されているだけ
 */
public class TileEntityTrafficSign extends GTSTileEntity {
	
	/**
	 * コンフィグデータ系
	 */
	public TrafficSign data;
	
	/**
	 * 内部でテクスチャの管理に使われる名称（適当にするので変更不可）
	 */
	private String name;
	
	/**
	 * テクスチャが変更されたことを通知するための物。
	 */
	private boolean texChange;
	
	public TileEntityTrafficSign() {}
	public TileEntityTrafficSign(TrafficSign data) {
		this.data = data;
		this.name = RandomStringUtils.randomAlphanumeric(64); // 64文字もあれば被らないでしょ
	}
	
	public TrafficSign getData() {
		return data;
	}
	
	public void setData(TrafficSign data) {
		this.data = data;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		name = compound.getString("gts_sign_name");
		texChange = compound.getBoolean("gts_sign_tex");
		
		// data読む
		try (ByteArrayInputStream bais = new ByteArrayInputStream(compound.getByteArray("gts_sign_data"))) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				data = (TrafficSign) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			// 死んだとき
			GTS.GTSLog.warn("Warning. cannot load gts_sign_data.");
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		nbt.setString("gts_sign_name", name);
		nbt.setBoolean("gts_sign_tex", texChange);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(data);
			}
			nbt.setByteArray("gts_sign_data", baos.toByteArray());
		}
		catch (IOException e) {
			// 書き込みに失敗した場合
			e.printStackTrace();
			GTS.GTSLog.warn("Warning. cannot write gts_sign_data.");
		}
		return nbt;
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
	
	@Override
	public String toString() {
		return "TileEntityTrafficSign{" +
					   "data=" + data +
					   '}';
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isTexChange() {
		return texChange;
	}
	
	public void setTexChange(boolean texChange) {
		this.texChange = texChange;
	}
}
