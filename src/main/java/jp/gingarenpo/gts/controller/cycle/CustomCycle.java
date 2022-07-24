package jp.gingarenpo.gts.controller.cycle;

import java.io.File;

/**
 * Javaクラスを読み込むことに非対応な場合の為に、簡単な命令郡のセットで簡単に制御を実装できる
 * 独自スクリプト言語を定義してそれを使うための物。
 * 高度なことをしたい場合はJavaクラスを作成してアドオンとして追加することを考慮し、また
 * JavaScriptに関してはエンジン自体がJava11で廃止されるとのことなので（1.12.2ではJava8なので平気だが）
 * 依存するのをやめて独自スクリプトにしてみる。なお、一応JSも対応するようにするつもり（このクラスではない）
 *
 * なお解析自体は非常に遅いためあまりこれ使わない方がいいかも。要改善。
 * 現在試験使用の段階の為当クラスは非推奨
 *
 * @deprecated
 */
public class CustomCycle extends Cycle {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * カスタムスクリプトが設置されている場所
	 */
	private File path;
	
	/**
	 * 指定したサイクル名と指定したカスタムスクリプトの場所において
	 * このサイクルを作成する。指定したパスが存在しない場合でも作成を行うが
	 * 実行時に問題が発生する（詳しくは実行時参照）
	 * @param name 名前
	 * @param path パス（存在する場所を指定すること）
	 */
	public CustomCycle(String name, File path) {
		super(name);
		this.path = path;
	}
}
