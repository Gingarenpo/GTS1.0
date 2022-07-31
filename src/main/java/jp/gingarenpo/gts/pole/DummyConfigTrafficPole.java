package jp.gingarenpo.gts.pole;

import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;

public class DummyConfigTrafficPole extends ConfigTrafficPole {
	
	private static final long serialVersionUID = 1L;
	
	public DummyConfigTrafficPole() throws IOException {
		this.setId("dummy");
		this.setTexture("textures/models/dummy_tp.png");
		this.setTexImage(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tp.png")).getInputStream()));
		ArrayList<String> s = new ArrayList<>();
		s.add("base");
		this.setBaseObject(s);
		
		ArrayList<String> s1 = new ArrayList<>();
		s1.add("top");
		this.setTopObject(s1);
		
		ArrayList<String> s2 = new ArrayList<>();
		s2.add("bottom");
		this.setBottomObject(s2);
	}
}
