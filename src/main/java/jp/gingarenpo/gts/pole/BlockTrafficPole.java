package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.arm.ItemTrafficArm;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
	 * このブロックが右クリックされたときに呼び出されるメソッド。
	 * @param worldIn 世界。
	 * @param pos 場所。
	 * @param state 状態。
	 * @param playerIn 右クリックしたプレイヤー。
	 * @param hand どっちの手だったか。
	 * @param facing 右クリックされた面の向き。
	 * @param hitX 0-1の間で座標
	 * @param hitY 0-1の間で座標
	 * @param hitZ 0-1の間で座標
	 * @return trueを返すとイベント処理
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack is = playerIn.getHeldItemMainhand(); // 持っているアイテムを取得
		if (!(is.getItem() instanceof ItemTrafficArm)) return false; // アイテムがアームじゃなかったら無視
		
		TileEntityTrafficPole te = (TileEntityTrafficPole) worldIn.getTileEntity(pos); // 絶対にあるはず
		
		if (te.isPreConnect()) {
			// 既に接続中だった場合。ポールからポールには接続不可能なので処理を終了する
			GTS.GTSLog.info("Can't joint arm from pole to pole.");
			te.endConnect();
			NBTTagCompound ist = is.getTagCompound(); // 持っているItemStackに情報を追加する
			if (ist == null) {
				// 存在しない場合は新たに作成する
				ist = new NBTTagCompound();
				is.setTagCompound(ist);
			}
			ist.setIntArray("gts_pre_connect_xyz", new int[1]); // 1個しか要素がない状態は無接続とする
			ist.setBoolean("gts_pre_connect", false);
			return true;
		}
		
		// 接続されていない場合、接続を開始する
		te.startConnect();
		NBTTagCompound ist = is.getTagCompound(); // 持っているItemStackに情報を追加する
		if (ist == null) {
			// 存在しない場合は新たに作成する
			ist = new NBTTagCompound();
			is.setTagCompound(ist);
		}
		ist.setBoolean("gts_pre_connect", true);
		ist.setIntArray("gts_pre_connect_xyz", new int[] {pos.getX(), pos.getY(), pos.getZ()}); // 座標を読みださないと
		return true;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityTrafficPole self = (TileEntityTrafficPole) worldIn.getTileEntity(pos);
		if (self == null) return;
		checkStatus(worldIn, self, pos);
	}
	
	private void checkStatus(World world, TileEntityTrafficPole self, BlockPos pos) {
		if (world.isRemote) return;
		TileEntity top = world.getTileEntity(pos.up()); // 上部を取得
		TileEntity down = world.getTileEntity(pos.down()); // 下部を取得
		System.out.println(top);
		System.out.println(down);
		if (top == null && down == null) self.setBottom(true); // 単独の場合は下部扱いとする
		else if (top instanceof TileEntityTrafficPole && !(down instanceof TileEntityTrafficPole)) {
			// 上につながっているだけなので下部扱いとする
			self.setBottom(true);
		}
		else if (down instanceof TileEntityTrafficPole && !(top instanceof TileEntityTrafficPole)) {
			// 下につながっているだけなので上部扱いとする
			self.setTop(true);
		}
		else {
			// 中間扱い
			self.setTop(false);
			self.setBottom(false);
		}
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
	}
 }
