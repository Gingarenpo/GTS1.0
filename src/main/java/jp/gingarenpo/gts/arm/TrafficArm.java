package jp.gingarenpo.gts.arm;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * アームの基本情報（モデルとか）を格納しておくところ。
 * このデータ自体はポールに格納され、接続先のTileEntityインスタンスを格納して置ける。
 * 接続先は現在信号機のみとするが将来的に変更する可能性も。
 */
public class TrafficArm implements Serializable {
	
	/**
	 * このアームの使用モデル。
	 */
	private ModelTrafficArm addon;
	
	/**
	 * リソースロケーション（テクスチャ）
	 */
	private ResourceLocation texture;
	
	/**
	 * アームをつなぐ先の信号機。一度つないだらどっちか壊さないと解除できない。
	 */
	private ArrayList<double[]> to = new ArrayList<>();
	
	/**
	 * 指定したTileEntityを接続先としてインスタンスを作成する。
	 *
	 */
	public TrafficArm() {
		setDummyModel();
	}
	
	public ArrayList<double[]> getTo() {
		return to;
	}
	
	public void setTo(ArrayList<double[]> to) {
		this.to = to;
	}
	
	/**
	 * メソッドチェーンに使う
	 * @param to
	 * @return
	 */
	public TrafficArm add(double[] to) {
		this.to.add(to);
		return this;
	}
	
	public ModelTrafficArm getAddon() {
		return addon;
	}
	
	public void setAddon(ModelTrafficArm addon) {
		this.addon = addon;
	}
	
	public void setDummyModel() {
		try {
			this.addon = new ModelTrafficArm(new DummyConfigTrafficArm(), new MQO(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(GTS.MOD_ID, "models/dummy/dummy_ta.mqo")).getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ResourceLocation getTexture() {
		return texture;
	}
	
	public void setTexture(ResourceLocation texture) {
		this.texture = texture;
	}
}
