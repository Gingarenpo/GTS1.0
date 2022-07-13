package jp.gingarenpo.gts.core;

import jp.gingarenpo.gingacore.mqo.MQO;

import java.io.Serializable;

/**
 * モデルの原型となるクラス。このクラス自体はインスタンスを作成できないためサブクラスを利用する。
 * Configクラスを入れる
 * @param <T> Configクラス
 */
public abstract class ModelBase<T extends ConfigBase> implements Serializable {
	
	/**
	 * コンフィグクラス。
	 */
	protected T config;
	
	/**
	 * モデル。
	 */
	protected MQO model;
	
	protected  ModelBase() {}
	
	public ModelBase(T config, MQO model) {
		this.config = config;
		this.model = model;
		if (config != null) {
			this.model = model.normalize(config.getSize());
			
		}
	}
	
	public T getConfig() {
		return config;
	}
	
	public void setConfig(T config) {
		this.config = config;
	}
	
	public MQO getModel() {
		return model;
	}
	
	public void setModel(MQO model) {
		this.model = model;
	}
}
