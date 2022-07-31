package jp.gingarenpo.gts.light.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.gui.GTSSwingGUIModelChoiceBase;
import jp.gingarenpo.gts.light.ModelTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import jp.gingarenpo.gts.light.TrafficLight;
import jp.gingarenpo.gts.pack.Loader;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * 交通信号機のチャンネルとモデルパックを選択するためのGUIを開くやつ。
 * 制御機と違ってシンプルなのでウィンドウも小さめ。
 */
public class SwingGUITrafficLight extends GTSSwingGUIModelChoiceBase<ModelTrafficLight, TileEntityTrafficLight, TrafficLight> {
	
	
	public static void main(String[] args) throws IOException {
		// 作成には制御機のインスタンスが必要なので適当に作って入れる
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
		GTS.loader = new Loader();
		GTS.loader.load(GTSModDir);
		TileEntityTrafficLight tetl = new TileEntityTrafficLight();
		tetl.setData(new TrafficLight(1));
		SwingGUITrafficLight stl = new SwingGUITrafficLight(tetl);
		stl.setVisible(true);
	}
	
	public SwingGUITrafficLight(TileEntityTrafficLight tetl) {
		super(tetl.getAddon(), tetl, tetl.getData());
		this.setTitle("交通信号機の設定");
		create();
	}
	
	/**
	 * オーバーライドしないといけないのにfunc使わない
	 */
	protected SwingGUITrafficLight create() {
		super.create(()->{
			te.setAddon(model);
			this.te.getAddon().reloadTexture();
			setTitle(String.format("モデルを変更しました"));
			this.te.markDirty();
		});
		
		// チャンネルの設定
		JLabel l1 = new JLabel("チャンネル");
		l1.setBounds(0, 25, width, 20);
		this.getContentPane().add(l1);
		JTextField t1 = new JTextField();
		t1.setBounds(5, 50, width - 10, 20);
		t1.setText(String.valueOf(data.getSignal()));
		t1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						long channel = Long.parseLong(t1.getText());
						data.setSignal(Math.toIntExact(channel));
						setTitle(String.format("チャンネルを%dに変更", channel));
					} catch (IllegalArgumentException e2) {
						// 整数じゃない文字列が入ってきた場合
						JOptionPane.showMessageDialog(SwingGUITrafficLight.this, "正の整数を入力してください！", "入力値が不正です", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
				}
			}
		});
		this.getContentPane().add(t1);
		
		return this;
	}
}
