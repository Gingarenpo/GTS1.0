package jp.gingarenpo.gts.core.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * モデルを変えるためのGUIダイアログの共通クラス。
 * このクラス自体は抽象クラスとなっているため、使用できない。
 * このクラスを継承したSwingのパネルを呼び出すことで、自動的に
 * 先頭にモデルの選択フィールドを作ってくれる。
 * クラスが多いが全部指定する。
 * 一致するモデルしか選択されなくなる。
 *
 * 抽象クラスにしているのは使用禁止にしているからであり、別に実装しないといけないメソッドはない。
 */
public abstract class GTSSwingGUIModelChoiceBase<T extends ModelBase, U extends GTSTileEntity, V> extends JFrame {
	
	/**
	 * ウィンドウ幅。
	 */
	public static final int width = 300;
	public static final int height = 200;
	
	/**
	 * モデルアドオンを格納しておくところ。
	 */
	public T model;
	
	/**
	 * TileEntityを格納しておくところ。更新時にここに更新を伝える。
	 */
	protected U te;
	
	/**
	 * その中の実際のデータを格納しておくところ。ちなみにあくまで予約しているだけで実際はこのクラスでアクセスしない。
	 */
	protected V data;
	
	/**
	 * 指定したパラメーターでGUIウィンドウを作成する。子クラスではこの3つを渡すコンストラクタだとめんどくさいので
	 * 適当にオーバーライドしておく。
	 * @param model
	 * @param te
	 * @param data
	 */
	public GTSSwingGUIModelChoiceBase(T model, U te, V data) {
		super();
		this.model = model;
		this.te = te;
		this.data = data;
	}
	
	/**
	 * ウィンドウを作成して待機状態に入る。この後、自由にフィールドを追加することができる。
	 * 登録後に実施する処理は引数の中で指定する（普遍的なのでここでは指定できない）
	 */
	protected GTSSwingGUIModelChoiceBase create(Runnable func) {
		this.getContentPane().setPreferredSize(new Dimension(width, height));
		this.pack();
		this.setAlwaysOnTop(true);
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// モデルパックの設定
		LinkedHashMap<String, T> models = new LinkedHashMap<>();
		models.put("【組込】DUMMY（通常選べません）", null);
		int index = 0;
		int i = 1;
		for (Pack pack: GTS.loader.getPacks().values()) {
			// パックの中からさらにモデルを引っ張り出す
			for (ModelBase model: pack.getModels()) {
				// Traffic Lightと一致するものを取り出す
				if (!this.instanceOf(model)) {
					continue;
				}
				
				models.put(getChoiceName(pack, (T) model), (T) model);
				
				if (Objects.equals(model, this.model)) {
					GTS.GTSLog.info("found.");
					index = i;
				}
				i++;
			}
		}
		
		
		// モデルパック選択肢を表示
		JComboBox<String> c1 = new JComboBox(models.keySet().toArray());
		c1.setBounds(5, 5, width - 10, 20);
		c1.setSelectedIndex(index);
		c1.addActionListener((e) -> {
			// そのモデルに変更する
			T model = models.get(c1.getSelectedItem());
			if (model == null) {
				return; // 何もしません
			}
			this.model = model;
			func.run();
		});
		this.getContentPane().add(c1);
		
		return this;
	}
	
	/**
	 * 選択肢に使用するためのパックとモデルの組み合わせにした一意の文字列を返す。
	 * @param pack
	 * @param model
	 * @return
	 */
	protected String getChoiceName(Pack pack, T model) {
		return String.format("【%s】%s", pack.getName(), model.getConfig().getId());
	}
	
	/**
	 * 選択肢の名前からモデルを探し当てて返す。なければnull
	 * @param name
	 * @return
	 */
	public static ModelBase getModelFromChoiceName(String name) {
		Matcher m = Pattern.compile("^【(.*)】(.*)$").matcher(name);
		if (!m.matches()) return null;
		try {
			String packName = m.group(1);
			String modelName = m.group(2);
			for (Pack pack: GTS.loader.getPacks().values()) {
				if (!pack.getName().equals(packName)) continue;
				// パックの中からさらにモデルを引っ張り出す
				for (ModelBase model: pack.getModels()) {
					// モデル名が一致していればOK
					if (model.getConfig().getId().equals(modelName)) return model;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return null;
	}
	
	protected boolean instanceOf(Object other) {
		if (other == null || model == null) return false;
		return (other.getClass() == model.getClass());
	}

}
