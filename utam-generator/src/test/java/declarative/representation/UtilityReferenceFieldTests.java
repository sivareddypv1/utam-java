package declarative.representation;

import declarative.helpers.AnnotationUtils;
import org.testng.annotations.Test;

import static declarative.representation.UtilityMethodTests.getUtilityType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author elizaveta.ivanova
 * @since 228
 */
public class UtilityReferenceFieldTests {

  @Test
  public void validType() {
    TypeProvider utilityClassType = getUtilityType();
    PageClassField field = new UtilityReferenceField(utilityClassType);
    assertThat(field.getName(), is(equalTo("utilUtilityClass")));
    assertThat(
        field.getDeclaration(),
        is(equalTo("private final UtilityClass utilUtilityClass = getUtility(UtilityClass.class)")));
    assertThat(field.getAnnotations(), is(equalTo(AnnotationUtils.EMPTY_ANNOTATIONS)));
  }
}
