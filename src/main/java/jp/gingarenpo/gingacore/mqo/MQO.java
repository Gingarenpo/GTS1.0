package jp.gingarenpo.gingacore.mqo;

import jp.gingarenpo.gingacore.annotation.NeedlessMinecraft;
import jp.gingarenpo.gingacore.helper.GMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * このクラスは、MQOオブジェクトを作成します。このオブジェクト自体は基本的に使わず、サブオブジェクトを頻繁に使う
 * ことになるでしょう。<strike>ちなみに、オブジェクトによる分別は今のところ考えていません。</strike>考えざるを
 * えなくなってしまいました。
 *
 * @author 銀河連邦
 */
public class MQO implements Serializable, Cloneable {

	/**
	 * オブジェクトの名前をキーとして格納しています
	 */
	private HashMap<String, MQOObject> object = new HashMap<>();
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * クローン用に用意しているだけであり意味がありません
	 */
	private MQO() {
	
	}


	/**
	 * モデルがある場所を指定することで、そのモデルを読み込んだ新しいMQOオブジェクトを作成します。
	 *
	 * @param r モデルがある場所のリソースロケーション。
	 * @throws IOException 存在しなかった時。
	 */
	public MQO(ResourceLocation r) throws IOException {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			throw new RuntimeException("Server side startup!!!");
		}
		// getMinecraft()が使用できない環境が存在するらしい
		parse(Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream());
	}

	/**
	 * Minecraftの機能に依存せずにモデルを扱いたい場合に使用します。そのモデルを読み込んだ新しいMQOオブジェクトを作成します。
	 * こちらはFileオブジェクトから指定します。通常はこちらを使用してください。以下に記してあるStringによる指定は、非推奨です。
	 * リソースは自動的に閉じるようにしてあります。
	 *
	 * @param file ファイルオブジェクト。
	 * @throws IOException 存在しなかった時。
	 */
	@NeedlessMinecraft
	public MQO(File file) throws IOException {
		// ファイルの中身から単純に生成する。
		if (!file.exists()) throw new IOException("File is not found.");
		if (!file.getName().endsWith(".mqo")) throw new IOException("This file is not MQO file.");
		FileInputStream fis = new FileInputStream(file);
		parse(fis);
		fis.close();
	}
	
	/**
	 * InputStreamで対応する際はこちら。
	 * @param is
	 * @throws IOException
	 */
	@NeedlessMinecraft
	public MQO(InputStream is) throws IOException {
		// InputStream直接指定する
		parse(is);
	}

	/**
	 * モデルがある場所を文字列でパスとして指定します。主にMinecraft以外の用途で使用する場合に使うことを想定しています。
	 * このコンストラクタから呼び出した場合は、InputStreamはこちら側で閉じます（多分）。
	 * @param r パス文字列。パスに関しては、リソースフォルダーからの相対パスで指定する必要があります。
	 * 例えば、「src/main/resources」をソースフォルダとしている場合、「src/main/resources/test/abc.mqo」を
	 * 読み込むには、rに「test/abc.mqo」を指定します。
	 * 申し訳ありませんが、リソース外のMQOファイルにアクセスする場合は上記「File」による指定をお願いします。
	 *
	 * @throws IOException 指定されたファイルが存在しなかった時
	 */
	@NeedlessMinecraft
	public MQO(String r) throws IOException {
		// Minecraftのリソースに頼らないやつ
		final InputStream is = ClassLoader.getSystemResourceAsStream(r);
		if (is == null) throw new IOException(r + " is not found!");
		parse(is);
		is.close();
	}
	

	/**
	 * InputStreamからファイルを読み込み、MQOフォーマットとして解釈してインスタンス内の値を設定します。
	 *
	 * @param is InputStreamを指定します。勝手に閉じないので2回目の呼び出しをする際はご注意
	 */
	private void parse(InputStream is) {
		StringBuilder sb = new StringBuilder();
		try (Scanner s = new Scanner(is)) {
			// ということで読み込んでいきます。
			// まずは「Object "~~" {」を探します
			
			

			// 番号一覧
			//String o = ""; // オブジェクト名
			MQOObject obj = null; // 一時的に格納するオブジェクト（これを最後に追加するため）
			//int v = 0; // 頂点番号
			//int f = 0; // 面番号
			int col = 0; // 行番号

			// 正規表現一覧
			final String regexO = "Object \\\"(.+)\\\" \\{";
			final String regexVN = "[\t]*vertex [0-9]+ \\{";
			final String regexFN = "[\t]*face [0-9]+ \\{";
			final String regexV = "[\t]*[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)? [+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)? [+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?";
			final String regexF = "[\t]*[34] V\\(([0-9 ]+)\\) M\\(0\\) UV\\(([0123456789\\. ]+)\\)";

			// フラグ一覧
			boolean fo = false;
			boolean fv = false;
			boolean ff = false;
			
			boolean empty = true;

			while (s.hasNextLine()) {
				empty = false; // とりあえず空ではない
				col++;
				// 形しか見ないのでこの3つ以外見ない
				final String line = s.nextLine(); // 1行取得
				if (!Pattern.matches(regexO, line) && !Pattern.matches(regexV, line)
						&& !Pattern.matches(regexF, line) && !Pattern.matches("[\t]*\\}", line)
						&& !Pattern.matches(regexVN, line)
						&& !Pattern.matches(regexFN, line)) {
					// いずれの正規表現にも一致しなかった
					//System.out.println("何にも一致しなかったです");
					continue; // 次の行へ（スキップ）
				} else if (Pattern.matches("[\t]*\\}", line)) {
					// 終了フラグの場合
					if (ff) {
						ff = false;
					} else if (fv) {
						fv = false;
					} else if (fo) {
						this.object.put(obj.getName(), obj); // 格納
						obj = null; // いったん解放
						fo = false;
					} else {
						continue; // 何もしない（関係ない）
					}
					continue; // 処理終了
				}

				// ここからはいろいろ分かれますああ大変

				if (ff) {
					assert obj != null;
					// 次に求めるのはfであります
					if (!Pattern.matches(regexF, line)) // パターンが見つからないとき（MQOフォーマットが異常）
						throw new MQOException("Invalid mqo format!! (face expected but not.) at line " + col);

					// それを次の面番号に追加
					final Matcher m = Pattern.compile(regexF).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					final String vnum = m.group(1); // 頂点番号の格納
					final String uvnum = m.group(2); // UVの格納（頂点倍）

					// どうなっているかというと、グループ1=頂点番号（1 3 5） グループ2=材質番号（無視）
					// グループ3=UVマッピング（0-1正規化、頂点番号と同じ）
					// これをそれぞれ引数として渡す
					obj.getFaces().add(new MQOFace(obj, vnum, uvnum));
				} else if (fv) {
					// 頂点なので、次はvですね
					if (!Pattern.matches(regexV, line)) // パターンが見つからないとき（MQOフォーマットが異常）
						throw new MQOException("Invalid mqo format!! (vertex expected but not.) at line " + col);

					// それを次の頂点番号に追加
					final Matcher m = Pattern.compile(regexV).matcher(line); // 作成して…
					m.find(); // 絶対にあるはず
					final String vnum = m.group(); // 頂点座標の格納
					obj.getVertexs().add(new MQOVertex(obj, vnum));
				} else if (Pattern.matches(regexO, line)) {
					// Objectの始まりだった場合
					final Matcher m = Pattern.compile(regexO).matcher(line);
					m.find();
					obj = new MQOObject(this, m.group(1)); // 名前で作成
					fo = true;

				} else if (Pattern.matches(regexVN, line)) {
					// System.out.println("頂点の始まりです");
					fv = true;
				} else if (Pattern.matches(regexFN, line)) {
					// System.out.println("面の始まりです");
					ff = true;
				}

			}
			
			if (object.isEmpty()) {
				// オブジェクトがないのはおかしいので
				throw new MQOException("Object is empty. Is it valid MQO file?");
			}

		} catch (final MQOException e) {
			throw e;
			/*final CrashReport c = CrashReport.makeCrashReport(e, "MQO format error");
			c.makeCategory("Model Loading");
			Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(c); // クラレポ表示
			Minecraft.getMinecraft().displayCrashReport(c);*/
		}
	}
	
	/**
	 * 全オブジェクト名を配列にしたものを返します。
	 * @return 配列
	 */
	public ArrayList<String> getObjectNames() {
		ArrayList<String> s = new ArrayList<>();
		for (MQOObject o : getObjects4Loop()) {
			s.add(o.getName());
		}
		return s;
	}

	/**
	 * 任意座標の正規化を行います。引数に数字を指定することで、中心を0としたときにその指定した数値までの大きさにモデルの座標を直します。
	 * 例えば、「3」を指定した際は、XYZがそれぞれ「-1.5～1.5」の間に収まるように（最大の長さが3になるように）調整します。
	 * 精度はそこまで高くないため、極端に大きく細かなオブジェクトに対して実行すると切り捨てられて形が崩壊するかもしれません。
	 *
	 * 戻り値として新しくサイズをリサイズしたモデルを返します。
	 *
	 * ※全座標が指定したサイズ未満である場合は実行することで拡大されてしまう可能性があります。（修正する予定ですが）
	 *
	 * @param size 数字で指定します。指定したサイズが最大の長さになるように正規化します。
	 * @return この正規化を実行する前のMQOオブジェクト。
	 *
	 *
	 */
	public MQO normalize(double size) {
		return normalize(size, getObjectNames());

	}
	
	/**
	 * 任意座標の正規化を行います。引数に数字を指定することで、中心を0としたときにその指定した数値までの大きさにモデルの座標を直します。
	 * 例えば、「3」を指定した際は、XYZがそれぞれ「-1.5～1.5」の間に収まるように（最大の長さが3になるように）調整します。
	 * 精度はそこまで高くないため、極端に大きく細かなオブジェクトに対して実行すると切り捨てられて形が崩壊するかもしれません。
	 *
	 * 戻り値として新しくサイズをリサイズしたモデルを返します。
	 *
	 * ※全座標が指定したサイズ未満である場合は実行することで拡大されてしまう可能性があります。（修正する予定ですが）
	 *
	 * @param size 数字で指定します。指定したサイズが最大の長さになるように正規化します。
	 * @param object ここで指定したオブジェクトのみを正規化の対象とします。
	 * @return この正規化を実行する前のMQOオブジェクト。
	 *
	 *
	 */
	public MQO normalize(double size, ArrayList<String> object) {
		// まずロールバックできるように自分自身を代入
		
		MQO original = this.clone();
		// 効率悪いけど全部のオブジェクトに対して作業を繰り返す
		double[][] minmax = original.getMinMaxPosition(object);
		
		
		// ここで、正規化する前の最大・最小座標が入る
		//System.out.println("正規化前座標最小XYZ: " + minmax[0][0] + ", " + minmax[0][1] + ", " + minmax[0][2] + ", 最大XYZ: " + minmax[1][0] + ", " + minmax[1][1] + ", " + minmax[1][2]);
		
		// 次に、サイズに最適化するための係数を算出する
		// XYZそれぞれの距離を算出する
		double sizeX = GMathHelper.distance(minmax[0][0], minmax[1][0]);
		double sizeY = GMathHelper.distance(minmax[0][1], minmax[1][1]);
		double sizeZ = GMathHelper.distance(minmax[0][2], minmax[1][2]); // 以上、3つとも距離を算出する
		// 大きい数を割る
		double per = size / Math.max(Math.max(sizeX, sizeY), sizeZ); // この係数を頂点にかけることで正規化が可能
		
		
		// もう一度ループ回します
		for (MQOObject obj : original.getObjects4Loop()) {
			// オブジェクトの頂点座標を取得する
			for (MQOVertex vertex : obj.getVertexs()) {
				// 全ての頂点の全座標に対してさっきの係数をかける
				vertex.setX(vertex.getX() * per);
				vertex.setY(vertex.getY() * per);
				vertex.setZ(vertex.getZ() * per);
			}
		}
		
		minmax = original.getMinMaxPosition(object);
		
		
		// 処理終了（faceの方には頂点番号しか格納していないので弄る必要がない）
		//System.out.println("正規化後座標最小XYZ: " + minmax[0][0] + ", " + minmax[0][1] + ", " + minmax[0][2] + ", 最大XYZ: " + minmax[1][0] + ", " + minmax[1][1] + ", " + minmax[1][2]);
		return original;
	}
	
	/**
	 * 指定された点（ox, oy, oz）を中心として、xper / yper / zperの倍率でモデルを拡大・縮小します。
	 * 指定される点がモデルの外側にある場合でも拡縮を行います。
	 * 極端に小さな値を指定した場合は計算誤差により正しい形を維持できない場合があります。
	 *
	 * @param ox 原点とみなすX座標。
	 * @param oy 原点とみなすY座標。
	 * @param oz 原点とみなすZ座標。
	 * @param xper X方向の拡縮率。
	 * @param yper Y方向の拡縮率。
	 * @param zper Z方向の拡縮率。
	 * @return 拡縮した後の状態のMQOインスタンス。
	 */
	public MQO rescale(double ox, double oy, double oz, double xper, double yper, double zper) {
		MQO res = this.clone(); // まず返すべきMQOをクローンする
		
		// 指定した原点分を全座標から引いた値をper倍した値が答え
		for (MQOObject o: res.getObjects4Loop()) {
			for (MQOVertex v: o.getVertexs()) {
				v.setX((v.getX() - ox) * xper + ox);
				v.setY((v.getY() - oy) * yper + oy);
				v.setZ((v.getZ() - oz) * zper + oz);
			}
		}
		
		return res;
	}
	
	
	
	/**
	 * 指定した点（ox, oy, oz）を中心として、XYZの比率を維持したままモデルをperで拡大・縮小します。
	 * @param ox 原点とみなすX座標。
	 * @param oy 原点とみなすY座標。
	 * @param oz 原点とみなすZ座標。
	 * @param per 拡縮率。
	 * @return 拡縮した後の状態のMQOインスタンス。
	 */
	public MQO rescale(double ox, double oy, double oz, double per) {
		return rescale(ox, oy, oz, per, per, per);
	}
	
	/**
	 * 指定した頂点を中心として、XYZの比率を維持したままモデルをperで拡大・縮小します。
	 * @param vertex 原点とみなす頂点。
	 * @param per 拡縮率。
	 * @return 拡縮した後の状態のMQOインスタンス。
	 */
	public MQO rescale(MQOVertex vertex, double per) {
		return rescale(vertex.getX(), vertex.getY(), vertex.getZ(), per);
	}
	
	

	/**
	 * このMQOファイルが持つオブジェクトを返します。オブジェクト名を指定する形で返します。オブジェクトが存在しない
	 * 場合はNullが返ります。
	 * @param name オブジェクト名。
	 * @return オブジェクトがあればそのMQOObject、なければnull
	 */
	public MQOObject getObject(String name) {
		return object.get(name);
	}

	/**
	 * このMQOファイルが持つオブジェクトの一覧を返します。あまり使わないでください。ループさせる目的で使用する場合は
	 * それ専用のメソッドを使用してください。
	 * @return オブジェクト一覧
	 */
	public HashMap<String, MQOObject> getObjects() {
		return object;
	}

	/**
	 * MQOオブジェクトをコレクションとして返します。拡張for文にそのまんま使用できるのでループさせたいときはこちらを
	 * ご利用ください。
	 * @return ループできるコレクションとして設定されたMQOObject
	 */
	public Collection<MQOObject> getObjects4Loop() {
		return object.values();
	}


	/**
	 * このMQOオブジェクトを複製します。挙動不審です。

	 * @return
	 */

	public MQO clone() {
		return this;
	}

	public static class MQOException extends RuntimeException {
		// 例外処理

		public MQOException(String mes) {
			super(mes);
		}
	}
	
	/**
	 * このモデルの中心点を算出します。重い処理なので連続で呼び出すことは推奨していません。
	 * 結果はXYZの順番で格納されたdoubleになります
	 * @return 中心点の座標（原点=0としたときの）
	 */
	public double[] getCenterPosition() {
		// 単に各頂点の平均値を取ればいい
		// 効率悪いけど全部のオブジェクトに対して作業を繰り返す
		double[][] minmax = getMinMaxPosition();
		
		double sizeX = GMathHelper.distance(minmax[0][0], minmax[1][0]);
		double sizeY = GMathHelper.distance(minmax[0][1], minmax[1][1]);
		double sizeZ = GMathHelper.distance(minmax[0][2], minmax[1][2]); // 以上、3つとも距離を算出する
		
		return new double[] {minmax[1][0] - sizeX / 2, minmax[1][1] - sizeY / 2, minmax[1][2] - sizeZ / 2};
	}
	
	/**
	 * このMQOオブジェクトの最も小さいand最も大きい頂点座標をXYZ各頂点ごとに算出します。
	 * 結果は二次元配列で返され、[0]が最小値、[1]が最大値となり、その中に0,1,2=X,Y,Zと格納されています。
	 *
	 * あくまで各方向の最小値と最大値であり、これらすべてを満たす頂点が存在するとは限りません。
	 *
	 * @param object 除外するオブジェクト。
	 *
	 * @return 最小XYZと最大XYZ
	 */
	public double[][] getMinMaxPosition(ArrayList<String> object) {
		double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0; // それぞれの頂点に対する最大値を一時的に代入するもの
		for (MQOObject obj : this.getObjects4Loop()) {
			// オブジェクトの頂点座標を取得する
			for (MQOVertex vertex : obj.getVertexs()) {
				if (!object.contains(obj.getName())) continue; // 関係ないオブジェクトの場合は無視
				// 全ての頂点に対して最小値と最大値を算出していく
				if (minX > vertex.getX()) minX = vertex.getX();
				if (maxX < vertex.getX()) maxX = vertex.getX();
				if (minY > vertex.getY()) minY = vertex.getY();
				if (maxY < vertex.getY()) maxY = vertex.getY();
				if (minZ > vertex.getZ()) minZ = vertex.getZ();
				if (maxZ < vertex.getZ()) maxZ = vertex.getZ();
			}
		}
		return new double[][] {new double[] {minX, minY, minZ}, new double[] {maxX, maxY, maxZ}};
	}
	
	/**
	 * 全オブジェクトに対してXYZ各方向の最小値と最大値を返します。
	 * @return 最小値と最大値
	 */
	public double[][] getMinMaxPosition() {
		return getMinMaxPosition(new ArrayList<>());
	}
	
}