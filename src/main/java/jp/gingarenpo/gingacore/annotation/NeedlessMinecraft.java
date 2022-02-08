package jp.gingarenpo.gingacore.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * ソースの可読性を増やすだけのアノテーションです。これがついているメソッドは、Minecraftを起動していなくても
 * 動作することが保証されています（直接叩ける）。
 * @author 銀河連邦
 *
 */
@Retention(SOURCE)
@Target({METHOD, CONSTRUCTOR})
public @interface NeedlessMinecraft {

}
