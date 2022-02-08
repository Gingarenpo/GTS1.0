package jp.gingarenpo.gingacore.mqo;

import java.util.ArrayList;

/**
 * MQOのオブジェクトごとに、面と頂点を格納したものとなります。（頂点番号が重複するためこうしなくてはならない）
 * @author 銀河連邦
 *
 */
public class MQOObject {
	
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
	
	public ArrayList<MQOVertex> getVertexs() {
		return vertex;
	}
	
	public String getName() {
		return name;
	}
	
	public MQO getParent() {
		return mqo;
	}
}
