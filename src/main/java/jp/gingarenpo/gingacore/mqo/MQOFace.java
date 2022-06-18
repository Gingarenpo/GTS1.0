package jp.gingarenpo.gingacore.mqo;

import jp.gingarenpo.gingacore.helper.GPosHelper;
import jp.gingarenpo.gingacore.helper.GRenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.io.Serializable;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

/**
 * MQOの面を格納するクラスです。この中にはさらに頂点も格納しているため、このクラスからOpenGL描画を行うこともできます
 *
 * @author 銀河連邦
 */
public class MQOFace implements Serializable {

	/**
	 * この面の向きを表す定数で、上面を表します（すなわち、全頂点のY座標が一致します）。
	 * @deprecated 廃止予定
	 */
	public static final int FACING_TOP = 1;
	/**
	 * この面の向きを表す定数で、南北方向を向いていることを表します（すなわち、全頂点のZ座標が固定されています）。
	 * @deprecated 廃止予定
	 */
	public static final int FACING_SIDE_NS = 2;

	/**
	 * この面の向きを表す定数で、東西方向を向いていることを表します（すなわち、全頂点のX座標が一致します）。
	 * @deprecated 廃止予定
	 */
	public static final int FACING_SIDE_EW = 4;

	/**
	 * この面はXYZいずれの軸にも平行でない面であることを表します。
	 * @deprecated 廃止予定
	 */
	public static final int FACING_NO = 0;

	private final MQOObject mqo; // 親オブジェクト
	private final int[] v; // 頂点番号を格納（固定なのでプリミティブ配列で）
	private final ArrayList<double[]> uv = new ArrayList<double[]>(); // 頂点対応のUV座標を格納
	private int facing; // この面が向いている向き。
	private float cos; // 地面との成すcos

	/**
	 * 指定した親オブジェクト内に存在する面として、新規に面オブジェクトを作成します。面オブジェクトは三角形か四角形
	 * じゃないとめんどくさいことになります。チェックしないので自力で確認してください。
	 *
	 * @param mqo 親元となるMQOオブジェクト。
	 * @param vnum 頂点が記されている対応番号を記したもの。1 3 5とかそんな感じ
	 * @param uvnum それぞれの頂点に対応したUVを記録する。必ずvnumの倍の引数を持つ
	 */
	public MQOFace(MQOObject mqo, String vnum, String uvnum) {
		this.mqo = mqo; // 親オブジェクトを代入
		final String[] vs = vnum.split(" "); // 空白で
		final String[] uvs = uvnum.split(" "); // これも
		String vv = null;
		for (final String element : vs) {
			vv = vv + element + " ";
		}
		// System.out.println("obj("+mqo.getName()+") : v["+vs.length+"] uv["+uvs.length+"], v=["+vv+"]");

		if (vs.length * 2 != uvs.length) // 座標数が一致しない
			throw mqo.getParent().new MQOException("Illegal UV or Vertex parameter!!");

		// X,Y,Zそれぞれを代入する
		final double[] vX = new double[vs.length];
		final double[] vY = new double[vs.length];
		final double[] vZ = new double[vs.length]; // それぞれ座標を格納するもの
		v = new int[vs.length]; // 指定した数で初期化
		for (int i = 0; i < vs.length; i++) {
			v[i] = Integer.parseInt(vs[i]); // 頂点番号を代入
			uv.add(i, new double[] {Double.parseDouble(uvs[i*2]), Double.parseDouble(uvs[i*2+1])}); // UVをね
			vX[i] = mqo.getVertexs().get(v[i]).getX();
			vY[i] = mqo.getVertexs().get(v[i]).getY();
			vZ[i] = mqo.getVertexs().get(v[i]).getZ();
		}
		
		// cosを求める(影に使う)
		cos = (float) GPosHelper.getAngle(vX[0], vY[0], vZ[0], vX[1], vY[1], vZ[1], vX[2], vY[2], vZ[2]);

		// この面がどの方向を向いているのかを検証する
		if (GPosHelper.areSameNumber(vX)) {
			// X座標がすべて一致している
			facing = FACING_SIDE_EW;
		}
		else if (GPosHelper.areSameNumber(vY)) {
			// Y座標がすべて一致している
			facing = FACING_TOP;
		}
		else if (GPosHelper.areSameNumber(vZ)) {
			// Z座標がすべて一致している
			facing = FACING_SIDE_NS;
		}
	}


	/**
	 * この面を実際に描画します。直接OpenGLを使用するので、レンダーメソッドで呼び出す必要があります。そうでない場合の
	 * チェックは一切しないのでご了承ください。
	 *
	 *
	 * @param color バッファカラーを指定します。0を指定すると自動で陰影をつけますが、真っ黒にしたい場合は妥協して0.0000001とかで。
	 */
	public void drawFace(double color) {
		// Tessellatorを試してみる
		final Tessellator t = Tessellator.getInstance(); // インスタンスを取得

		// Tessellatorで描画するときは面の向きが逆になる？
		t.getBuffer().begin((v.length == 3) ? GL11.GL_TRIANGLES : GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		for (int i = v.length - 1; i >= 0; i--) {
			t.getBuffer().pos(mqo.getVertexs().get(v[i]).getX(),
					mqo.getVertexs().get(v[i]).getY(), mqo.getVertexs().get(v[i]).getZ())
			.tex(uv.get(i)[0], uv.get(i)[1]);
			
			// float変換
			float color2 = (float) color;
			
			if (color != 0) t.getBuffer().color(color2, color2, color2, 1.0f);
			else if (getDirection() == EnumFacing.UP || getDirection() == EnumFacing.DOWN) t.getBuffer().color(1.0f, 1.0f, 1.0f, 1.0f);
			else if (getDirection() == EnumFacing.EAST || getDirection() == EnumFacing.WEST) t.getBuffer().color(0.8f, 0.8f, 0.8f, 1.0f);
			else if (getDirection() == EnumFacing.SOUTH || getDirection() == EnumFacing.NORTH) t.getBuffer().color(0.5f, 0.5f, 0.5f, 1.0f);
			else t.getBuffer().color(1.0f, 1.0f, 1.0f, 1.0f);
			
			
			
			t.getBuffer().endVertex();
		}
		t.draw();

		// GL11.glEnd(); // 終了
	}
	
	/**
	 * この面が南北東西上下のどちらに向いているかを返しますが、曲面の場合は近似されてしまうため正常に表現できない場合があります。
	 *
	 * @return
	 */
	public EnumFacing getDirection() {
		// 面の向きを確かめる
		//System.out.println(Math.acos(cos));
		return EnumFacing.fromAngle(Math.toDegrees(Math.acos(cos)));
	}

	
}
