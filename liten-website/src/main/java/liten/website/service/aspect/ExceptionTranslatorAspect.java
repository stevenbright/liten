package liten.website.service.aspect;

import com.truward.dao.exception.ItemNotFoundException;
import com.truward.semantic.id.exception.IdParsingException;
import liten.website.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * An aspect, that translates business logic-level exceptions into HTTP exceptions.
 */
@Aspect
public final class ExceptionTranslatorAspect {

  @Pointcut("@within(org.springframework.stereotype.Service)")
  public void withinService() {}

  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  @Around("publicMethod() && withinService()")
  public Object around(ProceedingJoinPoint jp) throws Throwable {
    try {
      return jp.proceed();
    } catch (IdParsingException | ItemNotFoundException e) {
      // all those exceptions are equivalents of 404:
      // IdParsingException - indicates that ID is misspelled
      // ItemNotFoundException - indicates that though ID seems valid, corresponding item is nowhere to be found
      throw new ResourceNotFoundException(e);
    }
  }
}
