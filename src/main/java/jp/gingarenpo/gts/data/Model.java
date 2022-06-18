package jp.gingarenpo.gts.data;

import jp.gingarenpo.gingacore.mqo.MQO;

import javax.imageio.ImageIO;
import java.io.Serializable;

/**
 * モデルセットの一覧。パックには複数あることがあるよ
 */
public class Model implements Serializable {
	
	private ConfigBase config; // コンフィグ
	private MQO model; // モデル

	
	public Model(ConfigBase config, MQO model) {
		this.config = config;
		this.model = model;
		if (config != null) {
			model.normalize(config.getSize());
		}
	}
	
	public ConfigBase getConfig() {
		return config;
	}
	
	public void setConfig(ConfigBase config) {
		this.config = config;
	}
	
	public MQO getModel() {
		return model;
	}
	
	public void setModel(MQO model) {
		this.model = model;
	}
	
	@Override
	public String toString() {
		return "Model{" +
					   "config=" + config +
					   ", model=" + model +
					   '}';
	}
}
