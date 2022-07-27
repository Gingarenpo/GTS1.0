package jp.gingarenpo.gts.light;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.ModelBase;
import jp.gingarenpo.gts.pack.Pack;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * モデルセットの一覧。パックには複数あることがあるよ
 */
public class ModelTrafficLight extends ModelBase<ConfigTrafficLight> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * テクスチャの変更を通知するフラグ
	 */
	private boolean needChangeTex = false;
	
	/**
	 * 指定したコンフィグ、指定したモデル、指定したファイルを用いてこのモデルを初期化する。
	 * 正規化処理は行わないが、代わりにモデルをリロードしようと試みる。
	 *
	 * @param config コンフィグ
	 * @param model モデル
	 * @param file パック
	 */
	public ModelTrafficLight(ConfigTrafficLight config, MQO model, File file) {
		this(config, model, file, false);
		this.reloadModel();
	}
	
	/**
	 * 指定したコンフィグ、指定したモデル、指定したファイルを用いてこのモデルを初期化する。
	 * normalize処理が割と重く、forで総当たりパック検索したほうが早かったりするので
	 * Loaderから初回読み込み時にはモデルをnormalizeするためにtrueとする。その他の場合はfalseとして
	 * normalizeをおこなわないようにすることで高速化を図る。
	 *
	 * 何も指定しないとfalseを指定したものとして扱う（オーバーロードしてある）
	 * @param config モデルのコンフィグ
	 * @param model モデルの実体
	 * @param file パックファイル
	 * @param normalize 正規化する場合はtrue
	 */
	public ModelTrafficLight(ConfigTrafficLight config, MQO model, File file, boolean normalize) {
		this.config = config;
		this.file = file;
		
		// 描画対象オブジェクトのみを対象として正規化をかける
		ArrayList<String> objects = new ArrayList<>();
		objects.addAll(config.getBody());
		objects.addAll(config.getLight());
		this.model = model;
		
		if (normalize) {
			this.model = model.normalize(config.getSize(), objects);
		}
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
	
	/**
	 * テクスチャの所在を再読み込みする。
	 * あくまで保持するのはテクスチャ座標であり、実際のテクスチャのリソースロケーションはクライアント側で保持するべき。
	 */
	public void reloadTexture() {
		if (isDummy()) {
			GTS.GTSLog.debug("This is dummy model. shouldn't reload texture.");
			return;
		}
		
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
			if (!(m instanceof ModelTrafficLight)) continue;
			if (m.equals(this)) {
				// System.out.println(((ModelTrafficLight) m).config.getTextures().getBase());
				// mのテクスチャを読み込む
				this.getConfig().getTextures().setBase(((ModelTrafficLight) m).config.getTextures().getBase());
				this.getConfig().getTextures().setLight(((ModelTrafficLight) m).config.getTextures().getLight());
				this.getConfig().getTextures().setNoLight(((ModelTrafficLight) m).config.getTextures().getNoLight());
				this.getConfig().getTextures().setBaseTex(GTS.loader.getTexture(file, this.getConfig().getTextures().getBase()));
				this.getConfig().getTextures().setLightTex(GTS.loader.getTexture(file, this.getConfig().getTextures().getLight()));
				this.getConfig().getTextures().setNoLightTex(GTS.loader.getTexture(file, this.getConfig().getTextures().getNoLight()));
				
				this.needChangeTex = true;
				return;
			}
		}
		
		GTS.GTSLog.warn("Warning. model cannot found.");
	}
	
	@Override
	public String toString() {
		return "ModelTrafficLight{" +
					   "config=" + config +
					   ", model=" + model +
					   ", file=" + file +
					   '}';
	}
	
	/**
	 * 登録されているコンフィグがダミー用かどうかを判別する。
	 * @return ダミーならtrue
	 */
	public boolean isDummy() {
		return config instanceof DummyConfigTrafficLight;
	}
	
//	/**
//	 * ResourceLocationをそのままでは読み込めないので、文字列に変換する形で送る。
//	 * デフォルトのシリアライズに追記する形で読み出す。transientつけているので
//	 * デフォルトでは書き込まれないはず。
//	 * @param oos ストリーム
//	 */
//	private void writeObject(ObjectOutputStream oos) throws IOException {
//		oos.defaultWriteObject(); // とりあえず書き込んでもらう
//		// 3つのリソースを書き込むがない場合は「<null>」と書き込む
//		System.out.println(baseTex);
//		oos.writeUTF(baseTex == null ? "<null>" : baseTex.getPath());
//		oos.writeUTF(lightTex == null ? "<null>" : lightTex.getPath());
//		oos.writeUTF(noLightTex == null ? "<null>" : noLightTex.getPath());
//		// 終わり
//	}
//
//	/**
//	 * 自前でwriteしたら自前でreadしてあげないと
//	 * @param ois
//	 * @throws IOException
//	 */
//	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
//		ois.defaultReadObject(); // とりあえず読み込んでもらう
//		// 3つのリソースを読み込むが、<null>だった場合はnullを入れる（入れなくてもいいだろうけど明示的に）
//		String bp = ois.readUTF();
//		String lp = ois.readUTF();
//		String np = ois.readUTF();
//
//		System.out.println(bp);
//
//		baseTex = !bp.equals("<null>") ? new ResourceLocation(bp) : null;
//		lightTex = !lp.equals("<null>") ? new ResourceLocation(lp) : null;
//		noLightTex = !np.equals("<null>") ? new ResourceLocation(np) : null;
//
//		// 終わり
//	}
}
