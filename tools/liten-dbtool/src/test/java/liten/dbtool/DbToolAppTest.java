package liten.dbtool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/DbToolAppTest-context.xml")
public final class DbToolAppTest {

  @Test
  public void shouldPass() {
    assertEquals(1, 1);
  }
}
