package jp.gingarenpo.gingacore.mqo;

import net.minecraft.client.renderer.BufferBuilder;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * MQOのオブジェクトごとに、面と頂点を格納したものとなります。（頂点番号が重複するためこうしなくてはならない）
 * @author 銀河連邦
 *
 */
public class MQOObject implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	
	// モデルの面（フェイス）
	ArrayList<MQOFace> face = new ArrayList<MQOFace>(); // 面を番号ごとに格納
	
	// モデルの頂点
	ArrayList<MQOVertex> vertex = new ArrayList<MQOVertex>(); // 頂点を番号ごとに格納
	
	// モデルオブジェクトの名前
	String name; // 必ずあるはずよ
	
	public MQOObject(String name) {
		this.name = name; // 名前
	}
	
	public ArrayList<MQOFace> getFaces() {
		return face;
	}
	
	private void setFaces(ArrayList<MQOFace> face) {
		this.face = face;
	}
	
	public ArrayList<MQOVertex> getVertexs() {
		return vertex;
	}
	
	private void setVertexs(ArrayList<MQOVertex> vertex) {
		this.vertex = vertex;
	}
	
	public String getName() {
		return name;
	}
	
	
	/**
	 * 指定した色を使用してこのオブジェクトを描画するバッファを返します。
	 *
	 * @param color 色（0=自動シャドー）
	 */
	public void draw(BufferBuilder b, float color) {
		for (MQOFace f : this.face) {
			f.drawFace(b, color);
		}
	}
	
	/**
	 * 全部クローンする
	 * @return
	 */
	public MQOObject clone() {
		MQOObject clone = new MQOObject(this.name);
		for (MQOFace f : this.face) {
			clone.face.add(f.clone());
		}
		for (MQOVertex v : this.vertex) {
			clone.vertex.add(v.clone());
		}
		return clone;
	}

}
