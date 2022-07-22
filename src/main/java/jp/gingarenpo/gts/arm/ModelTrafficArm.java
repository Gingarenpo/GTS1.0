package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.core.ModelBase;

/**
 * 注意：開始部分のオブジェクトを指定されたサイズで正規化します
 * Baseの部分は必要に応じて座標をX方向に勝手に拡大します（なので伸びても支障がない感じで）
 */
public class ModelTrafficArm extends ModelBase<ConfigTrafficArm> {
	
	public ModelTrafficArm(ConfigTrafficArm config, MQO model) {
		super();
		this.config = config;
		this.model = model;
		if (config != null) {
			this.model = model.normalize(config.getSize(), config.startObject);
			
		}
	}
}
