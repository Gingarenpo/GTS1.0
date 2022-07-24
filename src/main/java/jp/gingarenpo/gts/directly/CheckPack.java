package jp.gingarenpo.gts.directly;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.pack.Loader;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;


public class CheckPack {
	
	public static void main(String[] args) throws IOException {
		// パスを登録する（基本的にこの中身を見る）
		File GTSModDir = new File("run\\mods\\GTS"); // 代入
		System.out.println(GTSModDir.getAbsolutePath());
		if (!GTSModDir.exists()) {
			// 存在しない場合は作成する
			if (!GTSModDir.mkdir()) {
				throw new IOException("GTS can't create mod directory."); // ディレクトリを作れないとエラー
			}
		}
		
		GTS.GTSLog = LogManager.getLogger("ああああ");
		
		// パック確認する用
		Loader l = new Loader();
		l.load(GTSModDir);
	}
}
