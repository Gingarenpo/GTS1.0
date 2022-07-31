package jp.gingarenpo.gts.pole.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.gui.GTSSwingGUIModelChoiceBase;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;
import jp.gingarenpo.gts.pole.ModelTrafficPole;
import jp.gingarenpo.gts.pole.TileEntityTrafficPole;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SwingGUITrafficPole extends GTSSwingGUIModelChoiceBase<ModelTrafficPole, TileEntityTrafficPole, Object> {
	
	private ItemStack is;
	
	/**
	 * 指定したTileEntityで初期化する
	 * @param tetp
	 */
	public SwingGUITrafficPole(TileEntityTrafficPole tetp) {
		super(tetp.getAddon(), tetp, null);
		this.create();
	}
	
	public SwingGUITrafficPole(ItemStack is) {
		super(null, null, null);
		this.is = is;
		// アイテムスタックからモデルを作り出す
		NBTTagCompound compound = is.getTagCompound(); // あるはず
		String name = compound.getString("gts_item_model_pole");
		for (Pack pack: GTS.loader.getPacks().values()) {
			// パックの中からさらにモデルを引っ張り出す
			for (ModelBase model: pack.getModels()) {
				if (instanceOf(model) && getChoiceName(pack, (ModelTrafficPole) model).equals(name)) {
					// 見つかった場合は
					this.model = (ModelTrafficPole) model;
				}
			}
		}
		
		create(true);
		
	}
	
	protected SwingGUITrafficPole create() {
		super.create(() -> {
			te.setAddon(model);
			this.te.getAddon().reloadModel();
			te.setTexture(null); // 再生成させる
			setTitle(String.format("モデルを変更しました"));
			this.te.markDirty();
		});
		return this;
	}
	
	protected SwingGUITrafficPole create(boolean unused) {
		super.create(() -> {
			// ItemStackのタグに名前を保存する
			is.getTagCompound().setString("gts_item_model_pole", getChoiceName(GTS.loader.getPacks().get(model.getFile()), model));
		});
		return this;
	}
	
	@Override
	protected boolean instanceOf(Object other) {
		if (other instanceof ModelTrafficPole) return true;
		return super.instanceOf(other);
	}
}
