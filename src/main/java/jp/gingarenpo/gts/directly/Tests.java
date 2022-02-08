package jp.gingarenpo.gts.directly;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.controller.swing.GUITrafficController;
import net.minecraft.tileentity.TileEntity;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * 注意：開発用のテストクラスなので通常使用しないこと。
 * 本来はtestの方に書くべきなんだろうけど設定めんどくさいのでこちらに書いている
 */
public class Tests {
	
	/**
	 * エントリーポイント。テストしたい内容をここに記していく。常に変わる
	 * @param args
	 */
	public static void main(String[] args) {
		GTS.windowWidth = 960;
		GTS.windowHeight = 540;
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		JFrame j = new JFrame();
		GUITrafficController g = new GUITrafficController(j);
		g.update(new TileEntityTrafficController());
		j.getContentPane().setPreferredSize(new Dimension(960, 540));
		j.pack();
		j.setLayout(null);
		j.setLocationRelativeTo(null);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		j.getContentPane().add(g);
		j.setVisible(true);
		System.out.println(g.getComponent(2));
	}
}
