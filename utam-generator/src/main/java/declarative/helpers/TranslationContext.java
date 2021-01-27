package declarative.helpers;

import declarative.representation.*;
import declarative.translator.ProfileConfiguration;
import declarative.translator.TranslationTypesConfig;
import declarative.translator.TranslatorConfig;
import framework.consumer.UtamError;
import framework.context.Profile;

import java.util.*;

import static declarative.helpers.ElementContext.ROOT_ELEMENT_NAME;

/**
 * instance is created for every Page Object that is being translated <br>
 * contains reference to the Page Object context and translator context
 *
 * @author elizaveta.ivanova
 * @since 228
 */
public final class TranslationContext {

  static final String ERR_CONTEXT_ELEMENT_NOT_FOUND =
      "referenced element '%s' not found in context";
  static final String ERR_CONTEXT_DUPLICATE_PARAMETERS =
      "duplicate parameters with name '%s' in method '%s'";
  static final String ERR_CONTEXT_DUPLICATE_METHOD = "duplicate method '%s'";
  static final String ERR_CONTEXT_DUPLICATE_FIELD = "duplicate field '%s'";
  static final String ERR_CONTEXT_DUPLICATE_ELEMENT_NAME =
      "element with name '%s' already exists in same JSON";

  private final List<PageClassField> pageObjectFields = new ArrayList<>();
  private final List<PageObjectMethod> pageObjectMethods = new ArrayList<>();
  private final Set<String> methodNames = new HashSet<>();
  private final Map<String, ElementContext> elementContextMap =
      Collections.synchronizedMap(new HashMap<>());
  private final String pageObjectURI;
  private final Validation validation;
  private final TranslationTypesConfig translationTypesConfig;
  private final TranslatorConfig translatorConfiguration;
  private final Set<String> usedPrivateMethods = new HashSet<>();
  private boolean isAbstractPageObject = false;
  private final Map<String, ElementUnitTestHelper> testableElements = new HashMap<>();

  public TranslationContext(String pageObjectURI, TranslatorConfig translatorConfiguration) {
    this.pageObjectURI = pageObjectURI;
    this.validation = new Validation(pageObjectURI, elementContextMap);
    this.translationTypesConfig = translatorConfiguration.getTranslationTypesConfig();
    this.translatorConfiguration = translatorConfiguration;
  }

  private static void checkParameters(PageObjectMethod method) {
    if (method.getDeclaration().getParameters().isEmpty()) {
      return;
    }
    List<MethodParameter> parameters = new ArrayList<>(method.getDeclaration().getParameters());
    parameters.removeIf(MethodParameter::isLiteral);
    for (int i = 0; i < parameters.size(); i++) {
      String name = parameters.get(i).getValue();
      for (int j = i + 1; j < parameters.size(); j++) {
        if (parameters.get(j).getValue().equals(name)) {
          throw new UtamError(
              String.format(
                  ERR_CONTEXT_DUPLICATE_PARAMETERS, name, method.getDeclaration().getName()));
        }
      }
    }
  }

  public void setAbstract() {
    this.isAbstractPageObject = true;
  }

  public boolean isAbstractPageObject() {
    return isAbstractPageObject;
  }

  public TypeProvider getInterfaceType(String implementsProperty) {
    if (implementsProperty != null) {
      return translationTypesConfig.getInterfaceType(implementsProperty);
    }
    return translationTypesConfig.getInterfaceType(pageObjectURI);
  }

  public TypeProvider getClassType() {
    return translationTypesConfig.getClassType(pageObjectURI);
  }

  public TypeProvider getType(String type) {
    return translationTypesConfig.getInterfaceType(type);
  }

  public TypeProvider getUtilityType(String type) {
    return translationTypesConfig.getUtilityType(type);
  }

  public void setElement(ElementContext element) {
    if (elementContextMap.containsKey(element.getName())) {
      throw new UtamError(String.format(ERR_CONTEXT_DUPLICATE_ELEMENT_NAME, element.getName()));
    }
    validation.testLocalDuplicates(element);
    validation.testGlobalDuplicates(element);
    elementContextMap.put(element.getName(), element);
  }

  public void setClassField(PageClassField field) {
    if (pageObjectFields.stream().anyMatch(f -> f.getName().equals(field.getName()))) {
      throw new UtamError(String.format(ERR_CONTEXT_DUPLICATE_FIELD, field.getName()));
    }
    pageObjectFields.add(field);
  }

  public PageClassField setUtilityField(TypeProvider provider) {
    PageClassField field = new UtilityReferenceField(provider);
    // for utilities field might have been already declared
    if (pageObjectFields.stream().noneMatch(f -> f.getName().equals(field.getName()))) {
      pageObjectFields.add(field);
    }
    return field;
  }

  public void setMethod(PageObjectMethod method) {
    // first check if same method already exists
    if (methodNames.contains(method.getDeclaration().getName())) {
      throw new UtamError(
          String.format(ERR_CONTEXT_DUPLICATE_METHOD, method.getDeclaration().getName()));
    }
    checkParameters(method);
    // no duplicates - add method
    methodNames.add(method.getDeclaration().getName());
    pageObjectMethods.add(method);
  }

  public ElementContext getRootElement() {
    return getElement(ROOT_ELEMENT_NAME);
  }

  public ElementContext getElement(String name) {
    if (!elementContextMap.containsKey(name)) {
      throw new UtamError(String.format(ERR_CONTEXT_ELEMENT_NOT_FOUND, name));
    }
    return elementContextMap.get(name);
  }

  public List<PageObjectMethod> getMethods() {
    return pageObjectMethods;
  }

  public List<PageClassField> getFields() {
    return pageObjectFields;
  }

  public Profile getProfile(String name, String value) {
    ProfileConfiguration profileConfiguration =
        translatorConfiguration.getProfileConfiguration(name);
    return profileConfiguration.getFromString(value);
  }

  /**
   * temporary refactoring code: if private method was never used, we should not declare it it might
   * happen with getter for private element if it's only used as scope and never in compose
   *
   * @param name method name
   */
  public void setPrivateMethodUsage(String name) {
    usedPrivateMethods.add(name);
  }

  public Set<String> getUsedPrivateMethods() {
    return usedPrivateMethods;
  }

  public PageObjectMethod getMethod(String name) {
    return pageObjectMethods.stream()
        .filter(pageObjectMethod -> pageObjectMethod.getDeclaration().getName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  /**
   * used from unit test deserializer to get full list of elements <br>
   * some elements are not declared as fields
   * @return collection of element contexts
   */
  public Map<String, ElementUnitTestHelper> getTestableElements() {
    return testableElements;
  }


  /**
   * remember element for unit test deserializer
   * @param elementName name of the element
   * @param helper information used to generate unit test
   */
  public void setTestableElement(String elementName, ElementUnitTestHelper helper) {
    this.testableElements.put(elementName, helper);
  }

}
