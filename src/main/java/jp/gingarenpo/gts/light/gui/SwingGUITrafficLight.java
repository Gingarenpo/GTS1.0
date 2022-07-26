package jp.gingarenpo.gts.light.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.ModelBase;
import jp.gingarenpo.gts.light.ModelTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import jp.gingarenpo.gts.light.TrafficLight;
import jp.gingarenpo.gts.pack.Loader;
import jp.gingarenpo.gts.pack.Pack;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 交通信号機のチャンネルとモデルパックを選択するためのGUIを開くやつ。
 * 制御機と違ってシンプルなのでウィンドウも小さめ。
 */
public class SwingGUITrafficLight extends JFrame {
	
	public static final int width = 300;
	public static final int height = 200;
	
	private TileEntityTrafficLight tetl;
	private TrafficLight tl;
	public ModelTrafficLight model;
	
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
		super();
		this.tetl = tetl;
		this.tl = this.tetl.getData();
		
		this.getContentPane().setPreferredSize(new Dimension(width, height));
		this.pack();
		this.setAlwaysOnTop(true);
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("交通信号機の設定");
		
		// チャンネルの設定
		JLabel l1 = new JLabel("チャンネル");
		l1.setBounds(0, 0, width, 20);
		this.getContentPane().add(l1);
		JTextField t1 = new JTextField();
		t1.setBounds(5, 25, width - 10, 20);
		t1.setText(String.valueOf(tl.getSignal()));
		t1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						long channel = Long.parseLong(t1.getText());
						tl.setSignal(Math.toIntExact(channel));
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
		
		// モデルパックの設定
		LinkedHashMap<String, ModelTrafficLight> models = new LinkedHashMap<>();
		models.put("【組込】DUMMY（通常選べません）", null);
		int index = 0;
		int i = 1;
		for (Pack pack: GTS.loader.getPacks().values()) {
			// パックの中からさらにモデルを引っ張り出す
			for (ModelBase model: pack.getModels()) {
				// Traffic Lightと一致するものを取り出す
				if (!(model instanceof ModelTrafficLight)) {
					i++;
					continue;
				}
				
				models.put(getChoiceName(pack, (ModelTrafficLight) model), (ModelTrafficLight) model);
				
				if (Objects.equals(model, this.model)) {
					GTS.GTSLog.info("found.");
					index = i;
				}
				i++;
			}
		}
		System.out.println(i);
		
		
		// モデルパック選択肢を表示
		JComboBox<String> c1 = new JComboBox(models.keySet().toArray());
		c1.setBounds(5, 75, width - 10, 20);
		c1.setSelectedIndex(index);
		c1.addActionListener((e) -> {
			// そのモデルに変更する
			ModelTrafficLight model = models.get(c1.getSelectedItem());
			if (model == null) {
				return; // 何もしません
			}
			this.tetl.setAddon(model);
			this.tetl.getAddon().reloadTexture();
			setTitle(String.format("モデルを変更しました"));
			this.tetl.markDirty();
		});
		this.getContentPane().add(c1);
		
		
	}
	
	/**
	 * 選択肢に使用するためのパックとモデルの組み合わせにした一意の文字列を返す。
	 * @param pack
	 * @param model
	 * @return
	 */
	private String getChoiceName(Pack pack, ModelTrafficLight model) {
		return String.format("【%s】%s", pack.getName(), model.getConfig().getId());
	}
}
