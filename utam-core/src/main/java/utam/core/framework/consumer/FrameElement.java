/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.core.framework.consumer;

import utam.core.element.BasicElement;
import utam.core.element.Element;

/**
 * page object element that represents a frame or iframe
 *
 * @author james.evans
 * @since 236
 */
public interface FrameElement extends BasicElement {

  /**
   * gets the underlying Element object representing the frame
   *
   * @return the frame Element object
   */
  Element getFrameElement();
}
