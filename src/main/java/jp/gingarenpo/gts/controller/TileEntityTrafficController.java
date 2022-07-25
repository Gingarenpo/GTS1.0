package jp.gingarenpo.gts.controller;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * 交通信号制御機に関するTileEntity。制御機は送信側となるため、受信時の処理は基本的にない。
 * ただし、感知しているかどうかに関する受信のみは行う。
 */
public class TileEntityTrafficController extends GTSTileEntity implements ITickable {
	
	// 定数
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;

	private TrafficController data; // この制御機の状態を表すデータ。これをパケットで送信する
	private BufferedImage texture = null; // この制御機のテクスチャ。1制御機に対して1つもつようになるためメモリバカ食いするけど。
	private ResourceLocation textureLocation = null; // 制御機のテクスチャを擬似的にリソースとして扱うためのもの
	public static MQO model; // モデル（現在は固定で…）
	
	
	public TileEntityTrafficController() {
		this.data = new TrafficController();
		this.texture = createTexture();
		if (model == null) {
			// モデルが用意できていない場合
			loadModel();
		}
	}
	
	/**
	 * このTileEntityの初期化が終わりワールドにロードされるときに呼び出される。
	 */
	@Override
	public void onLoad() {
		search();
	}
	
	/**
	 * 制御機の周りの信号機を再検出し、それをアタッチしてリストに格納する。
	 * 非常に重い処理の為、何度も呼び出さないこと。何かをトリガーにして発動させる。
	 */
	public void search() {
		// デフォでコンフィグに入ったrangeの範囲の制御機を読み込んでリストに格納する
		// ごり押しだけど仕方がない
		ArrayList<TileEntityTrafficLight> list = new ArrayList<TileEntityTrafficLight>();
		for (int x = this.pos.getX() - GTS.GTSConfig.detectRange; x < this.pos.getX() + GTS.GTSConfig.detectRange; x++) {
			for (int y = this.pos.getY() - GTS.GTSConfig.detectRange; y < this.pos.getY() + GTS.GTSConfig.detectRange; y++) {
				for (int z = this.pos.getZ() - GTS.GTSConfig.detectRange; z < this.pos.getZ() + GTS.GTSConfig.detectRange; z++) {
					TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); // TileEntityを取得
					if (!(te instanceof TileEntityTrafficLight)) continue;
					list.add((TileEntityTrafficLight) te);
				}
			}
		}
		this.getData().setTrafficLights(list); // 見つかったリストに置き換え
	}
	
	/**
	 * モデルが読み込まれていない場合このメソッドで読み込む
	 */
	public static void loadModel() {
		// 作成する
		try {
			model = new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/default_traffic_controller.mqo")).getInputStream());
			model = model.normalize(2); // 正規化
		} catch (IOException e) {
			// 何らかの影響でMQOが呼べなかった場合
			e.printStackTrace();
		}
	}
	
	/**
	 * ITickableインターフェースで呼び出される、1Tick毎に実行する内容を記載している。
	 * 1Tick毎に呼び出される仕様のため、ここで重い処理を行わせるのは避ける。
	 *
	 * 1Tick毎に監視し、制御機のデータを切り替えている。また、検知信号の受信を受け付ける（正直これは感知器の方がやってくれるけど）
	 */
	@Override
	public void update() {
		if (this.world.isRemote) return; // クライアント側では実行しない
		boolean cycleChange = data.checkCycle(this.world); // 制御機のデータを更新する
		this.markDirty();
		if (!cycleChange) {
			// world.notifyBlockUpdate(this.pos, world.getBlockState(pos), world.getBlockState(pos), 2); // なんかなくても動く
			this.search(); // サイクル変わったら再読み込みする
		}
	}
	
	/**
	 * テクスチャを作成してBufferedImageを返す。実際に描画（ベイク）するときは、これをDynamicTextureに渡して登録させることになる。
	 * @return
	 */
	public BufferedImage createTexture() {
		// まず作成しなきゃ
		BufferedImage tex = new BufferedImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR); // メモリバカ食いするので256px四方とする
		Graphics2D g = tex.createGraphics(); // 描画用のコンテキストを呼び出す
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // テキストアンチエイリアス
		Color c = data.getColor(); // 色を制御機のベースカラーに指定する
		g.setColor(c); // この色で塗りつぶす
		g.fillRect(0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT); // 実際の塗りつぶし
		
		// 管理番号を記す
		// 描画終わり。返す
		return tex;
	}
	
	/**
	 * ダイレクトにテクスチャを作成するためのメソッド。テクスチャ作成と同時に自分のインスタンスのテクスチャに代入する。
	 * @param direct
	 * @return
	 */
	public BufferedImage createTexture(boolean direct) {
		BufferedImage b = createTexture();
		if (direct) {
			this.texture = b;
			this.textureLocation = null;
		}
		return b;
	}
	

	
	/**
	 * この制御機の内部情報を返す。
	 * @return 制御機情報。
	 */
	public TrafficController getData() {
		return data;
	}
	
	/**
	 * 制御機の内部情報をセットする。
	 * @param data 制御機情報
	 */
	public void setData(TrafficController data) {
		this.data = data;
	}
	
	/**
	 * この制御機をレンダリングするのに必要なレンダリングリソースロケーションを返す。
	 * @return bindTexture等で使用できるもの。nullかもしれないので注意。
	 */
	public ResourceLocation getTextureLocation() {
		return textureLocation;
	}
	
	/**
	 * この制御機のリソースロケーションを返す。
	 * @param textureLocation ロケーション
	 */
	public void setTextureLocation(ResourceLocation textureLocation) {
		this.textureLocation = textureLocation;
	}
	
	/**
	 * レンダリングできる準備が整っているかどうか。つまりテクスチャを既にバインドして用意できているかどうか。
	 * @return bindTextureの際にnullにならない場合はtrue
	 */
	public boolean isreadyRender() {
		return textureLocation != null;
	}
	
	/**
	 * テクスチャを返す。
	 * @return テクスチャ
	 */
	public BufferedImage getTexture() {
		return texture;
	}
	
	/**
	 * NBTタグとして記録されたデータを読み込むために必要なメソッド。定期的にこのデータが呼び出される。
	 * @param compound NBT
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		// 制御機の内容を読み込む
		GTS.GTSLog.log(Level.INFO, "Read NBT. " + this);
		byte[] nbtData = compound.getByteArray("gts_tc_data"); // 万が一存在しない場合サイズ0の配列が返ってくる
		try (ByteArrayInputStream bais = new ByteArrayInputStream(nbtData)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				TrafficController read = (TrafficController) ois.readObject(); // Object型になっているがこの中に含まれるのはデータインスタンスかnullのはず
				this.data = read; // 仕方ない
				createTexture(true);
			}
		} catch (IOException | ClassNotFoundException e) {
			// ByteArrayInputStreamが何らかの影響で開けなかった場合、もしくは展開に失敗した場合
			GTS.GTSLog.log(Level.ERROR, "Can't load data object(Maybe out of memory or data == null) -> " + e.getMessage());
		}
	}
	
	/**
	 * NBTタグにデータを書き込んで、TileEntityの中身を一時的に保存する。
	 * ログイン中は維持しているがパケット通信を行わないとこの内容は虚空の彼方へ消えてしまう。
	 *
	 * @param compound NBT
	 * @return
	 */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound c = super.writeToNBT(compound); // まずはデフォルトに任せる
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(data); // 制御機のデータを書き込む
				c.setByteArray("gts_tc_data", baos.toByteArray()); // シリアライズ可能にしてあるはずなので
			}
		} catch (IOException e) {
			// 何らかの影響でメモリの確保などに失敗した場合
			GTS.GTSLog.log(Level.ERROR, "Can't write data object(Maybe out of memory) -> " + e.getMessage());
		}
		return c;
	}
	
	/**
	 * サーバーからクライアントに同期させるために必要な更新パケットを取得する。
	 * ここで返すのは、NBTタグを内包したパケットクラスのオブジェクトとなる。独自拡張は可能（継承さえしていれば）。
	 * 今回はバニラにあらかじめ設定されているTileEntity送信用のパケットクラスを使用する。
	 *
	 * @return
	 */
	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, -1, this.getUpdateTag()); // パケットを送信する
	}
	
	/**
	 * サーバー側で何をするかってところだと思うが正確な説明はできない。
	 * おそらく、パケットを送信する準備を行う際に、ここでパケットのnbtタグを準備しろってことだろう。
	 * つまり、このメソッドが呼び出された時点でのNBTタグを送信することになるので、NBTを更新するべき！
	 * @return
	 */
	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound()); // 更新情報を通知するためのNBTタグを新規に作成し、今の状況を書き込み返す
	}
	
	/**
	 * パケットが受信されてきた際に行う処理。netが何を意味しているのかは不明だが、パケットの中にNBTタグが入っているためそれを読み出す。
	 * @param net チャンネルに関するあれこれを取得できるらしい（外部通信との際に必要？）
	 * @param pkt パケット
	 */
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		// super.onDataPacket(net, pkt); // 中身何もしていないのでいらない
		this.readFromNBT(pkt.getNbtCompound()); // パケットからNBTタグを取り出して、読み込ませる
	}
	
	public void sendPacket() {
		// クライアント側から全方向に向けてパケット発射（クライアント→パケット）
		if (this.world == null) return; // Nullの場合は発信できない
		if (!this.world.isRemote) {
			this.createTexture();
			GTS.MOD_NETWORK.sendToAll(new PacketTrafficController(this)); // 強制送信
		}
	}
	
	/**
	 * このTileEntityの文字列表現を返す。
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TextureLocation is " + (textureLocation == null ? "" : "not ") + "null. ");
		sb.append("BufferedTexture is " + (texture == null ? "" : "not ") + "null. ");
		sb.append("Traffic Controller Data: " + (data == null ? "!!NULL!!" : data.toString()));
		return sb.toString();
	}
}
