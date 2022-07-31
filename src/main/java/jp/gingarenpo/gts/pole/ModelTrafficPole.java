package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;

import java.io.File;
import java.io.Serializable;

/**
 * ポールのモデルクラス。特に差し替えるべき場所はない。
 */
public class ModelTrafficPole extends ModelBase<ConfigTrafficPole> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private boolean needChangeTex;
	
	public ModelTrafficPole(ConfigTrafficPole config, MQO model, File file) {
		super(config, model, file);
		this.config = config;
		this.model = model;
		if (config != null) {
			this.model = model.normalize(1);
			
		}
	}
	
	@Override
	public String toString() {
		return this.model + " / " + this.config;
	}
	
	@Override
	public void reloadTexture() {
		if (GTS.loader == null) {
			GTS.GTSLog.warn("Warning. loader is not ready.");
			return;
		}
		if (file == null) {
			GTS.GTSLog.warn("Warning. file is null.");
			return;
		}
		Pack p = GTS.loader.getPacks().get(file);
		// System.out.println(file);
		if (p == null) {
			GTS.GTSLog.warn("Warning. pack not found. Are the pack in the mods directory?");
			return;
		}
		
		for (ModelBase m: p.getModels()) {
			if (!(m instanceof ModelTrafficPole)) continue;
			if (m.equals(this)) {
				// System.out.println(((ModelTrafficLight) m).config.getTextures().getBase());
				// mのテクスチャを読み込む
				this.getConfig().setTexture(((ModelTrafficPole) m).getConfig().getTexture());
				this.getConfig().setTexImage(((ModelTrafficPole) m).getConfig().getTexImage());

				
				
				this.needChangeTex = true;
				return;
			}
		}
		
		GTS.GTSLog.warn("Warning. model cannot found.");
	}
	
	public boolean isNeedChangeTex() {
		return needChangeTex;
	}
	
	/**
	 * テクスチャの変更を伝えたときに実行する
	 */
	public void doneChangeTex() {
		needChangeTex = false;
	}
	
	
}
