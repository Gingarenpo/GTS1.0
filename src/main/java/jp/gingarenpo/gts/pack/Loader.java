package jp.gingarenpo.gts.pack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.arm.ConfigTrafficArm;
import jp.gingarenpo.gts.button.ConfigTrafficButton;
import jp.gingarenpo.gts.button.ModelTrafficButton;
import jp.gingarenpo.gts.core.config.ConfigBase;
import jp.gingarenpo.gts.core.model.ModelBase;
import jp.gingarenpo.gts.light.ConfigTrafficLight;
import jp.gingarenpo.gts.light.ModelTrafficLight;
import jp.gingarenpo.gts.pole.ConfigTrafficPole;
import jp.gingarenpo.gts.pole.ModelTrafficPole;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * GTSのパックのみを読み込むためのクラス。読み込んだパックのFileインスタンスを保持する。
 * ZipFileは必要に応じて開くこと（こればかりだとメモリが大変なことになるため）
 *
 * Jackson死ぬので既存のGsonを使ってみる
 */
public class Loader {
	
	private HashMap<File, Pack> packs = new HashMap<>(); // パックの存在場所を示すもの。必ず存在する（ゲーム開始時点では）
	private HashMap<File, HashMap<String, BufferedImage>> textures = new HashMap<>(); // テクスチャの存在を示すもの
	private HashMap<File, HashMap<String, byte[]>> sounds = new HashMap<>(); // サウンドのバイナリ自体を格納しておくところ
	
	private GTSSoundJson soundJson = new GTSSoundJson(); // JSON作成用
	private String soundJsonString; // inputStream用
	
	private boolean completeLoad = false;
	
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
	 * 読み込まれているサウンドを返す。あくまでoggファイルの実態であり、
	 * ここからサウンドイベントを取得することはできない。
	 * @return 音源
	 */
	public HashMap<File, HashMap<String, byte[]>> getSounds() {
		return sounds;
	}
	
	/**
	 * sound.jsonとして読み込める形式で返す。
	 * @return
	 */
	public String getSoundJsonString() {
		return soundJsonString;
	}
	
