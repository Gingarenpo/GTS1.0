package jp.gingarenpo.gts.button;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.*;

public class TileEntityTrafficButton extends GTSTileEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * ボタン自体のデータ。
	 */
	private TrafficButton button;
	
	/**
	 * ボタンの描画角度。
	 */
	private double angle;
	
	/**
	 * ボタンのモデル。
	 */
	private ModelTrafficButton addon;
	
	public TileEntityTrafficButton() {
		this.button = new TrafficButton();
		this.setDummyModel();
		this.markDirty();
	}
	
	/**
	 * このボタンにダミーのモデルを適用する。
	 */
	public void setDummyModel() {
		try {
			this.addon = new ModelTrafficButton(new DummyConfigTrafficButton(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_tb.mqo")).getInputStream()), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TrafficButton getButton() {
		return button;
	}
	
	public void setButton(TrafficButton button) {
		this.button = button;
	}
	
	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	public ModelTrafficButton getAddon() {
		return addon;
	}
	
	public void setAddon(ModelTrafficButton addon) {
		this.addon = addon;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		// 角度を落とし込む
		this.angle = compound.getDouble("gts_tb_angle");
		
		// アドオンのコンフィグを読み込む
		byte[] configData = compound.getByteArray("gts_tb_config");
		try (ByteArrayInputStream bais = new ByteArrayInputStream(configData)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.addon.setConfig((ConfigTrafficButton) ois.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			GTS.GTSLog.warn("Warning. Cannot read config of Button -> " + e.getMessage());
		}
		
		// アドオンのファイルを読み込む
		byte[] fileData = compound.getByteArray("gts_tb_file");
		try (ByteArrayInputStream bais = new ByteArrayInputStream(fileData)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.addon.setFile((File) ois.readObject());
				
				// コンフィグの再読み込みをする
				this.addon.reloadModel();
				this.addon.reloadTexture();
				
				if (this.addon.getFile() == null) {
					GTS.GTSLog.debug("Button dummy used");
					this.setDummyModel();
					//System.out.println(this.addon);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			GTS.GTSLog.warn("Warning. Cannot read file of Button -> " + e.getMessage());
		}
		
		// アドオンのコンフィグを読み込む
		byte[] data = compound.getByteArray("gts_tb_data");
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.button = ((TrafficButton) ois.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			GTS.GTSLog.warn("Warning. Cannot read data of Button -> " + e.getMessage());
		}
		
		//GTS.GTSLog.info("Read NBT(button)");
		
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		
		// 角度を落とし込む
		compound.setDouble("gts_tb_angle", angle);
		
		// コンフィグ
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.addon.getConfig());
				compound.setByteArray("gts_tb_config", baos.toByteArray());
			}
		} catch (IOException e) {
			GTS.GTSLog.warn("Warning. Cannot write config of Button -> " + e.getMessage());
		}
		
		// ファイル
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.addon.getFile());
				compound.setByteArray("gts_tb_file", baos.toByteArray());
			}
		} catch (IOException e) {
			GTS.GTSLog.warn("Warning. Cannot write file of Button -> " + e.getMessage());
		}
		
		// ボタン自体のデータ
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(this.button);
				compound.setByteArray("gts_tb_data", baos.toByteArray());
			}
		} catch (IOException e) {
			GTS.GTSLog.warn("Warning. Cannot write data of Button -> " + e.getMessage());
		}
		
		//GTS.GTSLog.info("Write NBT(button)");
		
		return compound;
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
}
