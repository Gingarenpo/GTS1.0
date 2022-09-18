package jp.gingarenpo.gts.sign.gui;

import jp.gingarenpo.gts.sign.data.NamedTrafficSign;
import jp.gingarenpo.gts.sign.data.TrafficSign;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 地名板とか時差式信号機とかの表示板デザインを保存するための物
 * SwingのGUIによって地名板を作成することができる
 * 基本的に右クリックでGUIを起動する設定。
 */
public class SwingGUITrafficSign extends JFrame {
	
	JLabel preview = new JLabel();
	
	/**
	 * ※単独動作確認用
	 * @param args
	 */
	public static void main(String[] args) {
		SwingGUITrafficSign a = new SwingGUITrafficSign(new NamedTrafficSign());
		a.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		a.setVisible(true);
	}
	
	public TrafficSign data;
	
	/**
	 * 指定したTileEntityでGUIを作成する
	 * @param data
	 */
	public SwingGUITrafficSign(TrafficSign data) {
		this.data = data;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 閉じたら破棄する
		this.getContentPane().setPreferredSize(new Dimension(1280, 540));
		this.pack();
		this.setTitle("看板を設定");
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setAlwaysOnTop(true);
		//this.setResizable(false);
		init();
		this.pack();
	}
	
	private void init() {
		// 左側にプレビュー画面を表示する
		preview.setBounds(0, 0, this.getWidth() / 2, this.getHeight());
		Color c = (Color) data.getValue("color");
		preview.setBackground(new Color(255-c.getRed(), 255 - c.getGreen(), 255-c.getBlue()));
		preview.setOpaque(true);
		refreshPreview();
		this.add(preview);
		
		// 右側にプロパティ一覧
		JScrollPane pp = new JScrollPane();
		pp.setBounds(this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight());
		
		JPanel ppp = new JPanel();
		ppp.setSize(this.getWidth(), this.getHeight());
		ppp.setLayout(null);
		
		HashMap<String, JComponent> pps = new HashMap<>();
		
		int i = 0;
		for (Map.Entry<String, Serializable> o:data.getConfig4Loop()) {
			if (o.getValue() instanceof String) {
				// テキストエリアとして追加
				JLabel l = new JLabel(o.getKey());
				l.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 20, 20);
				pps.put(o.getKey() + "_l", l);
				ppp.add(l);
				i++;
				JTextField t = new JTextField();
				t.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 20, 20);
				t.setText((String) o.getValue());
				t.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
						data.setValue(o.getKey(), t.getText());
						SwingGUITrafficSign.this.setTitle(o.getKey() + "を変更");
						refreshPreview();
					}
				});
				pps.put(o.getKey(), t);
				ppp.add(t);
			}
			else if (o.getValue() instanceof Float) {
				// テキストエリアとして追加（ただし整数）
				JLabel l = new JLabel(o.getKey());
				l.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 15, 20);
				pps.put(o.getKey() + "_l", l);
				ppp.add(l);
				i++;
				JTextField t = new JTextField();
				t.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 15, 20);
				t.setText(String.valueOf(o.getValue()));
				t.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
						try {
							Float.parseFloat(t.getText());
						} catch (NumberFormatException e2) {
							JOptionPane.showMessageDialog(SwingGUITrafficSign.this, "数値で入力してください!", "値エラー", JOptionPane.ERROR_MESSAGE);
							return;
						}
						data.setValue(o.getKey(), Float.valueOf(t.getText()));
						SwingGUITrafficSign.this.setTitle(o.getKey() + "を変更");
						refreshPreview();
					}
				});
				pps.put(o.getKey(), t);
				ppp.add(t);
			}
			else if (o.getValue() instanceof Boolean) {
				// チェックボックスとして追加
				JCheckBox a = new JCheckBox(o.getKey(), (Boolean) o.getValue());
				a.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 15, 20);
				a.addActionListener(e -> {
					data.setValue(o.getKey(), a.isSelected());
					SwingGUITrafficSign.this.setTitle(o.getKey() + "を変更");
					refreshPreview();
				});
				pps.put(o.getKey(), a);
				ppp.add(a);
				
			}
			else if (o.getValue() instanceof Color) {
				// カラーチューザーとして追加
				JButton b = new JButton(o.getKey() + " - click button to change color");
				b.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 15, 20);
				b.addActionListener(e -> {
					Color co = JColorChooser.showDialog(SwingGUITrafficSign.this, String.format("%s の色を選択", o.getKey()), (Color) o.getValue());
					if (co != null) {
						data.setValue(o.getKey(), co);
						SwingGUITrafficSign.this.setTitle(o.getKey() + "を変更");
						refreshPreview();
					}
				});
				pps.put(o.getKey(), b);
				ppp.add(b);
				
			}
			else if (o.getValue() instanceof Font || o.getValue() == null) {
				// フォントダイアログ用ボタンとして追加
				JButton b = new JButton(o.getKey() + " - click button to change font");
				b.setBounds(5, 5 + 20 * (i), this.getWidth() / 2 - 15, 20);
				b.addActionListener(e -> {
					// フォントダイアログは削除されてしまっているらしいので自前で作る
					JComboBox<String> list = new JComboBox<>();
					list.addItem("選択してください");
					Font[] fs = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
					for (Font ff: fs) {
						list.addItem(ff.getName());
					}
					
					
					JOptionPane.showMessageDialog(SwingGUITrafficSign.this, list, "Choose a font", JOptionPane.QUESTION_MESSAGE);
					if (list.getSelectedIndex() != 0) {
						// 反映
						data.setValue(o.getKey(), new Font((String) list.getSelectedItem(), Font.PLAIN, 512) );
						SwingGUITrafficSign.this.setTitle(o.getKey() + "を変更（→" + data.getValue(o.getKey()) + "）");
						refreshPreview();
					}
				});
				pps.put(o.getKey(), b);
				ppp.add(b);
				
			}
			i++;
		}
		
		pp.setViewportView(ppp);
		
		this.add(pp);
	}
	
	private void refreshPreview() {
		BufferedImage tex = data.createMainTexture();
		Image i = null;
		if (tex.getWidth() >= tex.getHeight()) {
			i = tex.getScaledInstance(this.getWidth() / 2, -1, Image.SCALE_SMOOTH);
		}
		else {
			i = tex.getScaledInstance(-1, this.getHeight() - 64, Image.SCALE_SMOOTH);
		}
		preview.setIcon(new ImageIcon(i));
		setTitle(getTitle() + "(プレビュー作成済み)");
	}
	
	/**
	 * 最新のデータを返却する。
	 * @return
	 */
	public TrafficSign getData() {
		return data;
	}
	

}
