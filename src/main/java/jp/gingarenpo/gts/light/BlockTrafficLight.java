package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.arm.ItemTrafficArm;
import jp.gingarenpo.gts.arm.TrafficArm;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
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
		TileEntityTrafficLight te = new TileEntityTrafficLight();
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
		// 角度情報を入れる
		EntityPlayer ep = (EntityPlayer) placer;
		System.out.println(GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180));
		te.setAngle(GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180)); // プレイヤーと逆向きに配置
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
		
		TileEntityTrafficLight te = (TileEntityTrafficLight) worldIn.getTileEntity(pos); // 絶対にあるはず
		NBTTagCompound ist = is.getTagCompound(); // 持っているItemStackの情報を取得する
		if (ist == null) return false;
		if (!ist.hasKey("gts_pre_connect") || !ist.getBoolean("gts_pre_connect")) return false;
		
		// アームを接続する
		int[] xyz = ist.getIntArray("gts_pre_connect_xyz");
		if (xyz.length != 3) {
			GTS.GTSLog.warn("Can't joint Traffic arm because illegal data.");
			return true;
		}
		
		TileEntity from = worldIn.getTileEntity(new BlockPos(xyz[0], xyz[1], xyz[2])); // 接続元のポールを取得
		if (!(from instanceof TileEntityTrafficPole)) {
			GTS.GTSLog.warn("Can't joint Traffic arm because Traffic pole is missing.");
			return true;
		}
		
		TrafficArm a = ((TileEntityTrafficPole) from).getArm();
		if (a == null) a = new TrafficArm();
		if (a.getTo().contains(te)) {
			// 既に接続済み
			GTS.GTSLog.warn("Already Connected.");
			return true;
		}
		
		// ようやく接続できる
		a.add(new double[] { pos.getX(), pos.getY(), pos.getZ() });
		((TileEntityTrafficPole) from).endConnect();
		((TileEntityTrafficPole) from).setArm(a); // アームを追加
		ist.setIntArray("gts_pre_connect_xyz", new int[1]); // 1個しか要素がない状態は無接続とする
		ist.setBoolean("gts_pre_connect", false);
		return true;
	}
}
