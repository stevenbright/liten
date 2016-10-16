package liten.test.integration.website;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alexander Shabanov
 */
public class RestControllerTest extends AbstractIntegrationTestBase {

  @BeforeClass
  public static void initServer() {
    initServer(null, "classpath:/litenIntegrationTest/");
  }

  @Test
  public void shouldPass() {
    //....
  }
}
