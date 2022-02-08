package jp.gingarenpo.gts.core;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.controller.TrafficController;
import net.minecraft.entity.player.EntityPlayer;

import javax.swing.*;
import java.awt.*;

/**
 * 交通信号制御機と灯器でもう少なくとも2つはあるので
 */
public abstract class GTSSwingGUIBase<T> extends JPanel {
	
	protected T data = null; // 実体となるデータ
	protected EntityPlayer player; // プレイヤーを指定しないとGUIを閉じることができない
	protected GTSSwingGUIBase INSTANCE; // このインスタンス
	protected JFrame w; // ウィンドウ
	
	public GTSSwingGUIBase(JFrame w) {
		// 初期設定
		super();
		this.w = w;
		this.setLayout(null); // 絶対座標指定
		this.setBounds(0, 0, GTS.windowWidth, GTS.windowHeight);
		this.init();
		INSTANCE = this;
	}
	
	/**
	 * GUIを開いたプレイヤーをセットすることでGUIを閉じる動作を実装することができる
	 * @param player
	 */
	public void setPlayer(EntityPlayer player) {
		this.player = player;
	}
	
	/**
	 * 指定したデータをもとにGUIを更新する
	 * @param data
	 */
	public void update(T data) {
		// データをアタッチする
		this.data = data;
		this.updateGUI();
	}
	
	public T getData() {
		return data;
	}
	
	/**
	 * このGUIの初期設定を行う（パーツの追加など）。
	 * コンストラクタ内一番最後で実行するが、この時点ではデータが入っていないので中身を呼び出すのはその後じゃないときつい。
	 */
	public abstract void init();
	
	/**
	 * 現在指定されているデータをGUIの表示値に反映させるメソッド。initなどで実行されるので
	 * ここで値の反映を必ず行っておく必要がある。
	 */
	public abstract void updateGUI();
	
	/**
	 * Nullの場合は表示できないのでそれを判別するためのメソッド
	 * @return
	 */
	public boolean canShow() {
		return this.data != null;
	}
	
	/**
	 * GUIを閉じる際の処理。デフォルトでは単にプレイヤーのGUIを閉じて、自身の親ウィンドウを非表示にするだけ。
	 * なおパネルセットしていないのに呼び出すとエラー！
	 */
	public void onClose() {
		w.setVisible(false);
		player.closeScreen();
	}
	
	/**
	 * 指定したグリッドの座標と大きさで指定するメソッド。
	 * どこでも使用できるようにこちらで定義。
	 * @param g レイアウト
	 * @param c コンポーネント
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public static void addGrid(GridBagLayout g, GridBagConstraints gbc, Component c, int x, int y, int w, int h, double we) {
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.weightx = we;
		gbc.anchor = GridBagConstraints.CENTER;
		g.setConstraints(c, gbc);
	}
}
