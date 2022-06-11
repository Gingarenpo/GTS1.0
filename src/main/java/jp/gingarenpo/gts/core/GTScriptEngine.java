package jp.gingarenpo.gts.core;

/**
 * 簡易的なスクリプト解析エンジン。オリジナルで定義したカスタムスクリプト文法をコンパイルし、
 * Javaのデータとしてまとめて入出力を可能としている。コンパイルエラーが発生すると例外を発生させる。
 * カスタムスクリプトの仕様についてはGitHub参照
 *
 * 定義をしていないため現在非推奨。
 * @deprecated
 */
public class GTScriptEngine {
	
	/**
	 * スクリプト全体を保持する変数。UTF-8とする
	 */
	private String script;
}
