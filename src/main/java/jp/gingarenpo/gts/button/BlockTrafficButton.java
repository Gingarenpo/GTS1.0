package jp.gingarenpo.gts.button;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pole.gui.SwingGUITrafficPole;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * 押ボタン箱のインスタンス。ようは、触ると制御機に対して検知信号を
 * 送信することができるブロック。勿論既存のブロックによる描画は行わない。
 *
 *
 */
public class BlockTrafficButton extends BlockContainer {
	
	/**
	 * このブロックのインスタンスを作成する。
	 */
	public BlockTrafficButton() {
		super(Material.ROCK);
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "button")); // Minecraft内部での登録名。どうやらUnlocalizedNameは削除されたみたい
		this.setTranslationKey("button"); // 代わりにこれが使える
		this.setResistance(65535f); // 爆破耐性
		this.setHardness(1.0f); // 硬さ。正直サバイバルで制御機なんて使わないと思うので適当な大きさにしている。
		this.setCreativeTab(GTS.gtsTab);
	}
	
	/**
	 * このブロックのTileEntityを返す。
	 * @param worldIn 世界。
	 * @param meta
	 * @return
	 */
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityTrafficButton();
	}
	
	/**
	 * 非推奨扱いになっているがこれを指定しないと描画がおかしくなるため、オーバーライドする。
	 * このブロックが透明かどうか返す。trueにすると地面に穴が開くためfalseとする。
	 * @param state 現在のブロックの状態。
	 * @return 今回は描画をTESRに任せるため、false
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	/**
	 * このブロックが完全に立方体であるかどうかを返す。
	 * ただこれも非推奨となっており、isFullCubeという似たようなメソッドもあるため不明
	 * @param state
	 * @return
	 */
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	/**
	 * このブロックが右クリックされたときの動作。
	 * ボタンを押す。
	 * @param worldIn 世界。
	 * @param pos 座標。
	 * @param state ブロックの状態。
	 * @param playerIn クリックしたプレイヤー。
	 * @param hand クリックした手。
	 * @param facing クリックされた面の向き。
	 * @param hitX クリックされた場所を正規化したX座標（0-1）。
	 * @param hitY クリックされた場所を正規化したY座標（0-1）。
	 * @param hitZ クリックされた場所を正規化したZ座標（0-1）。
	 * @return falseなら何もイベントが起こらなかったことになる。
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// TileEntity探す
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof  TileEntityTrafficButton)) return false;
		TileEntityTrafficButton tetb = (TileEntityTrafficButton) te;
		
		// TileEntity内のデータをPushにする（既になっていたら無視）
		if (tetb.getButton().isPushed()) return false;
		tetb.getButton().push();
		
		// ついでに音声も再生する
		if (worldIn.isRemote) {
			Minecraft.getMinecraft().getSoundHandler().playSound(new SoundTrafficButton(tetb));
		}
		
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntityTrafficButton te = (TileEntityTrafficButton) worldIn.getTileEntity(pos); // 必ずできると信じる
		if (te == null || !(placer instanceof EntityPlayer)) {
			// もし間に合わなかったら、もしくはプレイヤーじゃない別の何かによって置かれたら
			return;
		}
		// 角度情報を入れる
		EntityPlayer ep = (EntityPlayer) placer;
		// System.out.println(GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180));
		te.setAngle(GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180)); // プレイヤーと逆向きに配置
		
		// モデルの変更があればアームの形状も変更する
		// ItemStackの状態を確認する
		if (!(placer instanceof EntityPlayer)) return;
		ItemStack is = placer.getHeldItem(EnumHand.MAIN_HAND); // メインハンドに持っているアイテムを取得
		if (is.getItem() != ItemBlock.getItemFromBlock(GTS.Blocks.button)) return; // 違う者の場合は無視（あり得ないけど）
		
		// ItemStackのモデル名を確認する
		NBTTagCompound c = is.getTagCompound();
		if (c == null) return; // 無視
		String name = c.getString("gts_item_model_pole"); // NBTから取得したパック名
		ModelBase model = SwingGUITrafficPole.getModelFromChoiceName(name);
		if (model == null) {
			GTS.GTSLog.warn("ItemStack declare default model as " + name + ", but it is not found. dummy used.");
			return;
		}
		te.setAddon((ModelTrafficButton) model); // 入れる
	}
}
