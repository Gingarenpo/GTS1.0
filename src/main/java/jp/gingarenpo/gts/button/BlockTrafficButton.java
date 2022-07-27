package jp.gingarenpo.gts.button;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gts.GTS;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
	 * 押ボタン箱のモデルを変更するためのSwingパネルを開く。
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
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
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
	}
}
