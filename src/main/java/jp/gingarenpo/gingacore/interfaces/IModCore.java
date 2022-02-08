package jp.gingarenpo.gingacore.interfaces;

/**
 * GingaCoreにおいて、バージョンチェックをしたい場合はこれを指定する必要があります。バージョンチェックしたいModの
 * コアファイル（@Modアノテーションがついている）クラスに実装してください。アノテーションによる取得も考えたんですが
 * めんどくさいのと技術不足なためこちらを採用します。いずれアノテーションによりできた場合でも、下位互換性維持のため
 * 外さないでおくことをお勧めします。
 * <hr>
 * このインターフェースは、4つのメソッドを必要とします。
 *
 * <h2>getMajorVersion()</h2>
 * <p>メジャーバージョンを取得します。このModのメジャーバージョンを取得できるようにしてください。整数値です。</p>
 *
 * <h2>getMinorVersion()</h2>
 * <p>マイナーバージョンを取得します。マイナーバージョンとは、「1.2.3.4」というバージョン名だとしたら「2」「3」「4」
 * となります。このように、複数のマイナーバージョンがある場合が多いので、返すものはその配列とします。左側にあるほど
 * 配列の添え字が小さくなるようにしてください。さっきの例だと、「(0)=2」「(1)=3」「(2)=4」となります。</p>
 *
 * <h2>getModId()</h2>
 * <p>ModのIDを文字列で取得します。名前ではありません。文字列です！</p>
 *
 * <h2>getURL()</h2>
 * <p>更新データを置いているURLを<b>文字列</b>で返します。URLで返してもいいんですがそうすると例外処理が一任する
 * 形になりややこしくなるため、文字列での返却にしています。</p>
 *
 * <p>ちなみに、GingaCore自体にはこれ入れていない（Staticで読み込める）ので、サンプルを見たい場合は「GTC」の
 * Modファイルを参照してみてください。staticメソッドじゃないので注意！</p>
 *
 * @since Ver2.0～
 *
 * @author 銀河連邦
 *
 */
public interface IModCore {

	/**
	 * <p>メジャーバージョンを取得します。このModのメジャーバージョンを取得できるようにしてください。整数値です。</p>
	 * @return この実行しているMod自体のメジャーバージョン。
	 */
	public int getMajorVersion();

	/**
	 * <p>マイナーバージョンを取得します。マイナーバージョンとは、「1.2.3.4」というバージョン名だとしたら「2」「3」「4」
	 * となります。このように、複数のマイナーバージョンがある場合が多いので、返すものはその配列とします。左側にあるほど
	 * 配列の添え字が小さくなるようにしてください。さっきの例だと、「(0)=2」「(1)=3」「(2)=4」となります。</p>
	 * @return この実行しているMod自体のマイナーバージョンを配列として返す。
	 */
	public int[] getMinorVersion();

	/**
	 * <p>ModのIDを文字列で取得します。名前ではありません。文字列です！</p>
	 * @return ModのID
	 */
	public String getModId();

	/**
	 * <p>更新データを置いているURLを<b>文字列</b>で返します。URLで返してもいいんですがそうすると例外処理が一任する
	 * 形になりややこしくなるため、文字列での返却にしています。</p>
	 * @return 更新データのあるURL（http://～）
	 */
	public String getURL();
}
