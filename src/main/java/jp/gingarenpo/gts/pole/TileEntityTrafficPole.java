package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.arm.TrafficArm;
import jp.gingarenpo.gts.core.GTSTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;

/**
 * 電柱（ポール）のTileEntity。Version1現在ではあまり意味をなさないが、
 * 将来的に可変的な対応をできるようにする予定。
 *
 * 現在はポールのモデルのみを格納する。
 */
public class TileEntityTrafficPole extends GTSTileEntity implements ITickable {
	
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
	
	/**
	 * 接続されているアーム。何も接続されていない場合はnullが格納される。
	 */
	private TrafficArm arm;
	
	/**
	 * アームの接続が行われている最中かどうか。
	 */
	private boolean preConnect = false;
	
	private AxisAlignedBB renderBoundingBox = null;
	
	
	
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
			this.addon = (new ModelTrafficPole(new DummyConfigTrafficPole(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_tp.mqo")).getInputStream()), null));
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
	
	public TrafficArm getArm() {
		return arm;
	}
	
	public void setArm(TrafficArm arm) {
		this.arm = arm;
	}
	
	/**
	 * おそらく、レンダー範囲を返すものだと思われる。通常このブロック（デフォルト1x1）から視点が外れると
	 * レンダリングされなくなるためアームがちぎれるといった現象が発生するが、
	 * それを回避できるメソッドと思われる。毎回呼び出されるのか最初に呼び出されるのかは謎。
	 * @return レンダリングすべき範囲
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		// アタッチしている交通信号機の最小座標と最大座標を取得してその範囲とする
		if (renderBoundingBox == null) return super.getRenderBoundingBox();
		return renderBoundingBox;
		
	}
	
	/**
	 * レンダーボックスの再計算を行う。毎フレームやるには重いので必要に応じて呼び出す。
	 */
	public void calcRenderBoundingBox() {
		if (arm == null) {
			return;
		}
		
		double minX = getPos().getX();
		double minY = getPos().getY();
		double minZ = getPos().getZ();
		double maxX = getPos().getX() + 1;
		double maxY = getPos().getY() + 1;
		double maxZ = getPos().getZ() + 1;
		
		for (double[] to: arm.getTo()) {
			minX = Math.min(to[0], minX);
			minY = Math.min(to[1], minY);
			minZ = Math.min(to[2], minZ);
			
			maxX = Math.max(to[0], maxX);
			maxY = Math.max(to[1], maxY);
			maxZ = Math.max(to[2], maxZ);
		}
		
		this.renderBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		
		
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
		if (compound.hasKey("gts_tp_arm")) {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(compound.getByteArray("gts_tp_arm"))) {
				try (ObjectInputStream ois = new ObjectInputStream(bais)) {
					this.arm = (TrafficArm) ois.readObject();
				}
			} catch (IOException | ClassNotFoundException e) {
				// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
				GTS.GTSLog.log(Level.DEBUG, "Can't load arm object (Maybe out of memory or data == null) -> " + e.getMessage());
			}
		}
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(compound.getByteArray("gts_tp_addon"))) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				this.addon = (ModelTrafficPole) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			// メモリ不足などでストリームを確保できなかった場合、あるいはオブジェクトが正しく読み込まれなかった時
			GTS.GTSLog.log(Level.DEBUG, "Can't load Traffic addon object (Maybe out of memory or data == null) -> " + e.getMessage());
		}
		top = compound.getBoolean("gts_tp_top");
		bottom = compound.getBoolean("gts_tp_bottom");
		packLocation = compound.getString("gts_tp_pl").isEmpty() ? null : new File(compound.getString("gts_tp_pl"));
		
		this.calcRenderBoundingBox();
		
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound c = super.writeToNBT(compound); // デフォルトの情報（XYZとか）を書き込んでもらう
		c.setBoolean("gts_tp_top", top);
		c.setBoolean("gts_tp_bottom", bottom);
		c.setString("gts_tp_pl", (packLocation == null) ? "" : packLocation.getAbsolutePath());
		if (arm != null) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
					oos.writeObject(this.arm); // アームの情報を書き込む
					c.setByteArray("gts_tp_arm", baos.toByteArray()); // バイト列にしてタグに書き込み
				}
			} catch (IOException e) {
				// メモリ不足などでストリームを確保できなかった場合
				GTS.GTSLog.log(Level.ERROR, "Can't write addon object(Maybe out of memory) -> " + e.getMessage());
			}
		}
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
	
	/**
	 * アームの接続を開始する。
	 */
	public void startConnect() {
		this.preConnect = true;
	}
	
	/**
	 * アームの接続を解除する。
	 */
	public void endConnect() {
		this.preConnect = false;
	}
	
	public boolean isPreConnect() {
		return this.preConnect;
	}
	
	@Override
	public void update() {
		if (this.addon == null) return;
		if (this.addon.isNeedChangeTex()) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			this.addon.doneChangeTex();
		}
	}
}
