package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.core.ModelBase;

import java.io.Serializable;

/**
 * ポールのモデルクラス。特に差し替えるべき場所はない。
 */
public class ModelTrafficPole extends ModelBase<ConfigTrafficPole> implements Serializable {
	public ModelTrafficPole(ConfigTrafficPole config, MQO model) {
		super();
		this.config = config;
		this.model = model;
		if (config != null) {
			model.normalize(1);
			
		}
	}
	
	@Override
	public String toString() {
		return this.model + " / " + this.config;
	}
}
