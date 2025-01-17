/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.core.declarative.translator;

import utam.core.declarative.representation.TypeProvider;

import java.io.IOException;
import java.io.Writer;

/**
 * configuration of source with JSON files and target for generated Java files
 *
 * @author elizaveta.ivanova
 * @since 228
 */
public interface TranslatorTargetConfig {

  /**
   * writer for generated type
   *
   * @param typeProvider type of the PO, can be interface or class
   * @return writer for class
   * @throws IOException if the file does not exist
   */
  Writer getClassWriter(TypeProvider typeProvider) throws IOException;

  /**
   * get unit test writer to write unit test generated for new page object
   *
   * @param typeProvider type of the Page Object to generate test for
   * @return writer
   * @throws IOException if file does not exist
   */
  Writer getUnitTestWriter(TypeProvider typeProvider) throws IOException;

  /**
   * get configured type for unit test generator
   *
   * @return type of the unit tests to generate
   */
  UnitTestRunner getUnitTestRunnerType();

  /**
   * full path to the injection configuration files directory <br>
   * translator will write into dependency configs <br>
   * then Page Objects Provider will read from those to configure dependencies
   *
   * @return string with full path to resources folder with configs
   */
  String getInjectionConfigRootFilePath();
}
