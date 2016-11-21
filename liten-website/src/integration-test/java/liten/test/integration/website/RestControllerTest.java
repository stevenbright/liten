package liten.test.integration.website;

import com.truward.brikar.client.rest.RestOperationsFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static org.junit.Assert.assertTrue;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * @author Alexander Shabanov
 */
public final class RestControllerTest extends AbstractIntegrationTestBase {

  @BeforeClass
  public static void initServer() {
    initServer(null, "classpath:/litenIntegrationTest/");
  }

  @Test
  public void shouldGetPublicPages() {
    final URI baseUri = getServerUrl("/");

    try (final RestOperationsFactory rof = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final RestOperations ro = rof.getRestOperations();

      final URI uri = fromUri(baseUri).pathSegment("g").pathSegment("index").build().toUri();
      final ResponseEntity<String> entity = ro.getForEntity(uri, String.class);
      final String body = entity.getBody();

      assertTrue(body.length() > 0);
    }
  }
}
