package com.agentboard.e2e.support;

/**
 * Unique value generators for test data.
 */
public final class Generators {

  private Generators() {}

  /**
   * Generates a unique email address safe for test use.
   *
   * @return email string of the form {@code user-<timestamp>-<random>@test.agentboard.dev}
   */
  public static String generateEmail() {
    return generateEmail("user");
  }

  /**
   * Generates a unique email address with a custom prefix.
   *
   * @param prefix logical prefix for the generated address
   * @return unique email address
   */
  public static String generateEmail(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "-"
        + Long.toString(Double.doubleToLongBits(Math.random()), 36).substring(0, 5)
        + "@test.agentboard.dev";
  }

  /**
   * Generates a unique workspace name safe for test use.
   *
   * @return workspace name of the form {@code Tenant-<timestamp>-<random>}
   */
  public static String generateTenantName() {
    return "Tenant-" + System.currentTimeMillis() + "-"
        + Long.toString(Double.doubleToLongBits(Math.random()), 36).substring(0, 4);
  }
}
