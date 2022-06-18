package jp.gingarenpo.gts.light;

import jp.gingarenpo.gts.GTS;
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
 * 交通信号機（灯器）を追加するためのブロック。
 * 車灯、歩灯ともに同じインスタンスを使用する。鉄道信号は未対応なのでRTMでどうにかして。
 */
public class BlockTrafficLight extends BlockContainer {
	
	/**
	 * 基本はこちらを使う。このブロックを初期化する。
	 * 基本的なパラメーターもこちらで勝手に指定する。後から変更は可能なはず。
	 */
	public BlockTrafficLight() {
		super(Material.ROCK); // 固定で石で初期化
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "light")); // Minecraft内部での登録名。どうやらUnlocalizedNameは削除されたみたい
		this.setTranslationKey("light"); // 代わりにこれが使える
		this.setResistance(65535f); // 爆破耐性
		this.setHardness(1.0f); // 硬さ。
		this.setCreativeTab(GTS.gtsTab);
	}
	
	/**
	 * 指定した材質を使ってこのブロックを初期化する。こっちはあまり使わない。
	 * @param materialIn 材質。
	 */
	protected BlockTrafficLight(Material materialIn) {
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
		TileEntityTrafficLight te = new TileEntityTrafficLight(true);
		return te;
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
		TileEntityTrafficLight te = (TileEntityTrafficLight) worldIn.getTileEntity(pos); // 必ずできると信じる
		if (te == null || !(placer instanceof EntityPlayer)) {
			// もし間に合わなかったら、もしくはプレイヤーじゃない別の何かによって置かれたら
			return;
		}
	}
}
