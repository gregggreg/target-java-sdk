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
import java.util.Arrays;

public class ArtifactObfuscator {

  private static final int RANDOM_KEY_SIZE = 32;
  private static final byte[] HEADER_VERSION = "ATOD:001".getBytes(StandardCharsets.UTF_8);

  public String deobfuscate(String obfuscationKey, byte[] content) throws TargetInvalidArtifactException {
    validateContent(content);

    byte[] firstPart = obfuscationKey.getBytes(StandardCharsets.UTF_8);
    byte[] secondPart = extractRandomKey(content);
    byte[] key = buildKey(firstPart, secondPart);
    byte[] result = xor(key, content, true);

    return new String(result, StandardCharsets.UTF_8);
  }

  public byte[] obfuscate(String firstKey, String secondKey, byte[] content) {
    byte[] firstPart = firstKey.getBytes(StandardCharsets.UTF_8);
    byte[] secondPart = secondKey.getBytes(StandardCharsets.UTF_8);
    byte[] key = buildKey(firstPart, secondPart);
    byte[] encrypted = xor(key, content, false);

    return addHeaderAndVersion(secondPart, encrypted);
  }

  protected byte[] buildKey(byte[] firstPart, byte[] secondPart) {
    byte[] result = new byte[firstPart.length + secondPart.length];

    System.arraycopy(firstPart, 0, result, 0, firstPart.length);
    System.arraycopy(secondPart, 0, result, firstPart.length, secondPart.length);

    return result;
  }

  protected byte[] xor(byte[] key, byte[] content, boolean skipHeader) {
    int headerLength = skipHeader ? HEADER_VERSION.length + RANDOM_KEY_SIZE : 0;
    byte[] result = new byte[content.length - headerLength];

    for (int i = headerLength, j = 0; i < content.length; i++, j++) {
      result[j] = (byte) (content[i] ^ key[j % key.length]);
    }

    return result;
  }

  protected void validateContent(byte[] content) throws TargetInvalidArtifactException {
    if (content.length <= HEADER_VERSION.length + RANDOM_KEY_SIZE) {
      throw new TargetInvalidArtifactException("Invalid artifact");
    }

    if (!isHeaderVersionValid(content)) {
      throw new TargetInvalidArtifactException("Invalid artifact version");
    }
  }

  protected boolean isHeaderVersionValid(byte[] content) {
    return Arrays.equals(HEADER_VERSION, extractHeaderVersion(content));
  }

  private byte[] extractHeaderVersion(byte[] content) {
    byte[] result = new byte[HEADER_VERSION.length];
    System.arraycopy(content, 0, result, 0, HEADER_VERSION.length);
    return result;
  }

  private byte[] extractRandomKey(byte[] content) {
    byte[] result = new byte[RANDOM_KEY_SIZE];
    System.arraycopy(content, HEADER_VERSION.length, result, 0, RANDOM_KEY_SIZE);
    return result;
  }

  private byte[] extractObfuscatedArtifact(byte[] content) {
    int artifactSize = content.length - HEADER_VERSION.length - RANDOM_KEY_SIZE;
    byte[] result = new byte[artifactSize];
    System.arraycopy(content, HEADER_VERSION.length + RANDOM_KEY_SIZE, result, 0, artifactSize);
    return result;
  }

  protected byte[] addHeaderAndVersion(byte[] randomKey, byte[] content) {
    byte[] result = new byte[HEADER_VERSION.length + randomKey.length + content.length];

    System.arraycopy(HEADER_VERSION, 0, result, 0, HEADER_VERSION.length);
    System.arraycopy(randomKey, 0, result, HEADER_VERSION.length, randomKey.length);
    System.arraycopy(content, 0, result, HEADER_VERSION.length + randomKey.length, content.length);

    return result;
  }
}
