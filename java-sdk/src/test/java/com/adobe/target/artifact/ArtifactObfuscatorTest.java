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

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArtifactObfuscatorTest {

  private static final String KEY = "test";
  private static final String CONTENT = "Hello World!";

  private final ArtifactObfuscator artifactObfuscator = new ArtifactObfuscator();
  private final byte[] obfuscatedContent;

  public ArtifactObfuscatorTest() {
    String randomKey = "12345678901234567890123456789012";
    obfuscatedContent = artifactObfuscator.obfuscate(KEY, randomKey, CONTENT.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void testUnobfucate() {
    String result = artifactObfuscator.unobfuscate(KEY, obfuscatedContent);
    assertEquals(result, CONTENT);
  }

  @Test
  void throwExceptionBecauseOfInvalidArtifact() {
    byte[] badContent = { 65, 84, 79, 68, 58 };

    assertThrows(TargetInvalidArtifactException.class, () -> {
      artifactObfuscator.unobfuscate(KEY, badContent);
    });
  }

  @Test
  void throwExceptionBecauseOfInvalidVersion() {
    byte[] badContent = new byte[obfuscatedContent.length];
    System.arraycopy(obfuscatedContent, 0, badContent, 0, obfuscatedContent.length);
    badContent[0] = 64;

    assertThrows(TargetInvalidArtifactException.class, () -> {
      artifactObfuscator.unobfuscate(KEY, badContent);
    });
  }

}
