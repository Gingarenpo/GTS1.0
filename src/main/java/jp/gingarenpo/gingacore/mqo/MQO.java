package jp.gingarenpo.gingacore.mqo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gingarenpo.gingacore.annotation.NeedlessMinecraft;
import jp.gingarenpo.gingacore.helper.GMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * このクラスは、MQOオブジェクトを作成します。このオブジェクト自体は基本的に使わず、サブオブジェクトを頻繁に使う
 * ことになるでしょう。<strike>ちなみに、オブジェクトによる分別は今のところ考えていません。</strike>考えざるを
 * えなくなってしまいました。
 *
 * @author 銀河連邦
 */
public class MQO {

	/**
	 * オブジェクトの名前をキーとして格納しています
	 */
	private HashMap<String, MQOObject> object = new HashMap<String, MQOObject>();


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
	 * クローンを作成する際に使用する専用のコンストラクタです。このコンストラクタは使用してはいけません（もっぱら使えないようにしている）
	 *
	 * @param orig
	 */
	private MQO(MQO orig) {
		// 内部でのみ使用する
		this.object = (HashMap<String, MQOObject>) orig.object.clone();
	}

	/**
	 * InputStreamからファイルを読み込み、MQOフォーマットとして解釈してインスタンス内の値を設定します。
	 *
	 * @param is InputStreamを指定します。勝手に閉じないので2回目の呼び出しをする際はご注意
	 */
	private void parse(InputStream is) {
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
					// System.out.println("何にも一致しなかったです");
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
					// System.out.println("オブジェクトの始まりです");
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
	 * 任意座標の正規化を行います。引数に数字を指定することで、中心を0としたときにその指定した数値までの大きさにモデルの座標を直します。
	 * 例えば、「3」を指定した際は、XYZがそれぞれ「-1.5～1.5」の間に収まるように（最大の長さが3になるように）調整します。
	 * 精度はそこまで高くないため、極端に大きく細かなオブジェクトに対して実行すると切り捨てられて形が崩壊するかもしれません。
	 *
	 * 挙動不審なことが多いため、このメソッドは正規化した後返り値として正規化前のMQOオブジェクトを返します。通常は直接メソッドを
	 * 叩いて構いませんが、何かの都合でロールバックしたい場合などは、代入しておくとバックアップの代わりにもなります。
	 *
	 * ※全座標が指定したサイズ未満である場合は実行することで拡大されてしまう可能性があります。（修正する予定ですが）
	 *
	 * @param size 数字で指定します。指定したサイズが最大の長さになるように正規化します。
	 * @return この正規化を実行する前のMQOオブジェクト。
	 *
	 *
	 */
	public MQO normalize(double size) {
		// まずロールバックできるように自分自身を代入
		MQO original = (MQO) this.clone();

		// 効率悪いけど全部のオブジェクトに対して作業を繰り返す
		double minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0; // それぞれの頂点に対する最大値を一時的に代入するもの
		for (MQOObject obj : this.getObjects4Loop()) {
			// オブジェクトの頂点座標を取得する
			for (MQOVertex vertex : obj.getVertexs()) {
				// 全ての頂点に対して最小値と最大値を算出していく
				if (minX > vertex.getX()) minX = vertex.getX();
				if (maxX < vertex.getX()) maxX = vertex.getX();
				if (minY > vertex.getY()) minY = vertex.getY();
				if (maxY < vertex.getY()) maxY = vertex.getY();
				if (minZ > vertex.getZ()) minZ = vertex.getZ();
				if (maxZ < vertex.getZ()) maxZ = vertex.getZ();
			}
		}
		// ここで、正規化する前の最大・最小座標が入る
		System.out.println("正規化前座標最小XYZ: " + minX + ", " + minY + ", " + minZ + ", 最大XYZ: " + maxX + ", " + maxY + ", " + maxZ);

		// 次に、サイズに最適化するための係数を算出する
		// XYZそれぞれの距離を算出する
		double sizeX = GMathHelper.distance(minX, maxX);
		double sizeY = GMathHelper.distance(minY, maxY);
		double sizeZ = GMathHelper.distance(minZ, maxZ); // 以上、3つとも距離を算出する
		// 大きい数を割る
		double per = size / Math.max(Math.max(sizeX, sizeY), sizeZ); // この係数を頂点にかけることで正規化が可能

		// もう一度ループ回します
		for (MQOObject obj : this.getObjects4Loop()) {
			// オブジェクトの頂点座標を取得する
			for (MQOVertex vertex : obj.getVertexs()) {
				// 全ての頂点の全座標に対してさっきの係数をかける
				vertex.setX(vertex.getX() * per);
				vertex.setY(vertex.getY() * per);
				vertex.setZ(vertex.getZ() * per);
			}
		}

		// 処理終了（faceの方には頂点番号しか格納していないので弄る必要がない）
		System.out.println("正規化後座標最小XYZ: " + minX * per + ", " + minY * per + ", " + minZ * per + ", 最大XYZ: " + maxX * per + ", " + maxY * per + ", " + maxZ * per);
		return original;

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
	 * 各々のオブジェクトに存在する面に対してdrawFace()を呼び出すだけのラッピングメソッドです。
	 */
	public void draw() {
		// ラッピング処理
		for (final MQOObject obj : object.values()) {
			for (final MQOFace face : obj.getFaces()) {
				face.drawFace(0.0);
			}
		}
	}
	
	/**
	 * 各々のオブジェクトに存在する面に対してdrawFace()を呼び出すだけのラッピングメソッドです。
	 * 日照角度を指定するとそれに合わせて影をつけます。影をつけたくない場合は引数なしで。
	 */
	public void draw(double sun) {
		// ラッピング処理
		for (final MQOObject obj : object.values()) {
			for (final MQOFace face : obj.getFaces()) {
				face.drawFace(sun);
			}
		}
	}
	

	/**
	 * このMQOオブジェクトを複製します。挙動不審です。
	 * @return
	 */
	@Override
	public MQO clone() {
		// クローンする際は、全パラメーターを代入しなくてはならない
		// その際は、コンストラクタに任せている
		return new MQO(this);
	}

	@SuppressWarnings("serial")
	public class MQOException extends RuntimeException {
		// 例外処理

		public MQOException(String mes) {
			super(mes);
		}
	}
}