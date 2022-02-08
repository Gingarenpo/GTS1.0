package jp.gingarenpo.gts.controller;

import io.netty.buffer.ByteBuf;
import jp.gingarenpo.gts.GTS;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Level;

import java.util.List;

/**
 * 交通信号制御機に関するパケットの中身。パケットの送受信の際に行わせる処理を記載する。
 */
public class PacketTrafficController implements IMessage, IMessageHandler<PacketTrafficController, IMessage> {
	
	private TileEntityTrafficController te; // TileEntity自体を保管するために必要（データを返さなくてはならない）
	private NBTTagCompound data; // TileEntityのNBTタグ
	
	public TrafficController tcData; // 制御機のデータ（カスタム格納部分）
	
	public PacketTrafficController() {} // パケットを新規作成する際に必要
	
	/**
	 * パケットから新たにTileEntityを呼び出す場合に使用する
	 * @param te
	 */
	public PacketTrafficController(TileEntityTrafficController te) {
		this.te = te; // Nullの可能性はないとする
		this.data = te.getUpdateTag(); // 最新のタグを取得する
	}
	
	/**
	 * パケットではバイト列で送信するため、ここでバイト列からの復元を行う。
	 * @param buf
	 */
	@Override
	public void fromBytes(ByteBuf buf) {
		this.data = ByteBufUtils.readTag(buf); // これでカスタムデータは反映されているのだろうか…
	}
	
	/**
	 * パケットではバイト列で送信するため、ここでバイト列への変換を行う。
	 * @param buf
	 */
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.data); // タグ自体を書き込むがどうやらこれはデフォルトの物しか書き込んでくれない？（getUpdateTagにしたから平気なはずだが）
	}
	
	/**
	 * パケットを受け取ったときに実行する処理を記す。returnの部分には何かしら応答があればそれを返すが今回は内部で処理が完結するのでなし
	 * @param message パケット
	 * @param ctx コンテキスト
	 * @return
	 */
	@Override
	public IMessage onMessage(PacketTrafficController message, MessageContext ctx) {
		// パケットからTileEntityを取得する
		World world = ctx.getServerHandler().player.world; // サーバー側からプレイヤーのいるワールドを取得する
		TileEntityTrafficController te = message.getTileEntityFromPacket(world); // パケットからTileEntityを取得する
		te.setData(tcData); // 最新の情報をセットする
		GTS.GTSLog.log(Level.INFO, "Message recieved. " + te);
		return null;
	}
	
	/**
	 * パケットの中身にあるTileEntityの情報を取得する。中にXYZの座標がないと死ぬ。
	 * 存在しない場合はnullを返す。
	 * @param world 世界（NonNull）
	 * @return
	 */
	public TileEntityTrafficController getTileEntityFromPacket(World world) {
		BlockPos pos = new BlockPos(this.data.getInteger("X"), this.data.getInteger("Y"), this.data.getInteger("Z")); // デフォであるやつ
		TileEntity te = world.getTileEntity(pos); // 世界からその場所のTileEntityを取得する
		if (te == null) {
			// もしもそこにTileEntityが存在しない場合、最終手段としてその世界で読み込まれているすべてのTileEntityと照らし合わせる
			for (TileEntity load: world.loadedTileEntityList) {
				// 読み込まれたすべてのTileEntityにおいて繰り返し
				if (pos.equals(load.getPos()) && load instanceof TileEntityTrafficController) {
					// 場所が一致した場合はそれがこの場所のTileEntityである（ただしインスタンスも一致している必要がある）
					return (TileEntityTrafficController) load;
				}
			}
		}
		// ここまで来てしまったということは、もうその場所にTileEntityが存在しないということである
		return null; // 存在しない
	}
}
