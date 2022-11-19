package jp.gingarenpo.gts.sign;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.sign.data.NamedTrafficSign;
import jp.gingarenpo.gts.sign.data.TrafficSign;
import jp.gingarenpo.gts.sign.gui.SwingGUITrafficSign;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * ブロックとして置けるようにするもの
 */
public class BlockTrafficSign extends BlockContainer {
	
	
	public BlockTrafficSign() {
		super(Material.ROCK); // 固定で石で初期化
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "sign")); // Minecraft内部での登録名。どうやらUnlocalizedNameは削除されたみたい
		this.setTranslationKey("sign"); // 代わりにこれが使える
		this.setResistance(65535f); // 爆破耐性
		this.setHardness(1.0f); // 硬さ。
		this.setCreativeTab(GTS.gtsTab);
	}
	
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityTrafficSign(new NamedTrafficSign());
	}
	
	
	/**
	 * このブロックが透明かどうか。
	 * @param state
	 * @return false
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// 右クリックされたらGUI開く
		if (GTS.window != null) return false;
		if (worldIn.isRemote) return false;
		TileEntity te = worldIn.getTileEntity(pos); // あるはず
		GTS.window = new SwingGUITrafficSign(((TileEntityTrafficSign)(te)).getData()); // GUI開く
		playerIn.openGui(GTS.INSTANCE, 1, worldIn, playerIn.getPosition().getX(), playerIn.getPosition().getY(), playerIn.getPosition().getZ());
		GTS.window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// データをセットしてパケット送る
				((TileEntityTrafficSign)(te)).setData(((SwingGUITrafficSign) GTS.window).getData());
				((TileEntityTrafficSign)(te)).setTexChange(true);
				te.markDirty();
				
				worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 3);
				GTS.window = null; // ウィンドウ削除
			}
		});
		GTS.window.setVisible(true);
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		TileEntityTrafficSign te = (TileEntityTrafficSign) worldIn.getTileEntity(pos); // 必ずできると信じる
		if (te == null || !(placer instanceof EntityPlayer)) {
			// もし間に合わなかったら、もしくはプレイヤーじゃない別の何かによって置かれたら
			return;
		}
		// 角度情報を入れる
		EntityPlayer ep = (EntityPlayer) placer;
		te.setAngle(GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180)); // プレイヤーと逆向きに配置
		
		// モデルの変更があればアームの形状も変更する
		// ItemStackの状態を確認する
		ItemStack is = placer.getHeldItem(EnumHand.MAIN_HAND); // メインハンドに持っているアイテムを取得
		if (is.getItem() != ItemBlock.getItemFromBlock(GTS.Blocks.sign)) return; // 違う者の場合は無視（あり得ないけど）
		
		// ItemStackのモデル名を確認する
		// 保存されているデータを適用する
		if (is.getTagCompound() == null || !is.getTagCompound().hasKey("gts_sign_data")) {
			// タグがない場合は無視
			GTS.GTSLog.warn("Warning. sign data is null.");
			return;
		}
		
		TrafficSign ts;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(is.getTagCompound().getByteArray("gts_sign_data"))) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				ts = (TrafficSign) ois.readObject();
				
				// 追加できたらそれを代入する
				te.setData(ts);
				System.out.println(ts);
			}
		} catch (IOException | ClassNotFoundException e) {
			// 死んだとき
			e.printStackTrace();
		}
		
	}
}
