package jp.gingarenpo.gts.pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sound.jsonを明示的に作成するためのクラス。
 * こうしないとおそらくSoundEventで読み込んでくれない。消すかも
 *
 * GSON
 */
public class GTSSoundJson {

	public Map<String, GTSSoundJsonChild> content = new HashMap<>();

	public class GTSSoundJsonChild {
		public String category = "master";
		public List<String> sounds = new ArrayList<>();
		public boolean stream;
	}
}
