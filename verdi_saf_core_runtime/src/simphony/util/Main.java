package simphony.util;

import saf.core.runtime.Boot;

/**
 * Forwards calls to Main to Boot. This is useful when
 * you want to make an executable jar but for working directory
 * reasons that jar cannot be the saf.runtime jar.
 *
 * @author Nick Collier
 */
public class Main {

  // forwards to Boot.
  public static void main(String[] args) {
    Boot.main(args);
  }
}


