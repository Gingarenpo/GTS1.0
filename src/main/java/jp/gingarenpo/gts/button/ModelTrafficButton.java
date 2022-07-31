package jp.gingarenpo.gts.button;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;
import net.minecraft.util.ResourceLocation;

import java.io.File;

/**
 * 押ボタン箱のモデル。と言っても、保持するのは基本的にModelBaseと同じである。
 */
public class ModelTrafficButton extends ModelBase<ConfigTrafficButton> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * テクスチャの変更が必要な場合にフラグが立つ。
	 */
	private boolean needChangeTex;
	
	/**
	 * 指定した形式でモデルを初期化する。
	 * @param config コンフィグ
	 * @param model モデル
	 * @param file パックファイル
	 */
	public ModelTrafficButton(ConfigTrafficButton config, MQO model, File file) {
		super(config, model, file);
	}
	
	/**
	 * テクスチャを読み込みなおす。
	 */
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
			if (!(m instanceof ModelTrafficButton)) continue;
			if (m.equals(this)) {
				// System.out.println(((ModelTrafficLight) m).config.getTextures().getBase());
				// mのテクスチャを読み込む
				this.getConfig().setBaseTexture(((ModelTrafficButton) m).getConfig().getBaseTexture());
				this.getConfig().setPushTexture(((ModelTrafficButton) m).getConfig().getPushTexture());
				this.getConfig().setBaseTex(((ModelTrafficButton) m).getConfig().getBaseTex());
				this.getConfig().setPushTex(((ModelTrafficButton) m).getConfig().getPushTex());
				
				
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
	
	/**
	 * この押ボタンが押されたときのサウンドリソースを返す。
	 * @return
	 */
	public ResourceLocation getSoundLocation() {
		return new ResourceLocation(GTS.MOD_ID, config.getSoundPath());
	}
}
