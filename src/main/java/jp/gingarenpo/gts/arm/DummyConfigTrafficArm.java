package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class DummyConfigTrafficArm extends ConfigTrafficArm implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	public DummyConfigTrafficArm() throws IOException {
		this.setId("dummy");
		this.setTexImage(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tp.png")).getInputStream()));
		ArrayList<String> s = new ArrayList<>();
		s.add("base");
		this.setBaseObject(s);
		
		ArrayList<String> s1 = new ArrayList<>();
		s1.add("start");
		this.setStartObject(s1);
		
		this.setSize(1);
	}
}
