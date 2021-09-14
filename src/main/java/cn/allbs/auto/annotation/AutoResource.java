package cn.allbs.auto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 自动注入
 *
 * @author ChenQi
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface AutoResource {
}
