package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gts.GTS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * アイテムとして保持させるだけのアームモデル。
 *
 */
public class ItemTrafficArm extends Item {
	
	public ItemTrafficArm() {
		this.setRegistryName(new ResourceLocation(GTS.MOD_ID, "arm"));
		this.setTranslationKey("arm");
		this.setCreativeTab(GTS.gtsTab);
		this.setMaxStackSize(1);
	}
	
	/**
	 * このアイテムを右クリックしたときにおこる処理。ActionResultを返す。
	 * @param worldIn 世界。
	 * @param playerIn プレイヤー。
	 * @param handIn どっちの手で持っているか。
	 * @return アイテムば貸したりできるか
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	
}