	/**
	 * ロードが完了している場合はtrueを返す。
	 * この値がfalseである時は、まだロードが完了していないかエラーが発生したか、loadメソッドが呼ばれていない状況。
	 * つまり、このメソッドがfalseを返すときはモデルやテクスチャの読み込みができる保証がない。
	 * @return ロード完了していたらtrue
	 */
	public boolean isCompleteLoad() {
		return completeLoad;
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
		HashMap<String, byte[]> sounds = new HashMap<>(); // テクスチャを格納しておく場所
		
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
						Gson g = new Gson();
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(entry.getSize()))) {
							byte[] tmp = new byte[Math.toIntExact(entry.getSize())];
							zis.read(tmp);
							baos.write(tmp); // こうしないとJacksonがZipストリームを閉じてしまう
							ConfigBase c = null;
							ConfigTrafficLight c1 = g.fromJson(baos.toString(), ConfigTrafficLight.class);
							if (c1.getTextures() == null) {
								// 信号機としての必須項目の不足。この場合はポールのコンフィグとして読み込ませる
								ConfigTrafficPole c2 = g.fromJson(baos.toString(), ConfigTrafficPole.class);
								if (c2.getBaseObject() == null) {
									// ポールとして必須項目が不足している。この場合はアームのコンフィグとして読み込ませる
									ConfigTrafficArm c3 = g.fromJson(baos.toString(), ConfigTrafficArm.class);
									if (c3.getStartObject() == null) {
										// アームでも不足している。ならばボタンで読み込む
										ConfigTrafficButton c4 = g.fromJson(baos.toString(), ConfigTrafficButton.class);
										if (c4.getBaseTex() == null) {
											// ボタンにおいてもダメ。もうこれは不正なJSONファイル。
											throw new IOException("It's not any config file.");
										}
										else {
											c = c4;
										}
									}
									else {
										c = c3;
									}
								}
								else {
									c = c2;
								}
							}
							else {
								c = c1;
							}
							configs.add(c); // 追加
							
						} catch (JsonSyntaxException e) {
							GTS.GTSLog.log(Level.WARN, entry.getName() + " is not a available GTS Pack Config. It was skipped. -> " + e.getMessage() );
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
					else if (entry.getName().endsWith(".ogg")) {
						// サウンドファイルだった場合
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.toIntExact(entry.getSize()))) {
							byte[] tmp = new byte[Math.toIntExact(entry.getSize())];
							int read = 0;
							while (read < entry.getSize()) {
								int already =  zis.read(tmp, read, (int) (entry.getSize()-read));
								read += already;
							}
							baos.write(tmp);
							
							sounds.put(entry.getName(), tmp);
							
							
						} catch (IOException e) {
							// ファイル異常で読み込みすらできない場合
							GTS.GTSLog.log(Level.WARN, "Can't load" + entry.getName() + " some reason. -> " + e.getMessage());
						}
					}
					
				}
				
				// サウンドファイルに対してsound.jsonを作り上げる
				ProgressManager.ProgressBar bar = ProgressManager.push("GTS Sound JSON Parse", sounds.size());
				for (String path: sounds.keySet()) {
					GTSSoundJson.GTSSoundJsonChild c = soundJson.new GTSSoundJsonChild();
					List<String> l = new ArrayList<>();
					l.add(path);
					c.sounds = l;
					soundJson.content.put(path.replace("/", "."), c); // 追加
					bar.step(path);
				}
				
				ProgressManager.pop(bar);
				
				// 各種コンフィグに対して処理を追加する
				ArrayList<ModelBase> m = new ArrayList<>(); // 正常に追加したパックモデル
				bar = ProgressManager.push("GTS Model Config Parse", configs.size());
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
						
						// テクスチャをそれぞれ読み込む（ただし同じテクスチャの場合はスキップ）
						ConfigTrafficLight.TexturePath t = config.getTextures(); // もうめんどくさいので一回読み込み
						t.setBaseTex(textures.get(t.getBase()));
						t.setLightTex(textures.get(t.getLight()));
						t.setNoLightTex(textures.get(t.getNoLight())); // 以上、テクスチャのセット
						
						m.add(new ModelTrafficLight(config, models.get(config.getModel()), pack, true)); // モデルは絶対にあるはずなので
					}
					else if (configBase instanceof ConfigTrafficPole) {
						ConfigTrafficPole config = (ConfigTrafficPole) configBase;
						if (config.getTexture() == null) {
							GTS.GTSLog.log(Level.WARN, config.getId() + " didn't declare Texture. This model was skipped.");
							continue;
						}
						
						if (!textures.containsKey(config.getTexture())) {
							// テクスチャが存在しない
							GTS.GTSLog.warn(config.getTexture() + " is not found in this pack. This model was skipped.");
						}
						
						config.setTexImage(textures.get(config.getTexture()));
						m.add(new ModelTrafficPole(config, models.get(config.getModel()), pack));
						
					}
					else if (configBase instanceof ConfigTrafficButton) {
						ConfigTrafficButton config = (ConfigTrafficButton) configBase;
						if (config.getTextures() == null || config.getTextures().size() == 0) {
							GTS.GTSLog.log(Level.WARN, config.getId() + " didn't declare Texture. This model was skipped.");
							continue;
						}
						
						if (!textures.containsKey(config.getBaseTexture())) {
							GTS.GTSLog.warn(config.getBaseTexture() + " is not found in this pack. This model was skipped.");
						}
						
						config.setBaseTex(textures.get(config.getBaseTexture()));
						
						if (!textures.containsKey(config.getBaseTexture())) {
							GTS.GTSLog.warn("Push Texture " + config.getBaseTexture() + " is not found in this pack. Base texture use.");
							config.setPushTex(textures.get(config.getBaseTexture()));
						}
						else {
							config.setPushTex(textures.get(config.getPushTexture()));
						}
						m.add(new ModelTrafficButton(config, models.get(config.getModel()), pack));
						
					}
					bar.step(configBase.getId());
					
					
				}
				ProgressManager.pop(bar);
				
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
		this.sounds.put(pack, sounds);
		
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
		this.completeLoad = false;
		if (!search.exists() || !search.isDirectory()) throw new IllegalArgumentException("Only exists directory can be input."); // ないかディレクトリじゃない
		GTS.GTSLog.log(Level.INFO, "Pack search started.");
		File[] zips = search.listFiles((dir, name) -> (name.endsWith("zip"))); // Zipファイルだけを選別
		if (zips == null) throw new IOException("Can't find pack at search directory.");
		ProgressManager.ProgressBar bar = ProgressManager.push("GTS Model Pack Search", zips.length);
		for (File zip: zips) {
			bar.step(zip.getName());
			// 見つかったZipファイルの数だけ繰り返す
			Pack p = getPack(zip); // パックを探す
			if (p != null) {
				// Nullじゃなければ
				packs.put(zip, p);
				GTS.GTSLog.log(Level.INFO, "GTS Addon Pack '" + p.getName() + "' in " + zip.getAbsolutePath() + " loaded. (models: " + p.getModels().size() + ")");
			}
			
		}
		JsonObject jo = new JsonObject();
		for (Map.Entry<String, GTSSoundJson.GTSSoundJsonChild> map : soundJson.content.entrySet()) {
			JsonObject jo2 = new JsonObject();
			jo2.addProperty("category", map.getValue().category);
			jo2.addProperty("stream", map.getValue().stream);
			JsonArray jo3 = new JsonArray();
			jo3.add("d_gts:" + map.getValue().sounds.get(0));
			jo2.add("sounds", jo3);
			
			jo.add(map.getKey(), jo2);
		}
		soundJsonString = jo.toString();
		System.out.println(soundJsonString);
		GTS.GTSLog.log(Level.INFO, "Pack search ended. Add " + packs.size() +  " packs.");
		this.completeLoad = true;
		ProgressManager.pop(bar);
	}
}
