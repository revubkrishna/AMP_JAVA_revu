package utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to identify who created test methods and when.
 *
 * to use multiple values, use the notation in this way:
 *
 * @Author(user = "nagmeka", date ="MM/DD/YYYY", testID = "HYB-001")
 */
@Target({ ElementType.METHOD })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ATFTestDetails
{
   String user();


   String date();


   String displayName();
}
