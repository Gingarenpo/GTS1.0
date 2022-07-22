package jp.gingarenpo.gts.controller.gui.swing;

import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.controller.cycle.Cycle;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;

/**
 * Swingの機能を使って擬似的なGUIを作成する。
 * MinecraftのGUIでは限界があり、絶対的な座標指定しかできないため、Swingで臨機応変に対応しようとした。
 * このクラスはJFrameを継承している為、このインスタンスを作成して表示させることでGUI処理を行うことができる。
 *
 * 直接実行できるようにmainクラスも用意している（デバッグ検証用）
 */
public class SwingGUITrafficController extends JFrame {
	
	/**
	 * このGUIが変更をくわえようとする制御機のインスタンス。直接変更を加えるため参照は同じ必要がある。
	 */
	private TrafficController tc;
	
	public static final int maxWidth = 960;
	public static final int maxHeight = 540;
	
	private int width;
	private int height;
	
	/**
	 * 左側1/4に配置するベースの情報を入れるためのパネル
	 */
	private JPanel basePanel;
	
	/**
	 * サイクルを入れるためのスクロール可能パネル（コンテナ）
	 */
	private JScrollPane cyclesContainer;
	
	/**
	 * サイクルパネルを配置するために入れるべきヤツ
	 */
	private JPanel cyclesPanel;
	
	GridBagLayout cyclesPanelLayout = new GridBagLayout();

	public static void main(String[] args) {
		// 作成には制御機のインスタンスが必要なので適当に作って入れる
		TrafficController tc = new TrafficController();
		SwingGUITrafficController stc = new SwingGUITrafficController(tc);
		stc.setVisible(true);
	}
	
	/**
	 * 指定した制御機の情報をもとにして、GUIを作成する。
	 * このコンストラクタを呼んだ後、「setVisible」を実行するだけで表示される.
	 *
	 * @param tc
	 */
	public SwingGUITrafficController(TrafficController tc) {
		super();
		this.tc = tc;
		
		// ウィンドウ自体の設定
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 閉じたら破棄する
		this.setTitle(String.format("制御機「%s」の設定", tc.getName()));
		
		// 画面解像度の取得
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		if (d.width <= maxWidth) {
			// 幅が足りない場合
			width = d.width;
			height = d.height * (d.height / maxHeight);
		}
		else if (d.height <= maxHeight) {
			width = d.width * (d.width / maxWidth);
			height = d.height;
		}
		else {
			width = maxWidth;
			height = maxHeight;
		}
		
		this.setLayout(null); // 絶対座標
		
		// 制御機基本情報パネルを配置（左側）
		basePanel = new BaseInfoPanel();
		this.add(basePanel);
		
		// サイクル追加ボタンを配置
		JButton addCycleButton = new JButton("サイクル追加");
		addCycleButton.addActionListener((e) -> {
			// ボタンが押されたらサイクルを1つ追加する
			LinkedHashMap<String, Cycle> cs = SwingGUITrafficController.this.tc.getCycles();
			Cycle newCycle = new Cycle();
			cs.put(newCycle.getName(), newCycle);
			SwingGUITrafficController.this.tc.setCycles(cs); // 1個追加
			refreshCycles();
			setTitle(String.format("サイクル「%s」を追加しました - 制御機「%s」の設定", newCycle.getName(), tc.getName()));
		});
		addCycleButton.setBounds(width / 4, 0, width / 4 * 3, 20);
		add(addCycleButton);
		
		// サイクルパネルを用意
		cyclesPanel = new JPanel();
		cyclesPanel.setBounds(width / 4, 0, width / 4 * 3, height);
		cyclesPanel.setBackground(Color.WHITE);
		cyclesPanel.setLayout(cyclesPanelLayout);
		
		refreshCycles();
		
		// サイクルパネルを配置（右側）
		cyclesContainer = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		cyclesContainer.setBounds(width / 4, 25, width / 4 * 3, height - 25);
		cyclesContainer.setViewportView(cyclesPanel);
		this.add(cyclesContainer);
		
		this.getContentPane().setPreferredSize(new Dimension(width, height));
		this.pack();
		this.setLocationRelativeTo(null); // 画面中央へ
	}
	
