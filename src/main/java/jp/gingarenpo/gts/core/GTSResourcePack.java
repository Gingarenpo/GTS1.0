package jp.gingarenpo.gts.core;

import com.google.common.collect.ImmutableSet;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

/**
 * Minecraftにサウンドリソースパックの場所を教えてあげたりする、
 * リソースパックマネージャーのようなもの。デフォルトのものだとassetsを見に行ってしまうので
 * loaderから見にいかせるようにする。
 */
public class GTSResourcePack implements IResourcePack {
	
	/**
	 * 指定したリソースを読み込む。
	 * @param location
	 * @return
	 * @throws IOException
	 */
	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		System.out.println(location + " loading...");
		if (GTS.loader == null) {
			return null; // 読み込まれていないのでそもそもあるわけがない
		}
		if (location.getPath().equals("dynamic_sound.json")) {
			// sound.jsonは動的に用意したこれを返す（sound.jsonだとGTSのデフォルトが返るのでdynamic_とつける）
			return new ByteArrayInputStream(GTS.loader.getSoundJsonString().getBytes(StandardCharsets.UTF_8));
		}
		for (HashMap<String, BufferedImage> tex: GTS.loader.getTextures().values()) {
			if (tex.containsKey(location.getPath())) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					ImageIO.write(tex.get(location.getPath()), "png", baos);
					return new ByteArrayInputStream(baos.toByteArray());
				}
			}
		}
		for (HashMap<String, byte[]> sound: GTS.loader.getSounds().values()) {
			if (sound.containsKey(location.getPath())) {
				return new ByteArrayInputStream(sound.get(location.getPath()));
			}
		}
		
		return null; // どこにも存在しない
	}
	
	/**
	 * 指定したロケーションがリソースとして存在しうるかを返す。
	 * @param location
	 * @return
	 */
	@Override
	public boolean resourceExists(ResourceLocation location) {
		// あるかどうかだけなのでとりあえずキーがあるかどうかで判断する
		if (GTS.loader == null) {
			return false; // 読み込まれていないのでそもそもあるわけがない
		}
		for (HashMap<String, BufferedImage> tex: GTS.loader.getTextures().values()) {
			if (tex.containsKey(location.getPath())) return true; // 存在する
		}
		for (HashMap<String, byte[]> sound: GTS.loader.getSounds().values()) {
			if (sound.containsKey(location.getPath())) return true; // 存在する
		}
		return false; // どこにも存在しない
	}
	
	/**
	 * GTSのドメインでパックのルートディレクトリからアクセスできるようにする
	 * @return
	 */
	@Override
	public Set<String> getResourceDomains() {
		return ImmutableSet.of("gts");
	}
	
	@Nullable
	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
		return null;
	}
	
	@Override
	public BufferedImage getPackImage() throws IOException {
		return null;
	}
	
	@Override
	public String getPackName() {
		return "GTS Resource Pack";
	}
	
}
