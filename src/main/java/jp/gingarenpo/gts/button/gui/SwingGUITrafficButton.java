package jp.gingarenpo.gts.button.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.button.ModelTrafficButton;
import jp.gingarenpo.gts.button.TileEntityTrafficButton;
import jp.gingarenpo.gts.button.TrafficButton;
import jp.gingarenpo.gts.core.gui.GTSSwingGUIModelChoiceBase;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SwingGUITrafficButton extends GTSSwingGUIModelChoiceBase<ModelTrafficButton, TileEntityTrafficButton, TrafficButton> {
	
	private ItemStack itemStack;
	
	public SwingGUITrafficButton(ItemStack itemStack) {
		super(null, null, null); // 一旦nullで登録
		this.itemStack = itemStack;
		// アイテムスタックからモデルを作り出す
		NBTTagCompound compound = itemStack.getTagCompound(); // あるはず
		String name = compound.getString("gts_item_model_arm");
		for (Pack pack: GTS.loader.getPacks().values()) {
			// パックの中からさらにモデルを引っ張り出す
			for (ModelBase model: pack.getModels()) {
				if (instanceOf(model) && getChoiceName(pack, (ModelTrafficButton) model).equals(name)) {
					// 見つかった場合は
					this.model = (ModelTrafficButton) model;
				}
			}
		}
		
		create();
	}
	
	protected SwingGUITrafficButton create() {
		super.create(() -> {
			// ItemStackのタグに名前を保存する
			itemStack.getTagCompound().setString("gts_item_model_button", getChoiceName(GTS.loader.getPacks().get(model.getFile()), model));
		});
		return this;
	}
	
	@Override
	protected boolean instanceOf(Object other) {
		if (other instanceof ModelTrafficButton) return true;
		return super.instanceOf(other);
	}
}
