package jp.gingarenpo.gts.core;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

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
	protected MQO model; // 重いのでこれはシリアライズ非対象
	
	/**
	 * パックのロケーション。
	 */
	protected File file;
	
	protected  ModelBase() {}
	
	public ModelBase(T config, MQO model, File file) {
		this.config = config;
		this.model = model;
		this.file = file;
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
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * IDが同一であれば同じインスタンスとみなす
	 * @param o
	 * @return
	 */
	public boolean equals(ModelBase o) {
		System.out.printf("%s = %s%n", o.config.id, this.config.id);
		return (Objects.equals(o.config.id, this.config.id));
	}
	
	/**
	 * モデルをパックから再読み込みする
	 */
	public void reloadModel() {
		// Loaderからモデル読み込み
		if (GTS.loader == null || GTS.loader.getPacks() == null || file == null) {
			return;
		}
		for (ModelBase m: GTS.loader.getPacks().get(file).getModels()) {
			if (m.getConfig().getModel().equals(getConfig().getModel())) {
				// 同じモデルがあれば
				this.model = m.model;
			}
		}
	}
	
	@Override
	public String toString() {
		return "ModelBase{" +
					   "config=" + config +
					   ", model=" + model +
					   ", file=" + file +
					   '}';
	}
}
