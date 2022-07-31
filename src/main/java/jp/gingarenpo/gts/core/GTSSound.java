package jp.gingarenpo.gts.core;

import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;

/**
 * Soundインスタンスがデフォルトでsounds/から読み込もうとするのでそれをなしにして
 * パスでそのまま読み込めるようにするためのサウンドカスタムクラス。
 */
public class GTSSound extends Sound {
	
	public GTSSound(String nameIn, float volumeIn, float pitchIn, int weightIn, Type typeIn, boolean p_i46526_6_) {
		super(nameIn, volumeIn, pitchIn, weightIn, typeIn, p_i46526_6_);
	}
	
	@Override
	public ResourceLocation getSoundAsOggLocation() {
		System.out.println(super.getSoundLocation());
		return super.getSoundLocation();
	}
}
