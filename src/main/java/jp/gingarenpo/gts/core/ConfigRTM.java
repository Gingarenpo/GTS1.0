package jp.gingarenpo.gts.core;

import jp.gingarenpo.gts.light.ConfigTrafficLight;

import java.util.ArrayList;

/**
 * RTMと相互変換をするためのクラス。RTMの信号機に関する項目について、読み取って変換することができる。
 * つまり簡単な話、RTMのアドオンを使用したい場合にこちらを使用する。
 *
 * なお、ゲーム中にRTMのアドオンは読み込んでくれないので、自分でRTMのアドオンを変更する必要がある。
 * その際のアドオンの改造に関しては、アドオンの製作者に問い合わせてほしい（こちらは責任を負いません）
 *
 * また非公式のクラスなのでRTM製作者側からなんかあったらこのクラスは削除する予定。
 *
 * あとこれ信号機のみの対応なので。仕様変更されたら終わり・・・
 */
public class ConfigRTM {

	private String signalName; // 紐づけ: ID
	private String signalModel; // 紐づけ: モデル（mqoじゃないとダメだけど）
	private String signalTexture; // 紐づけ: BaseTex
	private String lightTexture; // 紐づけ: lightTex
	private String buttonTexture; // 現在これを紐づける場所が存在しない（予約済み）
	private ArrayList<String> lights; // 紐づけ: patterns、信号番号をサイクル名として登録する。I（点滅周期）が指定されているものは無視される）
	private boolean rotateBody; // 現在これは常に原点を中心にtrueになるため予約済み扱い
	private boolean smoothing; // 現在これを紐づける場所が存在しないしスムージングを行える技量がない
	private boolean doCulling; // 現在これを紐づける場所が存在しないし以下略
	
	/**
	 * GTSのパックに対応したコンフィグを、RTMのJSON形式に変換して返す。こちらはあまり用途が見いだせないが一応予約。
	 * modelPartsFixtureとかオブジェクトインナーで作るのめんどくさいので今のところ非推奨メソッド（中身なし）
	 * なお、いくつか制約あり。
	 * > booleanで指定するタイプの大半は未対応なので、デフォルトですべてtrueとなる。
	 * > 全て回転するパーツとして扱われる。
	 * > サイクル変換時、名前を保持できないため適当に番号が1から順番に振られる。
	 * > buttonTextureが空になる。
	 * @param gts 変換したいGTSのコンフィグ。
	 * @return ※現在はnullしか返さないので意味がありません
	 * @deprecated 使わないでね
	 */
	public static ConfigRTM GTS2RTM(ConfigTrafficLight gts) {
		return null;
	}
	
	/**
	 * このインスタンスのRTMコンフィグを、GTSのコンフィグに変換して返す。上記コメントに描いたような制約で変換される。
	 * 途中で値がおかしい場合など、何らかのエラーが発生した場合はnullを返す。
	 * @return GTSのコンフィグに変換した結果。正常に変換できなかった場合はnullを返す
	 */
	public ConfigTrafficLight RTM2GTS() {
		ConfigTrafficLight res = null;
		
		// まず値チェック（だけど今は特にない）
		// サイクルパターン変換
		for (String light: lights) {
			// S(n) I(n) P(a b c d e...)となっている
			// Todo: ここいずれやろうね
		}
		
		return res;
	}
	
	// 以下GetterとSetter
	public String getSignalName() {
		return signalName;
	}
	
	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}
	
	public String getSignalModel() {
		return signalModel;
	}
	
	public void setSignalModel(String signalModel) {
		this.signalModel = signalModel;
	}
	
	public String getSignalTexture() {
		return signalTexture;
	}
	
	public void setSignalTexture(String signalTexture) {
		this.signalTexture = signalTexture;
	}
	
	public String getLightTexture() {
		return lightTexture;
	}
	
	public void setLightTexture(String lightTexture) {
		this.lightTexture = lightTexture;
	}
	
	public String getButtonTexture() {
		return buttonTexture;
	}
	
	public void setButtonTexture(String buttonTexture) {
		this.buttonTexture = buttonTexture;
	}
	
	public ArrayList<String> getLights() {
		return lights;
	}
	
	public void setLights(ArrayList<String> lights) {
		this.lights = lights;
	}
	
	public boolean isRotateBody() {
		return rotateBody;
	}
	
	public void setRotateBody(boolean rotateBody) {
		this.rotateBody = rotateBody;
	}
	
	public boolean isSmoothing() {
		return smoothing;
	}
	
	public void setSmoothing(boolean smoothing) {
		this.smoothing = smoothing;
	}
	
	public boolean isDoCulling() {
		return doCulling;
	}
	
	public void setDoCulling(boolean doCulling) {
		this.doCulling = doCulling;
	}
}
