package liten.website.service.aspect;

import com.truward.semantic.id.exception.IdParsingException;
import liten.website.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Alexander Shabanov
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
    } catch (RuntimeException e) {

      // Don't let IdParsingException cause 500, it rather indicates that ID is misspelled
      if (e instanceof IdParsingException) {
        throw new ResourceNotFoundException(e);
      }

      // Throw exception as is
      throw e;
    }
  }
}
