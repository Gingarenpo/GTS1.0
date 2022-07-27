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
	
	// 元親の名前
	MQO mqo;
	
	public MQOObject(MQO mqo, String name) {
		this.mqo = mqo;
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
	
	public MQO getParent() {
		return mqo;
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
	
	public MQOObject clone() {
		MQOObject clone = new MQOObject(this.mqo, this.name);
		clone.vertex = new ArrayList<>(this.vertex);
		clone.face = new ArrayList<>(this.face);
		return clone;
	}

}
