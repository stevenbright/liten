package liten.website.controller;

import com.truward.orion.user.service.spring.SecurityControllerMixin;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

/**
 * Base controller that serves HTML.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class BaseHtmlController implements SecurityControllerMixin {

  @Nullable
  @ModelAttribute("userAccount")
  public UserDetails getUserAccountParameter() {
    return getUserAccount();
  }

  public Locale getUserLocale() {
    final Locale locale = LocaleContextHolder.getLocale();
    assert locale != null;
    return locale;
  }

  public String getUserLanguageCode() {
    return getUserLocale().getLanguage();
  }
}
