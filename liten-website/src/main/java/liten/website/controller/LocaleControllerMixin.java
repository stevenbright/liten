package liten.website.controller;

import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface LocaleControllerMixin {

  default Locale getUserLocale() {
    final Locale locale = LocaleContextHolder.getLocale();
    assert locale != null;
    return locale;
  }

  default String getUserLanguage() {
    return getUserLocale().getLanguage();
  }
}
