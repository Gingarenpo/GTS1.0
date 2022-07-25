package jp.gingarenpo.gts.light;

import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;

public class DummyConfigTrafficLight extends ConfigTrafficLight {
	
	private static final long serialVersionUID = 1L;
	
	public DummyConfigTrafficLight() {
		this.setId("dummy"); // 名前はこれで固定
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
		
		this.setShowBoth(true);
		
		// ベースのオブジェクト（無点灯）指定
		ArrayList<String> b = new ArrayList<String>();
		b.add("body");
		b.add("gbody");
		b.add("ybody");
		b.add("rbody");
		b.add("normalg");
		b.add("normaly");
		b.add("normalr");
		b.add("g");
		b.add("y");
		b.add("r");
		this.setBody(b);
		
		// 点灯部分指定
		b = new ArrayList<String>();
		b.add("g300");
		b.add("y300");
		b.add("r300");
		this.setLight(b);
		
		this.setSize(1.5f);
		
		// LightObjectも指定（デフォルトで青点灯）
		LightObject l = new LightObject();
		l.setName("green");
		ArrayList<String> objects = new ArrayList<String>();
		objects.add("g300");
		l.setObjects(objects);
		
		LightObject l3 = new LightObject();
		l3.setName("yellow");
		objects = new ArrayList<String>();
		objects.add("y300");
		l3.setObjects(objects);
		
		LightObject l4 = new LightObject();
		l4.setName("red");
		objects = new ArrayList<String>();
		objects.add("r300");
		l4.setObjects(objects);
		
		ArrayList<LightObject> l2 = new ArrayList<>();
		l2.add(l);
		l2.add(l3);
		l2.add(l4);
		this.setPatterns(l2);
		
		// 中心点も指定
		this.setCenterPosition(new double[] {0, 0, 0});
		
		
	}
}
