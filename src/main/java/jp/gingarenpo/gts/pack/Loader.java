package jp.gingarenpo.gts.pack;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.core.ConfigBase;
import jp.gingarenpo.gts.core.ModelBase;
import jp.gingarenpo.gts.light.ConfigTrafficLight;
import jp.gingarenpo.gts.light.ModelTrafficLight;
import jp.gingarenpo.gts.pole.ConfigTrafficPole;
import jp.gingarenpo.gts.pole.ModelTrafficPole;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * GTSのパックのみを読み込むためのクラス。読み込んだパックのFileインスタンスを保持する。
 * ZipFileは必要に応じて開くこと（こればかりだとメモリが大変なことになるため）
 */
public class Loader {
	
	private HashMap<File, Pack> packs = new HashMap<>(); // パックの存在場所を示すもの。必ず存在する（ゲーム開始時点では）
	private HashMap<File, HashMap<String, BufferedImage>> textures = new HashMap<>(); // テクスチャの存在を示すもの
	
	public Loader() {
		// 基本的にインスタンスを使うってよりはパックを保持するためだけなのでここでの記載なし
	}
	
	/**
	 * 現在読み込まれているパックのリストを返す。
	 * @return パックリスト
	 */
	public HashMap<File, Pack> getPacks() {
		return packs;
	}
	
	/**
	 * 現在読み込まれているテクスチャのリストを返す。
	 * @return テクスチャリスト
	 */
	public HashMap<File, HashMap<String, BufferedImage>> getTextures() {
		return textures;
	}
	
	/**
	 * 指定したパックの指定したテクスチャを返す。なければnullを返す。
	 * @param pack 入っているパック。
	 * @param location 入っている場所。
	 * @return テクスチャ、なければnull
	 */
	public BufferedImage getTexture(File pack, String location) {
		if (!textures.containsKey(pack)) {
			return null;
		}
		return textures.get(pack).get(location);
	}
	
	/**
	 * 指定したパックが読み込みされているかを返す（たとえこのメソッドを呼んだ時点で存在していなくても初回読み込み時にあればtrue）
	 *
	 * @param pack 調べたいパック。
	 * @return パックが読み込みされたらtrue、されていなかったらfalse
	 */
	public boolean isContain(File pack) {
		return packs.containsKey(pack);
	}
	
	/**
	 * 指定したパックが存在するか返す。読み込まれているのに今呼び出したらないって状態を阻止するために使う。
	 * ただしあくまで名前のチェックなので、パックが消されて同名の名前で変なファイルを置くとこれはtrueを返す。
	 * そこまで厳密にしたい場合はisAvailableメソッドを使う。
	 *
	 * @param pack 調べたいパック。
	 * @return パックが呼び出し時点で存在していればtrue、読み込まれてすらいないか存在しない場合はfalse
	 */
	public boolean isExist(File pack) {
		if (!isContain(pack)) return false; // そもそも読み込まれていない
		return pack.exists();
	}
	
	/**
	 * 指定したパックが存在し、かつそれが有効なファイルであるかどうかまで検証する。
	 * どうしてもぬるぽを出したくない場合など、厳密なチェックが必要な場合にこちらを使用する。
	 *
	 * @param pack 調べたいパック。
	 * @return パックがきちんとパックであり、存在していて読み込まれていればtrue。そうでないばあいはfalse
	 */
	public boolean isAvailable(File pack) {
		if (!isExist(pack)) return false; // そもそも存在しない
		return getPack(pack) != null; // パックとして正常に帰ってきたらtrueになるはず
	}
	
