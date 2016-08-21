package liten.website.controller;

import com.truward.orion.user.service.spring.UserIdRoleUtil;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
public interface SecurityControllerMixin {

  @Nullable
  default UserDetails getUserAccount() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return null;
    }
    final Object details = auth.getPrincipal();
    if (details instanceof UserDetails) {
      return (UserDetails) details;
    }
    throw new InsufficientAuthenticationException("Internal: unknown principal=" + details); // should not happen
  }

  default boolean hasUserAccount() {
    return getUserAccount() != null;
  }

  default long getUserId() {
    final UserDetails account = getUserAccount();
    final Long id = account != null ? UserIdRoleUtil.tryGetUserId(account.getAuthorities()) : null;
    if (id == null) {
      throw new InsufficientAuthenticationException("Internal: UserAccount has not been found or RoleId is not a " +
          "part of account=" + account);
    }
    return id;
  }

  default Map<String, Object> newMapWithAccount() {
    final Map<String, Object> result = new HashMap<>();
    result.put("userAccount", getUserAccount());
    return result;
  }
}
