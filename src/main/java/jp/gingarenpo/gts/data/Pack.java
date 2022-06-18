package jp.gingarenpo.gts.data;


import java.io.File;
import java.util.ArrayList;

/**
 * アドオンの詳細内容などを保持するクラス。GTS.txtから取得できる内容しか格納していません
 */
public class Pack {
	
	private String name; // GTS.txtで記載があればリストにこの名前が表示されたりする
	private String credit; // こちらもGTS.txt
	private ArrayList<Model> models = new ArrayList<Model>(); // モデルセット
	private File location; // ファイルの場所
	
	
	public Pack(String name, String credit, ArrayList<Model> models, File location) {
		this.name = name;
		this.credit = credit;
		this.models = models;
		this.location = location;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCredit() {
		return credit;
	}
	
	public void setCredit(String credit) {
		this.credit = credit;
	}
	
	public ArrayList<Model> getModels() {
		return models;
	}
	
	public File getLocation() {
		return location;
	}
	
	public void setLocation(File location) {
		this.location = location;
	}
	
	/**
	 * 指定したIDと一致するモデルデータを返す。ない場合はnull
	 * @param id
	 * @return
	 */
	public Model getModel(String id) {
		for (Model model: models) {
			// アドオンのモデルの中からコンフィグデータを読み込みidと一致するものを返す
			if (model.getConfig().getId().equals(id)) return model;
		}
		return null;
	}
	
	public void setModels(ArrayList<Model> models) {
		this.models = models;
	}
}