	/**
	 * 指定したパックの中身を解析し、パック情報をまとめて返す。
	 * @param pack 検証したいパック。
	 * @return 中身がパックとして正しい形式の場合Packオブジェクトが返る。不明なファイル形式の場合はnullが返る。
	 */
	public Pack getPack(File pack) {
		if (isExist(pack)) return null; // そもそも存在しない場合はダメ
		
		Pack res = null; // 帰すべきもの
		Properties p = new Properties();
		ArrayList<ConfigBase> configs = new ArrayList<ConfigBase>(); // コンフィグの内容を一時的に保持しておくところ
		HashMap<String, MQO> models = new HashMap<String, MQO>(); // モデルファイルを格納しておく場所
		HashMap<String, BufferedImage> textures = new HashMap<>(); // テクスチャを格納しておく場所
		
		try (FileInputStream fis = new FileInputStream(pack)) {
			try (ZipInputStream zis = new ZipInputStream(fis)) {
				// Zipの中身を覗いていく
				ZipEntry entry = null; // 1つずつしか確認できないので
				while ((entry = zis.getNextEntry()) != null) {
					// ファイルが終わるまで繰り返す
					if (entry.isDirectory()) continue; // ディレクトリの場合は無視
					
					if (entry.getName().endsWith("gts.txt")) {
						// GTCの情報が格納されたファイル（プロパティ形式）
						p.load(new InputStreamReader(zis, StandardCharsets.UTF_8)); // プロパティファイルを読み込ませる
						continue;
					}
					
					if (entry.getName().endsWith("json")) {
						// JSONファイルだった場合は設定ファイルかもしれないのでチェックする
						ObjectMapper om = new ObjectMapper();
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(entry.getSize()))) {
							byte[] tmp = new byte[Math.toIntExact(entry.getSize())];
							zis.read(tmp);
							baos.write(tmp); // こうしないとJacksonがZipストリームを閉じてしまう
							ConfigTrafficLight c = om.readValue(new ByteArrayInputStream(baos.toByteArray()), ConfigTrafficLight.class); // コンフィグとして読み込みを試みる
							configs.add(c); // 追加
						} catch (JsonParseException e) {
							// そもそもJSONとして不適切な場合
							GTS.GTSLog.log(Level.WARN, entry.getName() + " is not a JSON File. It was skipped. -> " + e.getMessage() );
						} catch (JsonMappingException e) {
							// JSONに適切にマッピングできなかった場合
							// ポールでやってみる
							try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(entry.getSize()))) {
								byte[] tmp = new byte[Math.toIntExact(entry.getSize())];
								zis.read(tmp);
								baos.write(tmp); // こうしないとJacksonがZipストリームを閉じてしまう
								ConfigTrafficPole c = om.readValue(new ByteArrayInputStream(baos.toByteArray()), ConfigTrafficPole.class);
								configs.add(c);
							} catch (JsonMappingException e2) {
								// それでもダメな場合
								GTS.GTSLog.log(Level.WARN, entry.getName() + " is not a available GTS Pack Config. It was skipped. -> " + e.getMessage() );
							}
						} catch (Exception e) {
							// なんかそれ以外のエラーが発生した場合
							GTS.GTSLog.log(Level.WARN, "Can't load " + entry.getName() + " some reason. -> " + e.getMessage() );
						}
					}
					else if (entry.getName().endsWith("mqo")) {
						// モデルファイルだった場合
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(entry.getSize()))) {
							byte[] tmp = new byte[Math.toIntExact(entry.getSize())];
							int read = 0;
							while (read < entry.getSize()) {
								int already =  zis.read(tmp, read, (int) (entry.getSize()-read));
								read += already;
							}
							baos.write(tmp);
							
							MQO mqo = new MQO(new ByteArrayInputStream(baos.toByteArray())); // MQOオブジェクトを設定
							models.put(entry.getName(), mqo); // MQOオブジェクトを登録
						} catch (IOException e) {
							// ファイル異常で読み込みすらできない場合
							GTS.GTSLog.log(Level.WARN, "Can't load" + entry.getName() + " some reason. -> " + e.getMessage());
						} catch (MQO.MQOException e) {
							// MQOファイルとして不適切な場合
							GTS.GTSLog.log(Level.WARN, entry.getName() + " is not a MQO Format. It was skipped. -> " + e.getMessage());
						}
					}
					else if (entry.getName().endsWith(".jpg") || entry.getName().endsWith(".png")) {
						// 画像ファイルだった場合
						textures.put(entry.getName(), ImageIO.read(zis)); // メモリバカ食いするのでどうにかしたいが
					}
					
				}
				
				// 各種コンフィグに対して処理を追加する
				ArrayList<ModelBase> m = new ArrayList<>(); // 正常に追加したパックモデル
				for (ConfigBase configBase : configs) {
					if (!models.containsKey(configBase.getModel())) {
						// モデルが存在しない場合（そもそもこのパックは使用不可）
						GTS.GTSLog.log(Level.WARN, configBase.getId() + " declared model as " + configBase.getModel() + ", but it is not found or broken. This model was skipped.");
						continue;
					}
					if (configBase instanceof ConfigTrafficLight) {
						ConfigTrafficLight config = (ConfigTrafficLight) configBase;
						if (!textures.containsKey(config.getTextures().getBase())) {
							// ベースのテクスチャが存在しない場合（そもそもこのパックは使用不可)
							GTS.GTSLog.log(Level.WARN, config.getId() + " declared Base Texture as " + config.getTextures().getBase() + ", but it is not found or broken. This model was skipped.");
							continue;
						}
						if (config.getTextures().getLight() == null || !textures.containsKey(config.getTextures().getLight())) {
							// 発光テクスチャが存在しない場合
							GTS.GTSLog.log(Level.WARN, config.getId() + "'s light texture will set " + config.getTextures().getBase());
							config.getTextures().setLight(config.getTextures().getBase()); // 基本画像を使用する
						}
						if (config.getTextures().getNoLight() == null || !textures.containsKey(config.getTextures().getNoLight())) {
							// 未発光テクスチャが存在しない場合
							GTS.GTSLog.log(Level.WARN, config.getId() + "'s nolight texture will set " + config.getTextures().getLight());
							config.getTextures().setNoLight(config.getTextures().getLight()); // 発光画像を使用する（発光画像もない場合は既にベース画像が使用されることになっている）
						}
						
						// テクスチャをそれぞれ読み込む
						ConfigTrafficLight.TexturePath t = config.getTextures(); // もうめんどくさいので一回読み込み
						t.setBaseTex(textures.get(t.getBase()));
						t.setLightTex(textures.get(t.getLight()));
						t.setNoLightTex(textures.get(t.getNoLight())); // 以上、テクスチャのセット
						
						m.add(new ModelTrafficLight(config, models.get(config.getModel()), pack)); // モデルは絶対にあるはずなので
					}
					else if (configBase instanceof ConfigTrafficPole) {
						ConfigTrafficPole config = (ConfigTrafficPole) configBase;
						if (config.getTexture() == null) {
							GTS.GTSLog.log(Level.WARN, config.getId() + " didn't declare Texture. This model was skipped.");
							continue;
						}
						
						if (!textures.containsKey(config.getTexture())) {
							// テクスチャが存在しない
							GTS.GTSLog.warn(config.getId() + " is not found in this pack. This model was skipped.");
						}
						
						config.setTexImage(textures.get(config.getTexture()));
						m.add(new ModelTrafficPole(config, models.get(config.getModel()), pack));
						
					}
					
					
					
				}
				
				// パックを生成する
				res = new Pack((String) p.getOrDefault("name", "<No Name>"), (String) p.getOrDefault("credit", "<No Credit>"), m, pack); // これで生成
			}
		} catch (IOException e) {
			// I/Oアクセスに失敗して開くことができなかった場合、もしくはZip形式じゃない場合
			GTS.GTSLog.log(Level.ERROR, "Can't complete add a pack '" + pack.getName() + "'. It was skipped. reason -> " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		this.textures.put(pack, textures);
		
		return res;
	}
	
	/**
	 * 指定したディレクトリ（Fileインスタンス）からパックを探し、パックを登録する。
	 * 指定したディレクトリがディレクトリではなかった場合や、存在しない場合はIllegalArgumentExceptionがスローされる。
	 * さらに、ファイルアクセス中に死んだ場合はIOExceptionもスローされる。
	 * なお階層が深いものは読み込まない。
	 * @param search 探したいディレクトリ
	 */
	public void load(File search) throws IOException {
		if (!search.exists() || !search.isDirectory()) throw new IllegalArgumentException("Only exists directory can be input."); // ないかディレクトリじゃない
		GTS.GTSLog.log(Level.INFO, "Pack search started.");
		File[] zips = search.listFiles((dir, name) -> (name.endsWith("zip"))); // Zipファイルだけを選別
		if (zips == null) throw new IOException("Can't find pack at search directory.");
		for (File zip: zips) {
			// 見つかったZipファイルの数だけ繰り返す
			Pack p = getPack(zip); // パックを探す
			if (p != null) {
				// Nullじゃなければ
				packs.put(zip, p);
				GTS.GTSLog.log(Level.INFO, "GTS Addon Pack '" + p.getName() + "' in " + zip.getAbsolutePath() + " loaded. (models: " + p.getModels().size() + ")");
			}
		}
		GTS.GTSLog.log(Level.INFO, "Pack search ended. Add " + packs.size() +  " packs.");
	}
}
