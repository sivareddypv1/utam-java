package utam.compiler.helpers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.expectThrows;
import static utam.compiler.helpers.TypeUtilities.PAGE_OBJECT;
import static utam.compiler.helpers.TypeUtilities.ROOT_PAGE_OBJECT;

import java.util.List;
import org.testng.annotations.Test;
import utam.compiler.UtamCompilationError;
import utam.compiler.grammar.DeserializerUtilities;
import utam.compiler.representation.PageObjectValidationTestHelper;
import utam.compiler.representation.PageObjectValidationTestHelper.MethodInfo;
import utam.compiler.representation.PageObjectValidationTestHelper.MethodParameterInfo;
import utam.core.declarative.representation.PageObjectMethod;
import utam.core.framework.consumer.UtamError;

/**
 * @author elizaveta.ivanova
 * @since 236
 */
public class ReturnTypeTests {

  private static final String METHOD_NAME = "test";

  private static PageObjectMethod test(String jsonFileName) {
    return new DeserializerUtilities().getContext(jsonFileName).getMethod(METHOD_NAME);
  }

  private static void test(String jsonFile, String expectedError) {
    UtamError e = expectThrows(UtamCompilationError.class,
        () -> test("validate/return/" + jsonFile));
    assertThat(e.getMessage(), containsString(expectedError));
  }

  @Test
  public void testReturnThrowsForObject() {
    test("returnObject",
        "error 10: method \"test\": property \"returnType\" should be a non empty string, instead found object");
  }

  @Test
  public void testReturnAllRedundant() {
    test("returnAllRedundant",
        "error 603: method \"test\" statement: \"returnAll\" property can't be set without setting return type in a compose statement");
  }

  @Test
  public void testReturnStringUnsupported() {
    test("returnIncorrectString",
        "error 602: method \"test\" statement: return type \"container\" is not supported in a compose statement");
  }

  @Test
  public void testReturnAllRedundantForMethodThrows() {
    test("returnMethodAllRedundant",
        "error 402: abstract method \"test\": \"returnAll\" property can't be set without setting return type");
  }

  @Test
  public void testReturnTypeNotAllowedThrows() {
    test("returnTypeMethodNotAllowed",
        "error 500: incorrect format of compose method: \nUnrecognized field \"returnType\"");
  }

  @Test
  public void testReturnStringUnsupportedForMethodThrows() {
    test("returnMethodIncorrectString",
        "error 403: abstract method \"test\": return type \"container\" is not supported");
  }

  @Test
  public void testContainerWithLiteralReturn() {
    PageObjectMethod method = test("compose/return/containerLiteralReturn");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "Foo");
    assertThat(method.getDeclaration().getCodeLine(), is("Foo test()"));
    expected.addImpliedImportedTypes("my.pack.Foo");
    expected.addImportedTypes("my.pack.Foo");
    expected.addCodeLine("Foo statement0 = this.getContainer(Foo.class)");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testContainerWithNonLiteralReturn() {
    PageObjectMethod method = test("compose/return/containerNonLiteralReturn");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "T");
    assertThat(method.getDeclaration().getCodeLine(), is("<T extends PageObject> T test(Class<T> param1)"));
    expected.addParameter(new MethodParameterInfo("param1", "Class<T>"));
    expected.addImpliedImportedTypes(PAGE_OBJECT.getFullName());
    expected.addImportedTypes(PAGE_OBJECT.getFullName());
    expected.addCodeLine("T statement0 = this.getContainer(param1)");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testReturnNonLiteralPageObjectList() {
    PageObjectMethod method = test("compose/return/containerNonLiteralReturnList");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "List<T>");
    assertThat(method.getDeclaration().getCodeLine(), is("<T extends PageObject> List<T> test(Class<T> param1)"));
    expected.addParameter(new MethodParameterInfo("param1", "Class<T>"));
    expected.addImpliedImportedTypes(PAGE_OBJECT.getFullName(), List.class.getName());
    expected.addImportedTypes(PAGE_OBJECT.getFullName(), List.class.getName());
    expected.addCodeLine("List<T> statement0 = this.getContainer(param1)");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testReturnLiteralPageObjectList() {
    PageObjectMethod method = test("compose/return/containerLiteralReturnList");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "List<Foo>");
    assertThat(method.getDeclaration().getCodeLine(), is("List<Foo> test()"));
    expected.addImpliedImportedTypes("my.pack.Foo", List.class.getName());
    expected.addImportedTypes("my.pack.Foo", List.class.getName());
    expected.addCodeLine("List<Foo> statement0 = this.getContainer(Foo.class)");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testContainerListShouldThrowWithoutReturnAll() {
    // container element has "returnAll" and its invocation return type does not say "returnAll"
    test("containerListThrows", "error 613: method \"test\" statement: "
        + "incorrect return type; expected \"List<T>\", provided is \"Foo\"");
  }

  @Test
  public void testContainerWrongBoundTypeShouldThrow() {
    // container is invoked and has "returnType" : "string" instead page object
    test("containerWrongBoundTypeThrows", "error 613: method \"test\" statement: "
        + "incorrect return type; expected \"List<T>\", provided is \"List<String>\"");
  }

  @Test
  public void testReturnRootPageObject() {
    PageObjectMethod method = test("compose/return/returnRootPageObject");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "T");
    assertThat(method.getDeclaration().getCodeLine(), is("<T extends RootPageObject> T test()"));
    expected.addImpliedImportedTypes(ROOT_PAGE_OBJECT.getFullName());
    expected.addImportedTypes(ROOT_PAGE_OBJECT.getFullName());
    expected.addCodeLine("T statement0 = this.myPrivateMethod()");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testReturnRootPageObjectList() {
    PageObjectMethod method = test("compose/return/returnRootPageObjectList");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "List<T>");
    assertThat(method.getDeclaration().getCodeLine(), is("<T extends RootPageObject> List<T> test()"));
    expected.addImpliedImportedTypes(ROOT_PAGE_OBJECT.getFullName(), List.class.getName());
    expected.addImportedTypes(ROOT_PAGE_OBJECT.getFullName(), List.class.getName());
    expected.addCodeLine("List<T> statement0 = this.myPrivateMethod()");
    expected.addCodeLine("return statement0");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }

  @Test
  public void testReturnListWithNameCollision() {
    PageObjectMethod method = test("compose/return/namesCollisionReturnsList");
    MethodInfo expected = new MethodInfo(METHOD_NAME, "List<my2.pack.Foo>");
    assertThat(method.getDeclaration().getCodeLine(), is("List<my2.pack.Foo> test()"));
    expected.addImpliedImportedTypes("my1.pack.Foo", List.class.getName(), TypeUtilities.COLLECTOR_IMPORT.getFullName());
    expected.addImportedTypes(List.class.getName());
    expected.addCodeLine("List<Foo> statement0 = this.myPrivateMethod()");
    expected.addCodeLine(
        "List<my2.pack.Foo> statement1 = statement0.stream().flatMap(element -> element.myPrivateMethod()"
            + ".stream()).collect(Collectors.toList())");
    expected.addCodeLine("return statement1");
    PageObjectValidationTestHelper.validateMethod(method, expected);
  }
}
