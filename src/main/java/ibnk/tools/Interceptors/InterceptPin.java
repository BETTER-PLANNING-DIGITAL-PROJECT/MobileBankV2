package ibnk.tools.Interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptPin {
    // You can define elements here if needed, for example:
    // String value() default ""; // Example of a parameterized annotation element
}
