package jackpot.orm.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import jackpot.orm.JackpotOrmInitializer;

@Aspect
public class OrmAspect {

//    @Pointcut("@annotation(jackpotOrmEnable) && execution(@JackpotOrmEnable * *.*(..))")
//    public void callAt(JackpotOrmEnable jackpotOrmEnable){
//    }

    @Pointcut("@annotation(jackpotOrmEnable) && execution(@JackpotOrmEnable * *.*(..))")
    public void callBefore(JackpotOrmEnable jackpotOrmEnable){
    }


//    @Around("callAt(jackpotOrmEnable) ")
//    public Object around(ProceedingJoinPoint pjp, JackpotOrmEnable jackpotOrmEnable) throws Throwable {
//        System.out.println("AROUND  " + pjp.getKind());
//        return pjp.proceed();
//    }

    @Before("callBefore(jackpotOrmEnable)")
    public void jaxckpot(JackpotOrmEnable jackpotOrmEnable) throws Throwable {
        System.out.println("Before");

        JackpotOrmInitializer.getInstance().init();
    }

}
