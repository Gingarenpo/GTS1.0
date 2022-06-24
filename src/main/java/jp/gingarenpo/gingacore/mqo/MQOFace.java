package jp.gingarenpo.gingacore.mqo;

import jp.gingarenpo.gingacore.helper.GMathHelper;
import jp.gingarenpo.gingacore.helper.GPosHelper;
import jp.gingarenpo.gingacore.helper.GRenderHelper;
import net.minecraft.client.renderer.BufferBuilder;
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
	
	private final MQOObject mqo; // 親オブジェクト
	private final int[] v; // 頂点番号を格納（固定なのでプリミティブ配列で）
	private final ArrayList<double[]> uv = new ArrayList<double[]>(); // 頂点対応のUV座標を格納

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
	}


	/**
	 * この面を描画するためのBufferを作成します。直接これを呼び出すのはやめて、Objectから呼び出してください。
	 *　内部ではbeginもdrawも行いません。
	 *
	 * @param color バッファカラーを指定します。0を指定すると自動で陰影をつけますが、真っ黒にしたい場合は妥協して0.0000001とかで。
	 */
	public void drawFace(BufferBuilder b, float color) {
		if (v.length == 4) {
			// 四角形の場合は二回呼び出す（1,2,3 - 3,4,1）
			set3Vertex(b, color, new MQOVertex[] {mqo.getVertexs().get(v[0]), mqo.getVertexs().get(v[1]), mqo.getVertexs().get(v[2])}, new double[][] {uv.get(0), uv.get(1), uv.get(2)});
			set3Vertex(b, color, new MQOVertex[] {mqo.getVertexs().get(v[0]), mqo.getVertexs().get(v[2]), mqo.getVertexs().get(v[3])}, new double[][] {uv.get(0), uv.get(2), uv.get(3)});
		}
		else {
			// 三角形の場合はそのまま呼び出す
			set3Vertex(b, color, new MQOVertex[] {mqo.getVertexs().get(v[0]), mqo.getVertexs().get(v[1]), mqo.getVertexs().get(v[2])}, new double[][] {uv.get(0), uv.get(1), uv.get(2)});
		}
	}
	
	/**
	 * ※3つの頂点であるかどうかのチェックはしないので注意
	 * @param b バッファ
	 * @param color 色
	 * @param vertex 頂点
	 */
	private void set3Vertex(BufferBuilder b, float color, MQOVertex[] vertex, double[][] uv) {
		for (int i = vertex.length - 1; i >= 0; i--) {
			b.pos(vertex[i].getX(),
					vertex[i].getY(), vertex[i].getZ())
					.tex(uv[i][0], uv[i][1]);
			
			if (color != 0.0f) b.color(color, color, color, 1.0f);
			else {
				// 自動計算を行う
				float c = (float) getShadowColor();
				b.color(c, c, c, 1.0f);
			}
			
			b.endVertex();
		}
	}
	
	private double getShadowColor() {
		// 4つ以上頂点がある場合はその中から3つ選ぶ。どれ選んでも同じなので前から3つ
		MQOVertex[] vs = new MQOVertex[3];
		for (int i = 0; i < 3; i++) {
			vs[i] = mqo.getVertexs().get(v[i]);
		}
		
		// 単位ベクトルの大きさを返しそのy座標を取得する
		double y = GMathHelper.getDefaultAreaDirection(vs[0].getX(), vs[0].getY(), vs[0].getZ(), vs[1].getX(), vs[1].getY(), vs[1].getZ(), vs[2].getX(), vs[2].getY(), vs[2].getZ())[1];
		
		// Version1の時点ではそこまで凝らない予定なので割愛
		// Version1.1とかで、太陽の向きに即した角度で影を反映させたい
		
		// いくら直射でも全発光はしないはずなのと、影であろうとも反射光が入るはずなので取りえる色の乗算値を0.4～1.0とする
		// まず-1～1になっているのを0～1に是正する
		y = (y+1) / 2; // これで補正される
		y = y * 0.6;
		y = 0.6 - y;
		return y + 0.4;
	}
	

	
}
