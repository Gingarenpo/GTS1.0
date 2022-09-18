package jp.gingarenpo.gts.sign.data;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * 地名板を作成するための組込コンフィグデータ。地名板のタイプは
 * 幅合わせ幅固定の2パターンから、縦横も選べるようにする予定。
 * なおwidthとheightに関しては尊重されるが、これのせいでつぶれることもあるので注意。
 */
public class NamedTrafficSign extends TrafficSign {
	
	private static final long serialVersionUID = 1L;
	
	public NamedTrafficSign() {
		config.put("name", "青野原"); // 地名板の名前
		config.put("en_name", "           Aonohara           "); // 英字部分
		config.put("portrait", false); // trueにすると縦書きレイアウトモードになる
		config.put("width_fix", true); // falseにすると幅を無限に増やす（国交省完全準拠）
		config.put("text_color", Color.BLUE); // テキストの色。デフォルト青。山梨みたいにしたい時帰る
		config.put("en_font", null);
		
	}
	
	@Override
	public BufferedImage createMainTexture() {
		float q = 512; // 1ブロック当たりの解像度
		String name = (String) config.get("name");
		BufferedImage tex = null;
		if ((boolean) config.get("width_fix")) {
			// 幅固定の場合はwidthの値を尊重
			tex = new BufferedImage((int)(q * ((Float) config.get("width"))), (int) (q * (Float)(config.get("height"))), BufferedImage.TYPE_4BYTE_ABGR);
		}
		else {
			// 幅可変の場合はwidthの値を無視
			tex = new BufferedImage((int) getNoFixWidth(name, q ), (int) (q * (float)(config.get("height"))), BufferedImage.TYPE_4BYTE_ABGR);
		}
		Graphics2D g = tex.createGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor((Color) (config.get("color")));
		g.fillRect(0, 0, tex.getWidth(), tex.getHeight()); // 単一色でとりあえず塗りつぶす
		
		Font f = (Font) config.getOrDefault("font", new Font(Font.SANS_SERIF, Font.BOLD, (int) (q)));
		if (f == null) f = new Font(Font.SANS_SERIF, Font.BOLD, (int) (q));
		g.setFont(f);
		
		// 枠線を描画
		g.setColor((Color) config.get("text_color"));
		g.setStroke(new BasicStroke(q/64));
		g.draw(new RoundRectangle2D.Float(q/32, q/32, tex.getWidth()-q/16, tex.getHeight()-q/16, q/16, q/16));
		
		// 日本語文字列を描画
		// 潰れる場合があるので一度別のテクスチャに描画する
		// 縦書きの場合は90度回転させる
		if ((boolean) config.get("portrait")) {
			g.rotate(Math.PI / 2 * 3, 0, 0);
			
		}
		BufferedImage texttex = null;
		
		for (int i = (int)q; i > 0; i--) {
			
			f = f.deriveFont((float) i);
			FontMetrics fm = g.getFontMetrics(); // フォントメトリックスを取得
			Rectangle r = fm.getStringBounds(name, g).getBounds();
			
			if (r.height < (q / 7 * 4)) {
				// 別のテクスチャに描画する
				
				
				if (name.length() > 4 && (boolean) config.get("width_fix")) {
					if ((boolean) config.get("portrait")) {
						texttex = new BufferedImage(r.height, (int) (r.width + 0.125 * fm.charWidth(name.charAt(0)) * (name.length() + 1)), BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D g2 = texttex.createGraphics();
						g2.setColor((Color) config.get("text_color"));
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setFont(f);
						for (int j = 0; j < name.length(); j++) {
							g2.drawString(String.valueOf(name.charAt(j)), r.x,  (int) ((j + 1) * 1.125 * fm.charWidth(name.charAt(j)) - 0.125 * fm.charWidth(name.charAt(j))));
						}
					}
					else {
						texttex = new BufferedImage((int) (r.width + 0.125 * fm.charWidth(name.charAt(0)) * (name.length() + 1)), r.height, BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D g2 = texttex.createGraphics();
						g2.setColor((Color) config.get("text_color"));
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setFont(f);
						for (int j = 0; j < name.length(); j++) {
							g2.drawString(String.valueOf(name.charAt(j)), (int) (0.125 * fm.charWidth(name.charAt(j)) + (j) * 1.125 * fm.charWidth(name.charAt(j))), - r.y);
						}
					}
				}
				else {
					// 4文字以下の場合は字間を1/4だけ空けて描画する
					if ((boolean) config.get("portrait")) {
						texttex = new BufferedImage(r.height, (int) (r.width + 0.25 * fm.charWidth(name.charAt(0)) * (name.length())), BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D g2 = texttex.createGraphics();
						g2.setColor((Color) config.get("text_color"));
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setFont(f);
						for (int j = 0; j < name.length(); j++) {
							g2.drawString(String.valueOf(name.charAt(j)), r.x,  (int) ((j + 1) * 1.25 * fm.charWidth(name.charAt(j)) - 0.25 * fm.charWidth(name.charAt(j))));
						}
					}
					else {
						texttex = new BufferedImage((int) (r.width + 0.25 * fm.charWidth(name.charAt(0)) * (name.length() + 1)), r.height, BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D g2 = texttex.createGraphics();
						g2.setColor((Color) config.get("text_color"));
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setFont(f);
						for (int j = 0; j < name.length(); j++) {
							g2.drawString(String.valueOf(name.charAt(j)), (int) (0.25 * fm.charWidth(name.charAt(j)) + (j) * 1.25 * fm.charWidth(name.charAt(j))), - r.y);
						}
					}
				}
				break;
			}
			
			
			g.setFont(f);
		}
		if ((boolean) config.get("portrait")) {
			g.drawImage(texttex, (int)-((q * (float)(config.get("height")))/6 + (q * (float)(config.get("height"))) / 7 * 3), (int)((q * (float)(config.get("width"))) / 16), -(int)((q * (float)(config.get("height"))) / 8), (int)(tex.getWidth() - (q * (float)(config.get("width"))) / 16), 0, 0, texttex.getWidth(), texttex.getHeight(), null);
		}
		else {
			g.drawImage(texttex, (int)((q * (float)(config.get("width")))/16), (int)((q * (float)(config.get("height")))/6), tex.getWidth() - (int)((q * (float)(config.get("width")))/16), (int)((q * (float)(config.get("height")))/6 + (q * (float)(config.get("height"))) / 7 * 3), 0, 0, texttex.getWidth(), texttex.getHeight(), null);
		}
		
		
		// 英語文字列を描画
		if ((boolean) config.get("portrait")) {
			g.rotate(-Math.PI / 2 * 3, 0, 0);
		}
		f = (Font) config.getOrDefault("en_font", new Font(Font.SANS_SERIF, Font.BOLD, (int) (q)));
		if (f == null) f = new Font(Font.SANS_SERIF, Font.BOLD, (int) (q));
		g.setFont(f);
		for (int i = (int)q; i > 0; i--) {
			f = f.deriveFont((float)i);
			FontMetrics fm = g.getFontMetrics(); // フォントメトリックスを取得
			Rectangle r = fm.getStringBounds((String) config.get("en_name"), g).getBounds();
			
			// 固定幅の場合は高さだけを判別する
			if (r.height < (q / 3)) {
				// 別のテクスチャに描画する
				texttex = new BufferedImage(r.width, r.height, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2 = texttex.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor((Color) config.get("text_color"));
				g2.setFont(f);
				g2.drawString((String) config.get("en_name"), r.x, -r.y);
				break;
			}
			
			
			g.setFont(f);
		}
		g.drawImage(texttex, (int)(q/16), (int)(tex.getHeight() - ((q * (float)(config.get("height"))) / 5 * 2)), tex.getWidth() - (int)(q/16), tex.getHeight() - (int)(q/16), 0, 0, texttex.getWidth(), texttex.getHeight(), null);
		
		
		
		return tex;
	}
	
	@Override
	public void renderMain(Tessellator t) {
		// 厚さ0.01のものとして描画する
		// XYZのずれを反映させる
		BufferBuilder b = t.getBuffer(); // バッファ開始
		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		if ((boolean) config.get("portrait")) {
			// 縦の場合はwidthとheightが入れ替わる
			b.pos(0.5 - (float)config.get("height")/2 - (float)config.get("X"), 0.5 - (float)config.get("width")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("height")/2 - (float)config.get("X"), 0.5 + (float)config.get("width")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2 - (float)config.get("X"), 0.5 + (float)config.get("width")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2 - (float)config.get("X"), 0.5 - (float)config.get("width")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
		}
		else {
			// 横の場合は普通に描画
			b.pos(0.5 - (float)config.get("width")/2 + (float)config.get("X"), 0.5 - (float)config.get("height")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("width")/2 + (float)config.get("X"), 0.5 + (float)config.get("height")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2 + (float)config.get("X"), 0.5 + (float)config.get("height")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2 + (float)config.get("X"), 0.5 - (float)config.get("height")/2 + (float)config.get("Y"), 0.499 + (float)config.get("Z"));
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			
			
			
		}
		
		
		t.draw();
	}
	
	@Override
	public void renderBack(Tessellator t) {
		// 厚さ0.01のものとして描画する
		// XYZのずれを反映させる
		BufferBuilder b = t.getBuffer(); // バッファ開始
		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		if ((boolean) config.get("portrait")) {
			// 縦の場合はwidthとheightが入れ替わる
			b.pos(0.5 - (float)config.get("height")/2 - (float)config.get("X"), 0.5 - (float)config.get("width")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2 - (float)config.get("X"), 0.5 - (float)config.get("width")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2 - (float)config.get("X"), 0.5 + (float)config.get("width")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("height")/2 - (float)config.get("X"), 0.5 + (float)config.get("width")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
		
		}
		else {
			// 横の場合は普通に描画
			b.pos(0.5 - (float)config.get("width")/2 + (float)config.get("X"), 0.5 - (float)config.get("height")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2 + (float)config.get("X"), 0.5 - (float)config.get("height")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2 + (float)config.get("X"), 0.5 + (float)config.get("height")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("width")/2 + (float)config.get("X"), 0.5 + (float)config.get("height")/2 + (float)config.get("Y"), 0.501 + (float)config.get("Z"));
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
		}
		t.draw();
	}
	
	private void render(Tessellator t, double z) {
		// 厚さ0.01のものとして描画する
		// XYZのずれを反映させる
		BufferBuilder b = t.getBuffer(); // バッファ開始
		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		if ((boolean) config.get("portrait")) {
			// 縦の場合はwidthとheightが入れ替わる
			b.pos(0.5 - (float)config.get("height")/2, 0.5 - (float)config.get("width")/2, z);
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("height")/2, 0.5 + (float)config.get("width")/2, z);
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2, 0.5 + (float)config.get("width")/2, z);
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("height")/2, 0.5 - (float)config.get("width")/2, z);
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
		}
		else {
			// 横の場合は普通に描画
			b.pos(0.5 - (float)config.get("width")/2, 0.5 - (float)config.get("height")/2, z);
			b.tex(1, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 - (float)config.get("width")/2, 0.5 + (float)config.get("height")/2, z);
			b.tex(1, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2, 0.5 + (float)config.get("height")/2, z);
			b.tex(0, 0);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			b.pos(0.5 + (float)config.get("width")/2, 0.5 - (float)config.get("height")/2, z);
			b.tex(0, 1);
			b.color(255, 255, 255, 255);
			b.endVertex();
			
			
			
			
		}
		
		
		t.draw();
	}
	
	/**
	 * 幅合わせの場合の幅を返す
	 * @param name
	 * @param q
	 * @return
	 */
	private float getNoFixWidth(String name, float q) {
		return (float) (name.length() * (q / 2) + (name.length()+2) * 0.25 * (q / 2));
	}
	
}
