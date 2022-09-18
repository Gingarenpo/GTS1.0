package jp.gingarenpo.gts.sign;

import io.netty.buffer.ByteBuf;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.sign.data.TrafficSign;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Level;

/**
 * サーバーとやり取りをするためのパケットを作成したり受け取ったときに展開したりするためのクラス
 */
public class PacketTrafficSign implements IMessage, IMessageHandler<PacketTrafficSign, IMessage> {
	
	/**
	 * TileEntityを作成する目的で保持する必要がある
	 */
	private TileEntityTrafficSign te;
	
	/**
	 * 基本的に上記teから取得できるが変なタイミングで初期化されるので
	 * フィールドとして別途定義しておくことも必要。
	 */
	private NBTTagCompound data;
	
	/**
	 * TileEntityから格納されているデータを格納する。fromBytesとかで使う。
	 */
	private TrafficSign tsData;
	
	/**
	 * パケットを新規作成するためのコンストラクタ。ソースからの利用はまず行わない。
	 */
	public PacketTrafficSign() {}
	
	/**
	 * 指定したTileEntityを使用したパケットを作成する。
	 * このパケットを送受信することが可能となる。
	 * @param te
	 */
	public PacketTrafficSign(TileEntityTrafficSign te) {
		this.te = te;
		this.data = te.getUpdateTag(); // こうすることで最新のNBTタグを取得できる
	}
	
	/**
	 * バイト列からデータを復元する。
	 * @param buf
	 */
	@Override
	public void fromBytes(ByteBuf buf) {
		this.data = ByteBufUtils.readTag(buf); // カスタムデータまですべて読み込めていると信じる
		
	}
	
	/**
	 * バイト列にデータを変換する。
	 * @param buf
	 */
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.data); // カスタムデータも含めてすべてをバイト列にしてくれる
	}
	
	/**
	 * パケットを受け取ったときにすべきこと。returnは応答を返すが必要ないのでなし
	 * @param message 受け取ったパケット。
	 * @param ctx コンテキスト
	 * @return null
	 */
	@Override
	public IMessage onMessage(PacketTrafficSign message, MessageContext ctx) {
		World world = ctx.getServerHandler().player.world; // サーバー側からプレイヤーのいるワールドを取得する
		TileEntityTrafficSign te = message.getTileEntityFromPacket(world); // パケットからTileEntityを取得する
		te.setData(tsData);
		GTS.GTSLog.log(Level.INFO, "Message recieved. " + te);
		return null;
	}
	
	/**
	 * パケットの中身にあるTileEntityの情報を取得する。中にXYZの座標がないと死ぬ。
	 * 存在しない場合はnullを返す。
	 * @param world 世界（NonNull）
	 * @return
	 */
	public TileEntityTrafficSign getTileEntityFromPacket(World world) {
		BlockPos pos = new BlockPos(this.data.getInteger("X"), this.data.getInteger("Y"), this.data.getInteger("Z")); // デフォであるやつ
		TileEntity te = world.getTileEntity(pos); // 世界からその場所のTileEntityを取得する
		if (te == null) {
			// もしもそこにTileEntityが存在しない場合、最終手段としてその世界で読み込まれているすべてのTileEntityと照らし合わせる
			for (TileEntity load: world.loadedTileEntityList) {
				// 読み込まれたすべてのTileEntityにおいて繰り返し
				if (pos.equals(load.getPos()) && load instanceof TileEntityTrafficSign) {
					// 場所が一致した場合はそれがこの場所のTileEntityである（ただしインスタンスも一致している必要がある）
					return (TileEntityTrafficSign) load;
				}
			}
		}
		// ここまで来てしまったということは、もうその場所にTileEntityが存在しないということである
		return null; // 存在しない
	}
}
