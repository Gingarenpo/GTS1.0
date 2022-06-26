package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.core.ModelBase;

import java.io.Serializable;

/**
 * モデルセットの一覧。パックには複数あることがあるよ
 */
public class ModelTrafficLight extends ModelBase<ConfigTrafficLight> implements Serializable {
	
	public ModelTrafficLight(ConfigTrafficLight config, MQO model) {
		super(config, model);
	}
}
