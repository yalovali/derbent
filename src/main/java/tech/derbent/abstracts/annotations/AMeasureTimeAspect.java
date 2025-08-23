package tech.derbent.abstracts.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AMeasureTimeAspect {

	@Around ("@annotation(MeasureTime)")
	public Object measureExecutionTime(final ProceedingJoinPoint joinPoint)
		throws Throwable {
		final long start = System.nanoTime();
		final Object result = joinPoint.proceed(); // Execute the target method
		final long duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println(joinPoint.getSignature() + " executed in " + duration + " ms");
		return result;
	}
}
