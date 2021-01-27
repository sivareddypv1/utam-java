package declarative.grammar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import declarative.helpers.TranslationContext;
import declarative.representation.PageClassField;
import declarative.representation.TypeProvider;
import declarative.representation.UtilityMethod;
import framework.consumer.UtamError;

import java.util.regex.Pattern;


/**
 * imperative utility JSON mapping
 * @author elizaveta.ivanova
 * @since 228
 */
class UtamMethodUtil {
  static final String ERR_METHOD_WRONG_UTILS_TYPE =
          "method '%s': extension class %s should be from same namespace '%s', was '%s'";

  final String type;
  final String apply;
  final UtamArgument[] args;

  @JsonCreator
  UtamMethodUtil(
      @JsonProperty(value = "type", required = true) String type,
      @JsonProperty(value = "apply", required = true) String apply,
      @JsonProperty(value = "args") UtamArgument[] args) {
    this.type = type;
    this.apply = apply;
    this.args = args;
  }

  UtilityMethod.Utility getMethodReference(String methodName, TranslationContext context) {
    TypeProvider utilityType = getExtensionType(methodName, context);
    PageClassField field = context.setUtilityField(utilityType);
    return new UtilityMethod.Utility(
        apply, UtamArgument.unknownTypesParameters(args, methodName).getOrdered(), field);
  }

  private TypeProvider getExtensionType(String methodName, TranslationContext context) {
    TypeProvider classType = context.getClassType();
    TypeProvider utilityType = context.getUtilityType(type);
    String pageNameSpace = classType.getFullName().split(Pattern.quote("."))[1];
    String utilNameSpace = utilityType.getFullName().split(Pattern.quote("."))[1];
    if (!pageNameSpace.equals(utilNameSpace)) {
      throw new UtamError(
          String.format(
              ERR_METHOD_WRONG_UTILS_TYPE, methodName, type, pageNameSpace, utilNameSpace));
    }
    return utilityType;
  }
}
