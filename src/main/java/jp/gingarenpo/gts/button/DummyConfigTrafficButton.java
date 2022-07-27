package jp.gingarenpo.gts.button;

import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * ダミーの押ボタン箱を格納するところ
 */
public class DummyConfigTrafficButton extends ConfigTrafficButton {
	
	public DummyConfigTrafficButton() throws IOException {
		this.id = "<DUMMY-BUTTON>";
		this.setBaseTexture("textures/models/dummy_tb.png");
		this.setPushTexture("textures/models/dummy_tb.png");
		this.setBaseTex(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tb.png")).getInputStream()));
		this.setPushTex(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tb.png")).getInputStream()));
	}
}
