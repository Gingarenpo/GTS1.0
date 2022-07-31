package jp.gingarenpo.gts.arm.gui;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.arm.ModelTrafficArm;
import jp.gingarenpo.gts.arm.TrafficArm;
import jp.gingarenpo.gts.core.GTSTileEntity;
import jp.gingarenpo.gts.core.gui.GTSSwingGUIModelChoiceBase;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.pack.Pack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SwingGUITrafficArm extends GTSSwingGUIModelChoiceBase<ModelTrafficArm, GTSTileEntity, TrafficArm> {
	
	private ItemStack itemStack;
	
	public SwingGUITrafficArm(ItemStack itemStack) {
		super(null, null, null); // 一旦nullで登録
		this.itemStack = itemStack;
		// アイテムスタックからモデルを作り出す
		NBTTagCompound compound = itemStack.getTagCompound(); // あるはず
		String name = compound.getString("gts_item_model_arm");
		for (Pack pack: GTS.loader.getPacks().values()) {
			// パックの中からさらにモデルを引っ張り出す
			for (ModelBase model: pack.getModels()) {
				if (instanceOf(model) && getChoiceName(pack, (ModelTrafficArm) model).equals(name)) {
					// 見つかった場合は
					this.model = (ModelTrafficArm) model;
				}
			}
		}
		
		create();
	}
	
	protected SwingGUITrafficArm create() {
		super.create(() -> {
			// ItemStackのタグに名前を保存する
			itemStack.getTagCompound().setString("gts_item_model_arm", getChoiceName(GTS.loader.getPacks().get(model.getFile()), model));
		});
		return this;
	}
	
	@Override
	protected boolean instanceOf(Object other) {
		if (other instanceof ModelTrafficArm) return true;
		return super.instanceOf(other);
	}
}
