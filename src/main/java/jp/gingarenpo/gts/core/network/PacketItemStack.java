package jp.gingarenpo.gts.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemStack implements IMessage, IMessageHandler<PacketItemStack, IMessage> {
	
	/**
	 * ItemStackのcompoundタグを保持する。
	 */
	NBTTagCompound compound;
	
	String name;
	
	/**
	 * パケットの初期化の為にデフォルトコンストラクタが必要
	 */
	public PacketItemStack() {}
	
	/**
	 * タグとプレイヤーのIDを渡す
	 * @param compound
	 * @param name
	 */
	public PacketItemStack(NBTTagCompound compound, String name) {
		this.compound = compound;
		this.name = name;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.compound = ByteBufUtils.readTag(buf);
		this.name = ByteBufUtils.readUTF8String(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.compound);
		ByteBufUtils.writeUTF8String(buf, this.name);
	}
	
	@Override
	public IMessage onMessage(PacketItemStack message, MessageContext ctx) {
		// パケットを受け取ったらWorld内のプレイヤーに反映する
		ctx.getServerHandler().player.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(message.compound);
		return null;
	}
}
