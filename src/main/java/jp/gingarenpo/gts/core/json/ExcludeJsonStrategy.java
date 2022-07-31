package jp.gingarenpo.gts.core.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * 指定されたアノテーション（ここでは「@Exclude」）がついているフィールドを
 * Gsonのシリアライズ対象外にするクラス。これをGson作成時に指定することでそのフィールドは除外される。
 */
public class ExcludeJsonStrategy implements ExclusionStrategy {
	/**
	 * 指定したフィールドに「@exclude」が含まれていた場合、スキップする。
	 * @param f
	 * @return
	 */
	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(Exclude.class) != null;
	}
	
	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}
}
