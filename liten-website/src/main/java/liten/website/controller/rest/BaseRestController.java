package liten.website.controller.rest;

import com.truward.brikar.error.helper.ExceptionResponseUtil;
import com.truward.brikar.server.controller.AbstractRestController;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public abstract class BaseRestController extends AbstractRestController {

  @Override
  protected Object getResponseObjectFromException(@Nonnull Throwable e) {
    return ExceptionResponseUtil.shallowConvert(e);
  }
}