	private void refreshCycles() {
		cyclesPanel.removeAll();

		// サイクルパネルに現在のサイクルを配置していく
		int i = 1;
		for (Cycle c: tc.getCycles().values()) {
			CyclePanel cp = new CyclePanel(c);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.NORTH;
			cyclesPanelLayout.setConstraints(cp, gbc);
			cyclesPanel.add(cp);
			i++;
		}
		
		cyclesPanel.revalidate();
		cyclesPanel.repaint();

	}
	
	/**
	 * 画面左側に表示させるパネル
	 */
	public class BaseInfoPanel extends JPanel {
		
		private JLabel nameLabel = new JLabel("制御機名称(ENTERで反映)");
		
		private JTextField nameField = new JTextField(32);
		
		private JButton colorButton = new JButton();
		
		public BaseInfoPanel() {
			super();
			this.setBounds(0, 0, SwingGUITrafficController.this.width / 4, SwingGUITrafficController.this.height);
			this.setBackground(Color.WHITE);
			
			// レイアウトを指定する（行配置を試みるためBoxLayoutにする）
			this.setLayout(null);
			
			// 子要素の編集
			nameLabel.setBounds(0, 0, this.getWidth(), 20);
			nameField.setBounds(0, 25, this.getWidth(), 20);
			nameField.setText(SwingGUITrafficController.this.tc.getName());
			nameField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() != KeyEvent.VK_ENTER) {
						// ENTERキーじゃない場合
						return;
					}
					// 制御機に使用可能な文字をチェックする（今回はパス）
					
					SwingGUITrafficController.this.tc.setName(nameField.getText());
					SwingGUITrafficController.this.setTitle(String.format("制御機名称を変更しました - 制御機「%s」の設定", tc.getName()));
				}
			});
			colorButton.setBounds(0, 50, this.getWidth(), 20);
			colorButton.setText(String.format("色 = RGB(%d, %d, %d)", SwingGUITrafficController.this.tc.getColor().getRed(), SwingGUITrafficController.this.tc.getColor().getGreen(), SwingGUITrafficController.this.tc.getColor().getBlue()));
			colorButton.addActionListener((e) -> {
				// カラーチューザーを開く
				Color newColor = JColorChooser.showDialog(this, "制御機の色を選択してください", SwingGUITrafficController.this.tc.getColor());
				if (newColor != null) {
					SwingGUITrafficController.this.tc.setColor(newColor);
					colorButton.setText(String.format("色 = RGB(%d, %d, %d)", newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
					SwingGUITrafficController.this.setTitle(String.format("色を変更しました - 制御機「%s」の設定", tc.getName()));
				}
			});
			
			
			// 全部追加
			this.add(nameLabel);
			this.add(nameField);
			this.add(colorButton);
		}
	}
	
	/**
	 * サイクルごとのパネル
	 */
	public class CyclePanel extends JPanel {
		
		private Cycle cycle;
		
		public CyclePanel(Cycle cycle) {
			super();
			this.cycle = cycle;
			setOpaque(false);
			setBackground(Color.white);
			setBorder(new LineBorder(Color.BLACK, 1));
			setLayout(null);
			setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 100));
			setMinimumSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 100));
			
			// サイクル名称いれるとこ
			JLabel l1 = new JLabel("サイクル名称");
			l1.setBounds(0, 0, SwingGUITrafficController.this.width / 4 * 3 - 50, 20);
			l1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 20));
			add(l1);
			// フィールド
			JTextField t1 = new JTextField();
			t1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			t1.setBounds(5, 25, SwingGUITrafficController.this.width / 4 * 3 - 60, 20);
			t1.setText(cycle.getName());
			add(t1);
			// サイクル削除ボタン
			JButton b1 = new JButton("このサイクルを削除する（戻せません！）");
			b1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			b1.setBounds(5, 50, SwingGUITrafficController.this.width / 4 * 3 - 60, 20);
			b1.addActionListener((e) -> {
				// ボタンが押されたらサイクルを削除する
				LinkedHashMap<String, Cycle> cs = SwingGUITrafficController.this.tc.getCycles();
				cs.remove(cycle.getName()); // そのサイクルを消去する
				SwingGUITrafficController.this.tc.setCycles(cs); // 1個追加
				setTitle(String.format("サイクル「%s」を削除しました - 制御機「%s」の設定", cycle.getName(), tc.getName()));
				refreshCycles();
				
			});
			add(b1);
			
		}
		
		public Cycle getCycle() {
			return cycle;
		}
	}
}
