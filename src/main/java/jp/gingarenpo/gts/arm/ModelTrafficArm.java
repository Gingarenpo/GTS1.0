package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;

/**
 * 注意：開始部分のオブジェクトを指定されたサイズで正規化します
 * Baseの部分は必要に応じて座標をX方向に勝手に拡大します（なので伸びても支障がない感じで）
 */
public class ModelTrafficArm extends ModelBase<ConfigTrafficArm> {
	
	private static final long serialVersionUID = 1L;
	
	private boolean needChangeTex;
	
	public ModelTrafficArm(ConfigTrafficArm config, MQO model, File pack) {
		super();
		this.config = config;
		this.model = model;
		this.file = pack;
		if (config != null) {
			this.model = model.normalize(config.getSize(), config.startObject);
			
		}
	}
	
	@Override
	public void reloadTexture() {
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
			if (!(m instanceof ModelTrafficArm)) continue;
			if (m.equals(this)) {
				// mのテクスチャを読み込む
				this.getConfig().setTexImage(((ModelTrafficArm)m).getConfig().getTexImage());
				
				
				this.needChangeTex = true;
				return;
			}
		}
		
		GTS.GTSLog.warn("Warning. model cannot found.");
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
	
	@Override
	public String toString() {
		return "ModelTrafficArm{" +
					   "needChangeTex=" + needChangeTex +
					   ", config=" + config +
					   ", model=" + model +
					   ", file=" + file +
					   '}';
	}
	
	/**
	 * TileEntityを持たないのでダミーを獲得する手段がないので、その代わりで用意している
	 * @return
	 * @throws IOException
	 */
	public static ModelTrafficArm getDummyModel() throws IOException {
		return new ModelTrafficArm(new DummyConfigTrafficArm(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_ta.mqo")).getInputStream()), null);
	}
}
