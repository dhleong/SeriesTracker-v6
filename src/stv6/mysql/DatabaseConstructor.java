package stv6.mysql;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * The DatabaseConstructor annotation should be applied
 * 	to whatever constructor you want to be used with
 * 	a {@link ClassRetriever}. If you've only a single
 * 	constructor, then you don't need to bother.
 * 
 * @author dhleong
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
public @interface DatabaseConstructor {

}
