package jp.gingarenpo.gts.core;

import java.io.Serializable;

/**
 * どのコンフィグにも基本的に用意する中身。
 */
public abstract class ConfigBase implements Serializable {
	
	protected String id; // コンフィグ同士で重複してはならない固有のID。基本的に全角バイトは含めないこと。
	protected String model; // モデルパスを代入する。パックのZIPファイルからの相対パスで指定すること。
	protected float size = 1; // このモデルを表示するためのサイズ。ブロック単位で指定する。モデルの大きさがこのサイズに収まるように縮小されて描画される。
	
	public ConfigBase() {}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public float getSize() {
		return size;
	}
	
	public void setSize(float size) {
		this.size = size;
	}
}
