package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * 交通信号機の設置に使われるポールのブロック。基本的に1ブロックにつき1つのTileEntityを配置するが
 * 重くなったりするので将来的には可変の長さをできるようにしたいところ。
 *
 * ポール固有の設定とかもあるので注意！
 * RTMはポールとアームが一体化していたがこちらはブロック自体を完全に分ける
 */
public class BlockTrafficPole extends BlockContainer {
	
	/**
	 * 基本はこちらを使う。このブロックを初期化する。
	 * 基本的なパラメーターもこちらで勝手に指定する。後から変更は可能なはず。
	 */
	public BlockTrafficPole() {
		super(Material.ROCK); // 固定で石で初期化
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "pole")); // Minecraft内部での登録名。どうやらUnlocalizedNameは削除されたみたい
		this.setTranslationKey("pole"); // 代わりにこれが使える
		this.setResistance(65535f); // 爆破耐性
		this.setHardness(1.0f); // 硬さ。
		this.setCreativeTab(GTS.gtsTab);
	}
	
	/**
	 * 指定した材質を使ってこのブロックを初期化する。こっちはあまり使わない。
	 * @param materialIn 材質。
	 */
	protected BlockTrafficPole(Material materialIn) {
		super(materialIn);
	}
	
	/**
	 * このブロックが完全な立方体かどうか。
	 * @param state
	 * @return false
	 */
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
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
	
	/**
	 * このブロックに対応するTileEntityを返す。
	 * @param worldIn 世界。
	 * @param meta ？
	 * @return
	 */
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityTrafficPole();
	}
	
	/**
	 * このブロックの当たり判定の範囲を返す。今回当たり判定あると邪魔なのでとりあえず消した。
	 * @param blockState
	 * @param worldIn
	 * @param pos
	 * @return
	 */
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	/**
	 * このブロックが置かれた際に呼び出される。
	 * 検証結果、どうやらTileEntityが作成されてから呼び出されるようなので安全？
	 * @param worldIn
	 * @param pos
	 * @param state
	 * @param placer
	 * @param stack
	 */
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// 上下のブロックを見てこのブロックの状態を設定する
		TileEntityTrafficPole self = (TileEntityTrafficPole) worldIn.getTileEntity(pos); // 絶対にあるはず
		if (self == null) return;
		checkStatus(worldIn, self, pos);
		
	}
	
	/**
	 * 近くの（隣接する）ブロックが更新されたときに呼び出される。
	 * @param world
	 * @param pos
	 * @param neighbor
	 */
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileEntityTrafficPole self = (TileEntityTrafficPole) world.getTileEntity(pos);
		if (self == null) return;
		checkStatus((World) world, self, pos);
		
	}
	
	private void checkStatus(World world, TileEntityTrafficPole self, BlockPos pos) {
		TileEntity top = world.getTileEntity(pos.up()); // 上部を取得
		TileEntity down = world.getTileEntity(pos.down()); // 下部を取得
		if (top == null && down == null) self.setBottom(true); // 単独の場合は下部扱いとする
		if (top instanceof TileEntityTrafficPole && !(down instanceof  TileEntityTrafficPole)) {
			// 上につながっているだけなので下部扱いとする
			self.setBottom(true);
		}
		if (down instanceof TileEntityTrafficPole && !(top instanceof  TileEntityTrafficPole)) {
			// 下につながっているだけなので上部扱いとする
			self.setTop(true);
		}
	}
 }
