package jp.gingarenpo.gts.proxy;

/**
 * サーバー用。こちらはクライアントでのみ実行する部分を空の実装に置き換える。
 * インターフェースをこのために用意するのはめんどくさいので止めた。
 */
public class GTSServerProxy extends GTSProxy {
	@Override
	public void registerItemModels() {
		// 空実装
	}
	
	@Override
	public void registerTESRs() {
		// 以下略
	}
	
	@Override
	public void registerResourcePackLoader() {
		//
	}
}
