package jp.gingarenpo.gts.data;

import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;

public class DummyConfig extends ConfigBase {
	
	public DummyConfig() {
		this.setId("dummy");
		// テクスチャを無理やり指定
		TexturePath t = new TexturePath();
		try {
			t.setBaseTex(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tl.png")).getInputStream()));
			t.setLightTex(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tl.png")).getInputStream()));
			t.setNoLightTex(ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "textures/models/dummy_tl.png")).getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setTextures(t);
		
		// オブジェクトも無理やり（）
		ArrayList<String> b = new ArrayList<String>();
		b.add("body");
		b.add("gbody");
		b.add("ybody");
		b.add("rbody");
		b.add("g300");
		b.add("y300");
		b.add("r300");
		b.add("normalg");
		b.add("normaly");
		b.add("normalr");
		b.add("g");
		b.add("y");
		b.add("r");
		this.setBaseObject(b);
		
		this.setSize(1.5f);
	}
}
