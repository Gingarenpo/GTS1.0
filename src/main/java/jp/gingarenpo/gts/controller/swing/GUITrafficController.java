package jp.gingarenpo.gts.controller.swing;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.TileEntityTrafficController;
import jp.gingarenpo.gts.core.GTSSwingGUIBase;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Swingで交通信号制御機についてのGUIを表示させるためのパネル。
 * 本当はマイクラ内で完結させるのがいいんだろうけど、そんな技量ないのでいずれってことで。
 */
public class GUITrafficController extends GTSSwingGUIBase<TileEntityTrafficController> {
	
	private static final int MARGIN = 2; // マージン
	
	private JPanel leftSide; // 左側で制御機自体の設定を行うことができる感じのパネル
	
	private JButton colorChoice; // 色を選択させるボタン
	
	private JTextField nameField; // 制御機の名前（ID）を入力するところ
	
	private JScrollPane cycleContainerBase; // スクロール可能なサイクルを保持するところ
	private JPanel cycleContainer; // 実際にサイクルコンテナが格納されるところ
	private ArrayList<CyclePane> cyclePanels; // サイクルパネルの一覧
	private JPanel cycleTitlePanel; // タイトルがないとわからないだろうので
	private JLabel cycleTitle;
	private JButton cycleAdd; // サイクルを増やすためのボタン
	
	private JButton done; // 変更を反映するボタン
	
	public GUITrafficController(JFrame w) {
		super(w);
	}
	
	
	/**
	 * パーツの準備をする
	 */
	@Override
	public void init() {
		// 固有の名前を付けておく
		this.setName("GUITrafficController");

		
		
		// 左サイドバーの設定
		leftSide = new JPanel();
		leftSide.setName("leftSide");
		leftSide.setBounds(0, 0, GTS.windowWidth / 4, GTS.windowHeight);
		leftSide.setLayout(null);
		leftSide.setOpaque(true);
		leftSide.setBackground(new Color(200, 200, 200));
		leftSide.setVisible(true);
		
		// 制御機の名前を変えられるところ
		nameField = new JTextField();
		nameField.setBounds(MARGIN, MARGIN, GTS.windowWidth / 4 - MARGIN * 2, GTS.windowHeight / 20);
		nameField.setText("EMPTY");
		
		// 色ボタンの表示
		colorChoice = new JButton();
		colorChoice.setBounds(MARGIN, GTS.windowHeight / 20 + MARGIN, GTS.windowWidth / 4 - MARGIN * 2, GTS.windowHeight / 20);
		colorChoice.setText("Color: RGB(XXX,XXX,XXX)");
		colorChoice.addActionListener(e -> {
			// ボタンが押されたとき
			// 色選択ダイアログを出す
			Color c = JColorChooser.showDialog(null, "Choose traffic controller color", GUITrafficController.this.data.getData().getColor()); // 色を求める
			if (c == null) return;
			GUITrafficController.this.data.getData().setColor(c); // 色を更新
			GUITrafficController.this.updateGUI(); // GUI更新
		});
		
		// OKボタンの表示（左下にでも）
		done = new JButton();
		done.setText("OK");
		done.setBounds(MARGIN, GTS.windowHeight - GTS.windowHeight / 20 - MARGIN, GTS.windowWidth / 4 - MARGIN * 2, GTS.windowHeight / 20);
		done.addActionListener(e -> {
			TileEntityTrafficController data = GUITrafficController.this.data;
			// まず値の更新
			data.getData().setName(nameField.getText());
			// ボタンが押されたらGUIを閉じて次回パケット更新を待つ
			data.createTexture(true);
			GUITrafficController.this.onClose();
			// んでクライアント側で更新を通知する
			data.getWorld().notifyBlockUpdate(data.getPos(), data.getWorld().getBlockState(data.getPos()), data.getWorld().getBlockState(data.getPos()), 2);
		});
		
		// サイクルパネルの設定
		cycleContainerBase = new JScrollPane();
		cycleContainer = new JPanel();
		cyclePanels = new ArrayList<CyclePane>();
		cycleAdd = new JButton();
		cycleTitlePanel = new JPanel();
		cycleTitlePanel.setBounds(GTS.windowWidth / 4, 0, GTS.windowWidth / 4 * 3, GTS.windowHeight / 20);
		cycleTitle = new JLabel();
		cycleContainerBase.setBounds(GTS.windowWidth / 4, GTS.windowHeight / 20, GTS.windowWidth / 4 * 3, GTS.windowHeight);
		cycleContainerBase.setViewportView(cycleContainer); // これを内包
		
		// タイトルの部分（サイクル）
		GridBagLayout g = new GridBagLayout(); // サイクルタイトルの部分はグリッドにする
		
		GridBagConstraints gbc = new GridBagConstraints();
		cycleTitlePanel.setLayout(g);
		cycleTitle.setText("There are no cycle.");
		cycleAdd.setText("+"); // こんな感じで
		GTSSwingGUIBase.addGrid(g, gbc, cycleTitle, 0, 0, 1, 1, 0.9);
		cycleTitlePanel.add(cycleTitle);
		GTSSwingGUIBase.addGrid(g, gbc, cycleAdd, 10, 0, 1, 1, 0.1);
		cycleTitlePanel.add(cycleAdd);
		
		
	
		// 最後に全部反映
		leftSide.add(nameField);
		leftSide.add(colorChoice);
		leftSide.add(cycleContainerBase);
		leftSide.add(done);
		this.add(leftSide);
		this.add(cycleContainerBase);
		this.add(cycleTitlePanel);
	}
	
	/**
	 * 制御機の値を反映させる
	 */
	@Override
	public void updateGUI() {
		if (!this.canShow()) return;
		// 色の反映
		Color c = data.getData().getColor();
		colorChoice.setText(String.format("Color: RGB(%d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue()));
		
		// 名前の反映
		nameField.setText(data.getData().getName());
		this.revalidate(); // 再レイアウト
	}
	
	/**
	 * オーバーライドしていないupdateでは同時にGUIも更新してしまう。
	 * サイクルが切り替わったことを確認したいだけなのでこちらを別途用意する。
	 * @param data
	 */
	public void updateCycle(TileEntityTrafficController data) {
		this.data = data;
	}
	
	
	
	/**
	 * サイクルのパネルはそれだけで結構特殊なので別途クラスを用意
	 */
	public class CyclePane extends JPanel {
		
	
	}
	
}
