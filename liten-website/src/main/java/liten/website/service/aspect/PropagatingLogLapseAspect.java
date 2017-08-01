package liten.website.service.aspect;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.log.metric.MetricsCollection;
import com.truward.brikar.common.time.TimeSource;
import com.truward.brikar.common.time.support.StandardTimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * Custom handler for {@link com.truward.brikar.common.log.LogLapse} aspects.
 * TODO: move to truward brikar.
 */
@Aspect
public final class PropagatingLogLapseAspect {
  private TimeSource timeSource;

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  @PostConstruct
  public void init() {
    if (timeSource == null) {
      setTimeSource(StandardTimeSource.INSTANCE);
    }
  }

  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  @Pointcut("@within(org.springframework.stereotype.Service)")
  public void withinService() {}

  @Pointcut("@within(org.springframework.stereotype.Repository)")
  public void withinRepository() {}

  @Around("publicMethod() && (withinService() || withinRepository()) && @annotation(logLapse)")
  public Object around(ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    final MetricsCollection metricsCollection = LogUtil.getLocalMetricsCollection();
    if (metricsCollection == null) {
      return jp.proceed();
    }

    return around(metricsCollection, jp, logLapse);
  }

  //
  // Private
  //

  private Object around(
      MetricsCollection metricsCollection,
      ProceedingJoinPoint jp,
      LogLapse logLapse) throws Throwable {
    final SimpleLapse lapse = new SimpleLapse();

    lapse.setStartTime(getTimeSource());

    String place = logLapse.value();
    if (!StringUtils.hasLength(place)) {
      // no text in annotation value - fallback to signature name
      place = getPlaceFromJoinPoint(jp);
    }
    lapse.setOperation(place);

    try {
      final Object result = jp.proceed();

      lapse.setEndTime(getTimeSource());
      lapse.setFailed(false);

      return result;
    } catch (Exception e) {
      // record end of call time and write lapse
      lapse.setEndTime(getTimeSource());
      lapse.setFailed(true);

      throw e;
    } finally {
      metricsCollection.add(lapse);
    }
  }

  private TimeSource getTimeSource() {
    return timeSource;
  }

  private static String getPlaceFromJoinPoint(ProceedingJoinPoint jp) {
    final Signature signature = jp.getSignature();
    if (signature instanceof MethodSignature) {
      final Method method = ((MethodSignature) signature).getMethod();
      return method.getDeclaringClass().getSimpleName() + '.' + method.getName();
    }

    // fallback to generic name
    return jp.getSignature().getName();
  }
}
