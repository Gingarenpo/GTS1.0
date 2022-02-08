package jp.gingarenpo.gts.controller;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.swing.GUITrafficController;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * 制御機を設置することができるブロックを追加する。あくまで設置できるというだけであり、制御機自体の動作は
 * TileEntityが担当する。このインスタンスは使いまわされるため実質staticと同等の扱いになり、
 * どうしてもここ自体にデータを保持させておくことができない。
 *
 * いくつかOverrideする引数があるが、わかっているものに関して勝手にそのパラメーターの内容を残す（備忘録）。
 * 間違っている可能性もあるので鵜呑みにはしないでほしい。
 */
public class BlockTrafficController extends BlockContainer {
	
	public static PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL); // むき
	
	/**
	 * 基本はこちらを使う。このブロックを初期化する。
	 * 基本的なパラメーターもこちらで勝手に指定する。後から変更は可能なはず。
	 */
	public BlockTrafficController() {
		super(Material.ROCK); // 固定で石で初期化
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "control")); // Minecraft内部での登録名。どうやらUnlocalizedNameは削除されたみたい
		this.setTranslationKey("control"); // 代わりにこれが使える
		this.setResistance(65535f); // 爆破耐性
		this.setHardness(1.0f); // 硬さ。正直サバイバルで制御機なんて使わないと思うので適当な大きさにしている。
		this.setCreativeTab(GTS.gtsTab);
	}
	
	/**
	 * 指定した材質を使ってこのブロックを初期化する。こっちはあまり使わない。
	 * @param materialIn 材質。
	 */
	protected BlockTrafficController(Material materialIn) {
		super(materialIn);
	}
	
	/**
	 * 新しいTileEntityを作る際に呼び出される。メタデータはよく分かっていないが、World（地上、ネザー、エンド）により
	 * 分岐をさせることも可能。今回はネザーでも一応設置自体はできるようにするが、ネザーに設置するようなユーザーが
	 * 果たしているのかどうかすら疑問である。nullを返すとめんどくさいことになるのでとりあえず新しいTileEntityを必ず返す。
	 * @param worldIn 設置されたワールド。
	 * @param meta 不明
	 * @return TileEntity
	 */
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntityTrafficController te = new TileEntityTrafficController();
		return te;
	}
	
	/**
	 * このブロックの上に松明を置くことができるかを表す。今回制御機は独立しており、その上に松明があると不自然極まりないので
	 * 問答無用でfalseとする。
	 * @param state 現在のブロックの状態が渡される。
	 * @param world このブロックが置かれているワールドが渡される。
	 * @param pos ブロックの置かれている座標が渡される。
	 * @return 今回は問答無用でfalseを返す。
	 */
	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
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
	 * これも非推奨になっているが当たり判定を指定することができる。IBlockStateに全部放り投げることにしたのだろうか。
	 * @param state このブロックの状態
	 * @param source ？
	 * @param pos ブロックが置かれている座標。
	 * @return 当たり判定として有効な範囲。
	 */
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0, 0, 0, 1, 2, 1); // 縦2ブロック
	}
	
	/**
	 * BlockState（現在のブロックの状態を保持する）から16方向で保持できるメタデータに変換する。
	 * これを入れておかないとめんどくさいことになる。
	 * @param state 状態。
	 * @return それに対応するメタデータ（最大16）
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex(); // むき
	}
	
	/**
	 * ブロックの状態を維持するコンテナを作成する。
	 * @return プロパティ
	 */
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING); // このプロパティを入れたものを返す
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
	 * このブロックを右クリック（開発者は逆にしているため違和感だけど要は壊さずブロックに対してタッチしたとき）の動作を指定する。
	 * ここではGUIが立ちあがる。引数が多い。
	 * @param worldIn 世界。
	 * @param pos 座標。
	 * @param state ブロックの状態。
	 * @param playerIn クリックしたユーザー
	 * @param hand クリックした手
	 * @param facing クリックされたときの向き（ブロックの？）
	 * @param hitX ブロックのどの辺をクリックされたか。0-1
	 * @param hitY Y
	 * @param hitZ Z
	 * @return これをfalseにするとイベントなし
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// GUIを表示する（現在はSwing）
		TileEntity te = worldIn.getTileEntity(pos); // 通常はあるはずなんだけど
		if (te == null) {
			playerIn.sendMessage(new TextComponentString("Something went to wrong. TileEntity is not found in this world."));
			return false;
		}
		TileEntityTrafficController tetc = (TileEntityTrafficController) te; // 絶対そうに決まっていると信じたい
		GUITrafficController gui = new GUITrafficController(GTS.window);
		gui.update(tetc); // 設定
		gui.setPlayer(playerIn); // プレイヤーセット
		GTS.window.getContentPane().removeAll(); // 一旦全部取り外す
		GTS.window.setVisible(true);
		GTS.window.getContentPane().add(gui); // コンポーネントを追加する
		if (GTS.window.getWindowListeners().length > 0) {
			GTS.window.removeWindowListener(GTS.window.getWindowListeners()[0]); // リスナーがある場合は削除
		}
		GTS.window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				gui.onClose();
			}
		}); // 閉じられたときに
		GTS.window.setTitle("GTS Traffic Controller Manager");
		gui.updateGUI(); // GUI値を更新
		// ポーズ対策として空のGUIも開く
		playerIn.openGui(GTS.INSTANCE, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
}
