package jp.gingarenpo.gts.controller.gui.swing;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.TrafficController;
import jp.gingarenpo.gts.controller.cycle.Cycle;
import jp.gingarenpo.gts.controller.cycle.TimeCycle;
import jp.gingarenpo.gts.controller.phase.Phase;
import jp.gingarenpo.gts.controller.phase.PhaseBase;
import jp.gingarenpo.gts.controller.phase.UntilDetectPhase;
import jp.gingarenpo.gts.core.json.Exclude;
import jp.gingarenpo.gts.core.json.ExcludeJsonStrategy;
import jp.gingarenpo.gts.exception.DataExistException;
import jp.gingarenpo.gts.light.ConfigTrafficLight;
import jp.gingarenpo.gts.light.TileEntityTrafficLight;
import net.minecraftforge.fml.common.Loader;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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
	private final TrafficController tc;
	
	public static final int maxWidth = 960;
	public static final int maxHeight = 540;
	
	private final int width;
	private final int height;
	
	
	/**
	 * 左側1/4に配置するベースの情報を入れるためのパネル
	 */
	private JPanel basePanel;
	
	/**
	 * サイクルを入れるためのスクロール可能パネル（コンテナ）
	 */
	private final JScrollPane cyclesContainer;
	
	/**
	 * サイクルパネルを配置するために入れるべきヤツ
	 */
	private final JPanel cyclesPanel;
	
	public static final JsonDeserializer<Phase> JSON_DESERIALIZER_PHASE = (json, t, ctx) -> {
		// 登録したクラスから確認する
		for (String className: GTS.phases) {
			try {
				// クラスを読み出す
				Class c = Class.forName(className, true, Loader.instance().getModClassLoader());
				
				// クラスで定義されたフィールドが存在すればそのクラスで返す
				for (Field f: c.getDeclaredFields()) {
					if (json.getAsJsonObject().get(f.getName()) != null) {
						return ctx.deserialize(json, c);
					}
				}
				
			} catch (ClassNotFoundException e) {
				// 見つからない場合はスキップする
				GTS.GTSLog.warn("Warning. " + className + " is not found on ClassLoader.");
				continue;
			}
		}
		GTS.GTSLog.error("Error. Cannot deserialize Phase because no class found. PhaseBase class use.");
		return ctx.deserialize(json, PhaseBase.class);
	};
	
	GridBagLayout cyclesPanelLayout = new GridBagLayout();

	public static void main(String[] args) throws IOException {
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
		this.setAlwaysOnTop(true);
		
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
			Cycle newCycle = new TimeCycle(0, 24000);
			cs.put(newCycle.getName(), newCycle);
			SwingGUITrafficController.this.tc.setCycles(cs); // 1個追加
			refreshCycles();
			setTitle(String.format("サイクル「%s」を追加しました - 制御機「%s」の設定", newCycle.getName(), tc.getName()));
		});
		addCycleButton.setBounds(width / 4 + 5, 2, width / 4 * 3 - 10, 20);
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
	
	private void refreshBase() {
		// 左側のパネル初期化
		this.remove(basePanel);
		basePanel = new BaseInfoPanel();
		this.add(basePanel);
		this.getContentPane().revalidate();
		this.getContentPane().repaint();
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
	
	private void refreshPhases(CyclePanel cp) {
		int i = 0;
		cp.phases.removeAll();
		for (Phase p: cp.cycle.getPhases()) {
			PhasePanel pp = new PhasePanel(cp.cycle, p);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.NORTH;
			cp.layout.setConstraints(pp, gbc);
			cp.phases.add(pp);
			i++;
		}
		cp.phases.revalidate();
		cp.phases.repaint();
	}
	
	private void refreshChannels(PhasePanel pp) {
		int i = 0;
		pp.channelPanel.removeAll();
		for (Map.Entry<Long, ConfigTrafficLight.LightObject> s: pp.phase.getChannels().entrySet()) {
			ChannelPanel cp = new ChannelPanel(pp.phase, s.getKey(), s.getValue(), pp);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.NORTH;
			pp.layout.setConstraints(cp, gbc);
			pp.channelPanel.add(cp);
			i++;
		}
		
		pp.revalidate();
		pp.repaint();
		
	}
	
	
	
	/**
	 * 画面左側に表示させるパネル
	 */
	public class BaseInfoPanel extends JPanel {
		
		
		public BaseInfoPanel() {
			super();
			this.setBounds(0, 0, SwingGUITrafficController.this.width / 4, SwingGUITrafficController.this.height);
			this.setBackground(Color.WHITE);
			
			// レイアウトを指定する（行配置を試みるためBoxLayoutにする）
			this.setLayout(null);
			
			// 制御機名称
			JLabel nameLabel = new JLabel("制御機名称(ENTERで反映)");
			JTextField nameField = new JTextField(32);
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
			
			// 色
			JButton colorButton = new JButton();
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
			
			// 半径
			JLabel rangeLabelX = new JLabel("検知半径X(8-16がベスト)");
			rangeLabelX.setBounds(0, 75, this.getWidth(), 20);
			JTextField rangeFieldX = new JTextField(32);
			rangeFieldX.setBounds(0, 100, this.getWidth(), 20);
			rangeFieldX.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRangeX()));
			rangeFieldX.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							int range = Integer.parseInt(rangeFieldX.getText());
							if (range > 32) {
								// 大きいと検索に時間がかかる為警告を出す
								int check = JOptionPane.showConfirmDialog(SwingGUITrafficController.this, "推奨範囲を超えていますが設定してもいいですか？（一般に32を超えるとスペックの低いPCではカクカクする場合があります）", "推奨範囲を超えています", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
								if (check == JOptionPane.CANCEL_OPTION || check == JOptionPane.CLOSED_OPTION) {
									rangeFieldX.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRange()));
									return;
								}
							}
							SwingGUITrafficController.this.tc.setDetectRangeX(range);
							SwingGUITrafficController.this.setTitle(String.format("検知半径Xを「%d」に設定しました - 制御機「%s」の設定", range, tc.getName()));
						} catch (NumberFormatException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "検知半径は0より大きい整数（かつ2147483648より小さい数）で入力する必要があります。", "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "セットに失敗しました -> " + e2.getMessage() , "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
			JLabel rangeLabelY = new JLabel("検知半径Y(8-16がベスト)");
			rangeLabelY.setBounds(0, 125, this.getWidth(), 20);
			JTextField rangeFieldY = new JTextField(32);
			rangeFieldY.setBounds(0, 150, this.getWidth(), 20);
			rangeFieldY.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRangeY()));
			rangeFieldY.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							int range = Integer.parseInt(rangeFieldY.getText());
							if (range > 32) {
								// 大きいと検索に時間がかかる為警告を出す
								int check = JOptionPane.showConfirmDialog(SwingGUITrafficController.this, "推奨範囲を超えていますが設定してもいいですか？（一般に32を超えるとスペックの低いPCではカクカクする場合があります）", "推奨範囲を超えています", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
								if (check == JOptionPane.CANCEL_OPTION || check == JOptionPane.CLOSED_OPTION) {
									rangeFieldY.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRange()));
									return;
								}
							}
							SwingGUITrafficController.this.tc.setDetectRangeY(range);
							SwingGUITrafficController.this.setTitle(String.format("検知半径Yを「%d」に設定しました - 制御機「%s」の設定", range, tc.getName()));
						} catch (NumberFormatException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "検知半径は0より大きい整数（かつ2147483648より小さい数）で入力する必要があります。", "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "セットに失敗しました -> " + e2.getMessage() , "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
			JLabel rangeLabelZ = new JLabel("検知半径Z(8-16がベスト)");
			rangeLabelZ.setBounds(0, 175, this.getWidth(), 20);
			JTextField rangeFieldZ = new JTextField(32);
			rangeFieldZ.setBounds(0, 200, this.getWidth(), 20);
			rangeFieldZ.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRangeZ()));
			rangeFieldZ.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							int range = Integer.parseInt(rangeFieldZ.getText());
							if (range > 32) {
								// 大きいと検索に時間がかかる為警告を出す
								int check = JOptionPane.showConfirmDialog(SwingGUITrafficController.this, "推奨範囲を超えていますが設定してもいいですか？（一般に32を超えるとスペックの低いPCではカクカクする場合があります）", "推奨範囲を超えています", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
								if (check == JOptionPane.CANCEL_OPTION || check == JOptionPane.CLOSED_OPTION) {
									rangeFieldZ.setText(String.valueOf(SwingGUITrafficController.this.tc.getDetectRange()));
									return;
								}
							}
							SwingGUITrafficController.this.tc.setDetectRangeZ(range);
							SwingGUITrafficController.this.setTitle(String.format("検知半径Zを「%d」に設定しました - 制御機「%s」の設定", range, tc.getName()));
						} catch (NumberFormatException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "検知半径は0より大きい整数（かつ2147483648より小さい数）で入力する必要があります。", "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "セットに失敗しました -> " + e2.getMessage() , "セットできませんでした", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			
			// 制御機の設定を保存するボタン
			JButton saveButton = new JButton("ファイルに保存");
			saveButton.setBounds(0, this.getHeight() - 50, this.getWidth(), 20);
			saveButton.setToolTipText("サイクル、フェーズ、制御機の基本情報をファイルに保存します。");
			saveButton.addActionListener((e) -> {
				// どこに保存するか選択させる
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("保存する場所を選択してください");
				jfc.setAcceptAllFileFilterUsed(false); // 中身JSONだけど拡張子をGTSに変更する。それ以外開かないようにする
				jfc.setApproveButtonText("ここに保存");
				jfc.setFileFilter(new FileNameExtensionFilter("制御機設定ファイル（*.gts）", "gts"));
				jfc.setCurrentDirectory(GTS.GTSModDir);
				
				int res = jfc.showOpenDialog(this);
				if (res == JFileChooser.APPROVE_OPTION) {
					try {
						saveController(jfc.getSelectedFile());
						SwingGUITrafficController.this.setTitle(String.format("設定を保存しました - 制御機「%s」の設定", tc.getName()));
					} catch (IOException e2) {
						JOptionPane.showMessageDialog(SwingGUITrafficController.this, "保存に失敗しました -> " + e2.getMessage() , "保存できませんでした", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			
			// 制御機の設定を読み込むボタン
			JButton loadButton = new JButton("ファイルから読み込み");
			loadButton.setBounds(0, this.getHeight() - 25, this.getWidth(), 20);
			loadButton.setToolTipText("サイクル、フェーズ、制御機の基本情報をファイルから読み込みます。バージョンが違うとうまく読み込めない場合があります。");
			loadButton.addActionListener((e) -> {
				// どこにあるか選択させる
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("読み込む場所を選択してください");
				jfc.setAcceptAllFileFilterUsed(false); // 中身JSONだけど拡張子をGTSに変更する。それ以外開かないようにする
				jfc.setApproveButtonText("ここから読み込む");
				jfc.setFileFilter(new FileNameExtensionFilter("制御機設定ファイル（*.gts）", "gts"));
				jfc.setCurrentDirectory(GTS.GTSModDir);
				
				
				int res = jfc.showOpenDialog(this);
				if (res == JFileChooser.APPROVE_OPTION) {
					try {
						loadController(jfc.getSelectedFile());
						SwingGUITrafficController.this.setTitle(String.format("設定を読み込みました - 制御機「%s」の設定", tc.getName()));
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(SwingGUITrafficController.this, "読み込みに失敗しました -> " + e2.getMessage() + "\n" + Arrays.toString(e2.getStackTrace()).replace(",", "\n"), "保存できませんでした -> " + e2.getClass(), JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			
			
			// 全部追加
			this.add(nameLabel);
			this.add(nameField);
			this.add(colorButton);
			this.add(rangeFieldX);
			this.add(rangeLabelX);
			this.add(rangeFieldY);
			this.add(rangeLabelY);
			this.add(rangeFieldZ);
			this.add(rangeLabelZ);
			this.add(saveButton);
			this.add(loadButton);
		}
	}
	
	/**
	 * サイクルごとのパネル
	 */
	public class CyclePanel extends JPanel {
		
		private final Cycle cycle;
		
		private final JPanel phases;
		
		private final GridBagLayout layout = new GridBagLayout();
		
		public CyclePanel(Cycle cycle) {
			super();
			this.cycle = cycle;
			setOpaque(false);
			setBackground(Color.white);
			setBorder(new LineBorder(Color.BLACK, 1));
			setLayout(null);
			setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 500));
			setMinimumSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 500));
			
			// サイクル名称いれるとこ
			JLabel l1 = new JLabel("サイクル名称（ENTERで反映）");
			l1.setBounds(0, 0, SwingGUITrafficController.this.width / 4 * 3 - 50, 20);
			l1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 50, 20));
			add(l1);
			
			// フィールド
			JTextField t1 = new JTextField();
			t1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			t1.setBounds(5, 25, SwingGUITrafficController.this.width / 4 * 3 - 60, 20);
			t1.setText(cycle.getName());
			t1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						cycle.setName(t1.getText());
						SwingGUITrafficController.this.setTitle(String.format("サイクル名称を「%s」に変更しました - 制御機「%s」の設定", cycle.getName(), tc.getName()));
					}
				}
			});
			add(t1);
			
			// サイクルの条件（カスタムは後回し）
			JComboBox<String> c1 = new JComboBox<String>(new String[] {"【組込】常時実行可能（複数非推奨）", "【組込】昼間実行可能", "【組込】夜間実行可能", "【組込】実行可能時間指定（%1=開始 / %2=終了）"});
			c1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			c1.setBounds(5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 60) / 2, 20);
			add(c1);
			
			// 引数%1、%2の入力欄
			JTextField t2 = new JTextField();
			t2.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 60) / 4, 20));
			t2.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 60) / 2 + 5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 60) / 4, 20);
			t2.setToolTipText("%1");
			
			add(t2);
			
			JTextField t3 = new JTextField();
			t3.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 60) / 4, 20));
			t3.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 60) / 4 * 3 + 5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 60) / 4, 20);
			t3.setToolTipText("%2");
			add(t3);
			
			// 後からじゃないと追加できない
			t2.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						applyArgs(t2.getText(), t3.getText());
					}
				}
			});
			t3.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						applyArgs(t2.getText(), t3.getText());
					}
				}
			});
			
			// CycleBaseの状況などによって上記のパラメーターを入れ替える
			if (cycle instanceof TimeCycle) {
				TimeCycle tc = (TimeCycle) cycle;
				if (tc.getFrom() == 0 && tc.getTo() == 24000) {
					// 常時実行可能
					c1.setSelectedIndex(0);
				}
				else if (tc.getFrom() == 6000 && tc.getTo() == 18000) {
					// 昼間実行可能
					c1.setSelectedIndex(1);
				}
				else if (tc.getFrom() == 18000 && tc.getTo() == 30000) {
					// 夜間実行可能
					c1.setSelectedIndex(2);
				}
				else {
					// 時間指定
					c1.setSelectedIndex(3);
					t2.setText(String.valueOf(tc.getFrom()));
					t3.setText(String.valueOf(tc.getTo()));
				}
			}
			
			// フェーズを追加するボタン
			JButton addPhaseButton = new JButton("フェーズ追加");
			addPhaseButton.setBounds(5, 75, SwingGUITrafficController.this.width / 4 * 3 - 70, 20);
			addPhaseButton.addActionListener((e) -> {
				Phase p = new PhaseBase(0);
				this.cycle.addPhase(p);
				refreshPhases(this);
			});
			add(addPhaseButton);
			
			// フェーズを実際に入れるパネル
			phases = new JPanel();
			phases.setBounds(0, 0, SwingGUITrafficController.this.width / 4 * 3 - 70, 120);
			phases.setLayout(layout);
			
			// フェーズを挿入
			refreshPhases(this);
			
			// フェーズを入れるスクロール可能パネル
			JScrollPane s1 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			s1.setBounds(5, 100, SwingGUITrafficController.this.width / 4 * 3 - 60, 360);
			s1.setViewportView(phases);
			add(s1);

			
			// サイクル削除ボタン
			JButton b1 = new JButton("このサイクルを削除する（戻せません！）");
			b1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			b1.setBounds(5, 470, SwingGUITrafficController.this.width / 4 * 3 - 60, 20);
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
		
		public void applyArgs(String from, String to) {
			if (this.cycle instanceof TimeCycle) {
				TimeCycle tc = (TimeCycle) cycle;
				// 引数チェック
				int fromi = 0;
				int toi = 0;
				try {
					fromi = Integer.parseInt(from);
					toi = Integer.parseInt(to);
				} catch (NumberFormatException e) {
					// 整数じゃない文字列が入ってきた場合
					JOptionPane.showMessageDialog(SwingGUITrafficController.this, "整数を入力してください！", "入力値が不正です", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				try {
					tc.setFrom(fromi);
					tc.setTo(toi);
				} catch (IllegalArgumentException e) {
					// セットできない値を設定した場合
					JOptionPane.showMessageDialog(SwingGUITrafficController.this, "値の変更に失敗しました: " + e.getMessage(), "入力値が不正です", JOptionPane.ERROR_MESSAGE);
					return;
				}
				setTitle(String.format("サイクル「%s」はMinecraft時間%d～%dの間に実行可能となります - 制御機「%s」の設定", cycle.getName(), fromi, toi, tc.getName()));
			}
		}
		
		
	}
	
	public class PhasePanel extends JPanel {
		
		private final Cycle cycle;
		private Phase phase;
		
		private final GridBagLayout layout = new GridBagLayout();
		private final JPanel channelPanel;
		
		public PhasePanel(Cycle cycle, Phase phaseIn) {
			super();
			this.cycle = cycle;
			this.phase = phaseIn;
			setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 70, 225));
			setMinimumSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 70, 225));
			setBackground(new Color(220, 255, 220));
			setBorder(new LineBorder(Color.black, 1));
			setLayout(null);
			
			// ラベルとしてフェーズ名称入れる
			JLabel l1 = new JLabel("フェーズ名称（ENTERで反映）");
			l1.setBounds(5, 0, SwingGUITrafficController.this.width / 4 * 3 - 70, 20);
			l1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 70, 20));
			add(l1);
			
			// フェーズ名称フィールド
			JTextField t1 = new JTextField();
			t1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			t1.setBounds(5, 25, SwingGUITrafficController.this.width / 4 * 3 - 60, 20);
			t1.setText(phase.getName());
			t1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						phase.setName(t1.getText());
						SwingGUITrafficController.this.setTitle(String.format("フェーズ名称を「%s」に変更しました - 制御機「%s」の設定", phase.getName(), tc.getName()));
					}
				}
			});
			add(t1);
			
			// フェーズ秒数
			JLabel l2 = new JLabel("表示Tick");
			l2.setBounds(5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 70) / 8, 20);
			l2.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 70) / 8, 20));
			add(l2);
			
			// フェーズ名称フィールド
			JTextField t2 = new JTextField();
			t2.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 70) / 4, 20));
			t2.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 70) / 8 + 5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 70) / 8, 20);
			if (phase instanceof PhaseBase) {
				t2.setText(String.valueOf(((PhaseBase) phase).getContinueTick()));
			}
			else if (phase instanceof  UntilDetectPhase) {
				t2.setText(String.valueOf(((UntilDetectPhase) phase).getWaitTick()));
			}
			t2.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (phase instanceof  PhaseBase) {
							try {
								long tick = Long.parseLong(t2.getText());
								if (tick <= 0) {
									throw new NumberFormatException();
								}
								((PhaseBase) phase).setContinueTick(Math.toIntExact(tick));
							} catch (NumberFormatException e2) {
								JOptionPane.showMessageDialog(SwingGUITrafficController.this, "正の整数を入力してください！", "入力値が不正です", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						SwingGUITrafficController.this.setTitle(String.format("フェーズTickを「%s」に変更しました - 制御機「%s」の設定", t2.getText(), tc.getName()));
					}
				}
			});
			add(t2);
			
			// フェーズの種類
			JComboBox<String> c1 = new JComboBox<>(new String[] {"固定間隔（Tickに現示時間を入力）", "検知信号を受信するまで（Tickに最低待ち時間を入力）"});
			c1.setSelectedIndex((phase instanceof PhaseBase) ? 0 : (phase instanceof UntilDetectPhase) ? 1 : 0);
			c1.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 70) / 4, 20));
			c1.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 70) / 4 + 5, 50, (SwingGUITrafficController.this.width / 4 * 3 - 70) / 8 * 5, 20);
			c1.addActionListener((event) -> {
				int tick;
				try {
					tick = Integer.parseInt(t2.getText());
					if (tick <= 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(SwingGUITrafficController.this, "正の整数を入力してください！", "入力値が不正です", JOptionPane.ERROR_MESSAGE);
					c1.setSelectedIndex((phase instanceof PhaseBase) ? 0 : (phase instanceof UntilDetectPhase) ? 1 : 0);
					return;
				}
				// チャンネルをコピーしてPhaseのインスタンスを切り替える
				if (c1.getSelectedIndex() == 0 && !(phase instanceof PhaseBase)) {
					// 固定間隔でかつ現在のものが固定間隔以外だった場合
					PhaseBase p = new PhaseBase(tick);
					p.setChannels(phase.getChannels()); // チャンネルを代入（どうせ前のフェーズ消えるからシャローコピーでも問題ない）
					p.setTicks(phase.getTick()); // Tickを代入
					p.setName(phase.getName()); // んでフェーズ名称も代入
					
					for (int i = 0; i < cycle.getPhases().size(); i++) {
						if (cycle.getPhases().get(i).getName().equals(p.getName())) {
							cycle.getPhases().remove(i);
							cycle.getPhases().add(i, p); // こうしないと入れ替えられない
							break;
						}
					}
					
					this.phase = p; // 入れ替え
					
				}
				else if (c1.getSelectedIndex() == 1 && !(phase instanceof UntilDetectPhase)) {
					// 固定間隔でかつ現在のものが固定間隔以外だった場合
					UntilDetectPhase p = new UntilDetectPhase(tick);
					p.setChannels(phase.getChannels()); // チャンネルを代入（どうせ前のフェーズ消えるからシャローコピーでも問題ない）
					p.setTicks(phase.getTick()); // Tickを代入
					p.setName(phase.getName()); // んでフェーズ名称も代入
					
					for (int i = 0; i < cycle.getPhases().size(); i++) {
						if (cycle.getPhases().get(i).getName().equals(p.getName())) {
							cycle.getPhases().remove(i);
							cycle.getPhases().add(i, p); // こうしないと入れ替えられない
							break;
						}
					}
					
					this.phase = p; // 入れ替え
					
				}
			});
			add(c1);
			
			// チャンネル追加ボタン
			JButton addChannelButton = new JButton("チャンネル追加");
			addChannelButton.setBounds(5, 75, SwingGUITrafficController.this.width / 4 * 3 - 70, 20);
			addChannelButton.addActionListener((e) -> {
				long newChannel = 0;
				for (int i = 1; i < 2147483647; i++) {
					try {
						phase = phase.addChannelTry(i, new ConfigTrafficLight.LightObject().setName("<EMPTY>"));
					} catch (DataExistException e2) {
						continue;
					}
					newChannel = i;
					break;
				}
				
				refreshChannels(this);
				setTitle(String.format("チャンネル「%d」を追加しました - 制御機「%s」の設定", newChannel, tc.getName()));
			});
			add(addChannelButton);
			
			// フェーズ入れるPanel
			channelPanel = new JPanel();
			channelPanel.setLayout(layout);
			
			// チャンネル入れる
			refreshChannels(this);
			
			// ScrollPane
			JScrollPane s2 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			s2.setBounds(5, 100, SwingGUITrafficController.this.width / 4 * 3 - 90, 95);
			s2.setViewportView(channelPanel);
			add(s2);
			
			// フェーズ削除
			JButton b1 = new JButton("フェーズを削除する（元に戻せません）");
			b1.setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 60, 20));
			b1.setBounds(5, 200, SwingGUITrafficController.this.width / 4 * 3 - 90, 20);
			b1.addActionListener((e) -> {
				// ボタンが押されたらフェーズを削除する
				int i = 0;
				ArrayList<Phase> cs = cycle.getPhases();
				for (Phase p: cs) {
					if (p.getName().equals(phase.getName())) {
						cs.remove(i);
						break;
					}
					i++;
				}
				setTitle(String.format("フェーズ「%s」を削除しました - 制御機「%s」の設定", phase.getName(), tc.getName()));
				refreshCycles();
				
			});
			add(b1);
		}
		
		public Phase getPhase() {
			return phase;
		}
	}
	
	public class ChannelPanel extends JPanel {
		
		private final Phase phase;
		private ConfigTrafficLight.LightObject lightObject;
		private long channel;
		private final PhasePanel phasePanel;
		
		public ChannelPanel(Phase p, long key, ConfigTrafficLight.LightObject l, PhasePanel pp) {
			super();
			setPreferredSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 110, 30));
			setMinimumSize(new Dimension(SwingGUITrafficController.this.width / 4 * 3 - 110, 30));
			setBackground(new Color(220, 255, 255));
			setLayout(null);
			phase = p;
			lightObject = l;
			channel = key;
			phasePanel = pp;
			
			// チャンネル番号
			JLabel l1 = new JLabel("チャンネル番号");
			l1.setBounds(0, 0, (SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20);
			l1.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20));
			add(l1);
			
			// のフィールド
			JTextField t1 = new JTextField();
			t1.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20));
			t1.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5 + 5, 5, (SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20);
			t1.setText(String.valueOf(channel));
			t1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						long newChannel = 0;
						// チャンネル番号が既に存在する場合などを確認
						try {
							newChannel = Long.parseLong(t1.getText());
							if (channel == newChannel) return;
							phase.copyChannel(channel, newChannel);
							phase.getChannels().remove(channel);
							
						} catch (NumberFormatException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "整数を入力してください！", "入力値が不正です", JOptionPane.ERROR_MESSAGE);
							return;
						} catch (IllegalArgumentException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "値の変更に失敗しました: " + e2.getMessage(), "入力値が不正です", JOptionPane.ERROR_MESSAGE);
							return;
						}
						setTitle(String.format("フェーズ「%s」のチャンネル番号を「%d」から「%d」に変更しました - 制御機「%s」の設定", phase.getName(), channel, newChannel, tc.getName()));
						channel = newChannel;
					}
				}
			});
			add(t1);
			
			// LightObject名称
			JLabel l2 = new JLabel("LightObject名称");
			l2.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5 * 2 + 5, 0, (SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20);
			l2.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 120) / 5, 20));
			add(l2);
			
			// のフィールド
			JTextField t2 = new JTextField();
			t2.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 120) / 4, 20));
			t2.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5 * 3 + 5, 5, (SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20);
			t2.setText(l.getName());
			t2.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						ArrayList<ConfigTrafficLight.LightObject> lightObject = new ArrayList<>();
						// この制御機にアタッチする交通信号機全てをチェックして、LightObjectが存在するか探す
						// 一致するもの全部取ってくる
						for (TileEntityTrafficLight tetl: SwingGUITrafficController.this.tc.getTrafficLights()) {
							// ただしチャンネルが違うやつはなし
							if (tetl.getData().getSignal() != channel) continue;
							ConfigTrafficLight ctl = tetl.getAddon().getConfig();
							for (ConfigTrafficLight.LightObject l : ctl.getPatterns()) {
								System.out.println(l.getName());
								if (l.getName().equals(t2.getText())) {
									// 一致したオブジェクトがあった場合はそれを変更対象にする
									lightObject.add(l);
								}
								
							}
						}
						if (lightObject.isEmpty()) {
							// 見つからない
							JOptionPane.showMessageDialog(SwingGUITrafficController.this, "アタッチしている交通信号機の中にそのような名前のLightObjectは見つかりませんでした。先に信号機を置いてから編集してください。", "LightObjectが見つかりません", JOptionPane.ERROR_MESSAGE);
							return;
						}
						ChannelPanel.this.lightObject = lightObject.get(0);
						ChannelPanel.this.phase.addChannel(channel, ChannelPanel.this.lightObject);
						setTitle(String.format("フェーズ「%s」のチャンネル番号「%d」のLightObjectを「%s」に変更しました - 制御機「%s」の設定", phase.getName(), channel, t2.getText(), tc.getName()));
					}
				}
			});
			add(t2);
			
			// チャンネル削除ボタン
			JButton b1 = new JButton("削除");
			b1.setPreferredSize(new Dimension((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20));
			b1.setBounds((SwingGUITrafficController.this.width / 4 * 3 - 110) / 5 * 4 + 5, 5, (SwingGUITrafficController.this.width / 4 * 3 - 110) / 5, 20);
			b1.addActionListener((e) -> {
				// チャンネルを削除する
				this.phase.getChannels().remove(channel);
				refreshChannels(pp);
				setTitle(String.format("フェーズ「%s」のチャンネル番号「%d」を削除しました - 制御機「%s」の設定", phase.getName(), channel, tc.getName()));
			});
			add(b1);
		}
	}
	
	/**
	 * 指定されたファイルに制御機情報を保存する。
	 * 要はバックアップ取れるやつ。
	 * @param file
	 * @throws IOException
	 */
	public void saveController(File file) throws IOException {
		try (FileWriter fw = new FileWriter(file)) {
			fw.append(new GsonBuilder().setPrettyPrinting().setExclusionStrategies(new ExcludeJsonStrategy()).create().toJson(tc));
		}
	}
	
	/**
	 * 指定されたファイルから制御機情報を読み込む。
	 * バックアップからサイクルとフェーズを復元する。
	 * @param file
	 * @throws IOException
	 */
	public void loadController(File file) throws IOException, IllegalAccessException {
		try (FileReader fr = new FileReader(file)) {
			TrafficController tc = new GsonBuilder().setExclusionStrategies(new ExcludeJsonStrategy()).registerTypeAdapter(Phase.class, JSON_DESERIALIZER_PHASE).create().fromJson(fr, TrafficController.class);
			// 読み込んだ制御機のフィールドを反映させていく（ただし@Excludeがついているフィールドは対象外とする）
			// フィールド増える可能性があるのでリフレクション
			Class c = TrafficController.class; // クラスを読み込み
			for (Field f: c.getDeclaredFields()) {
				if (f.getAnnotation(Exclude.class) == null) {
					// このフィールドを指定する
					f.setAccessible(true); // 問答無用でtrue
					Class<?> type = f.getType(); // 型の情報を読み込み
					Object o = f.get(tc); // 一旦内容を読み込み
					f.set(this.tc, type.cast(o)); // 無理やり入れ込む
				}
			}
			
			refreshBase(); // 左側のパネル再読み込み
			refreshCycles(); // サイクル強制再読み込み
		}
	}
}
