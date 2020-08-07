/*
 * ADOBE CONFIDENTIAL
 * __________________
 *
 * Copyright 2020 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */
package com.adobe.target.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArtifactUnobfuscatorTest {

  private static final String KEY = "test";
  private static final String CONTENT = "Hello World!";
  private static final byte[] OBFUSCATED_CONTENT = {
      65, 84, 79, 68, 58, 48, 48, 49, 46, 92, 106, 105, 81, 99, 44, 120, 48, 81,
      123, 49, 37, 55, 87, 108, 55, 46, 54, 50, 85, 39, 104, 105, 39, 108, 95, 72,
      50, 118, 126, 93, 60, 0, 31, 24, 65, 124, 61, 6, 35, 15, 72, 89
  };

  private final ArtifactUnobfuscator artifactUnobfuscator = new ArtifactUnobfuscator();

  @Test
  void testUnobfucate() {
    String result = artifactUnobfuscator.unobfuscate(KEY, OBFUSCATED_CONTENT);
    assertEquals(result, CONTENT);
  }

  @Test
  void throwExceptionBecauseOfInvalidArtifact() {
    byte[] badContent = { 65, 84, 79, 68, 58 };

    assertThrows(TargetInvalidArtifactException.class, () -> {
      artifactUnobfuscator.unobfuscate(KEY, badContent);
    });
  }

  @Test
  void throwExceptionBecauseOfInvalidVersion() {
    byte[] badContent = new byte[OBFUSCATED_CONTENT.length];
    System.arraycopy(OBFUSCATED_CONTENT, 0, badContent, 0, OBFUSCATED_CONTENT.length);
    badContent[0] = 64;

    assertThrows(TargetInvalidArtifactException.class, () -> {
      artifactUnobfuscator.unobfuscate(KEY, badContent);
    });
  }

}
