package jp.gingarenpo.gts.button;

import jp.gingarenpo.gts.core.GTSSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;

/**
 * クライアント側で鳴らすためのサウンドイベントをここに登録する。
 * デフォルトでは大音量で鳴り響くようにしているため注意。
 */
public class SoundTrafficButton implements ISound {
	
	private float x;
	private float y;
	private float z;
	
	private ResourceLocation rs;
	
	private SoundEventAccessor soundEventAccessor;
	private Sound sound;
	
	public SoundTrafficButton(TileEntityTrafficButton te) {
		this.rs = te.getAddon().getSoundLocation();
		this.x = te.getPos().getX();
		this.y = te.getPos().getY();
		this.z = te.getPos().getZ();
	}
	
	@Override
	public ResourceLocation getSoundLocation() {
		return rs;
	}
	
	@Nullable
	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler) {
		
		this.soundEventAccessor = new SoundEventAccessor(new ResourceLocation("gts", "dynamic_sound.json"), null);
		return soundEventAccessor;
	}
	
	/**
	 * 実際に鳴らす音源を返す。
	 * @return
	 */
	@Override
	public Sound getSound() {
		return new GTSSound(rs.toString(), getVolume(), getPitch(), 1, Sound.Type.FILE, false);
		//return soundEventAccessor.cloneEntry();
	}
	
	@Override
	public SoundCategory getCategory() {
		return SoundCategory.BLOCKS;
	}
	
	@Override
	public boolean canRepeat() {
		return false;
	}
	
	@Override
	public int getRepeatDelay() {
		return 0;
	}
	
	@Override
	public float getVolume() {
		return 1;
	}
	
	@Override
	public float getPitch() {
		return 1;
	}
	
	@Override
	public float getXPosF() {
		return x;
	}
	
	@Override
	public float getYPosF() {
		return y;
	}
	
	@Override
	public float getZPosF() {
		return z;
	}
	
	@Override
	public AttenuationType getAttenuationType() {
		return AttenuationType.LINEAR;
	}
}
