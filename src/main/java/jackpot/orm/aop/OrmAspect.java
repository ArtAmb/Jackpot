package jackpot.orm.aop;

import jackpot.orm.JackpotOrmInitializer;
import jackpot.orm.repository.TransactionPoolManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import javax.transaction.Transactional;


@Aspect
public class OrmAspect {

//    @Pointcut("@annotation(jackpotOrmEnable) && execution(@JackpotOrmEnable * *.*(..))")
//    public void callAt(JackpotOrmEnable jackpotOrmEnable){
//    }

    @Pointcut("@annotation(jackpotOrmEnable) && execution(@JackpotOrmEnable * *.*(..))")
    public void callBefore(JackpotOrmEnable jackpotOrmEnable) {
    }

    @Pointcut("@annotation(transactional) && execution(@javax.transaction.Transactional * *.*(..))")
    public void aroundTransactional(Transactional transactional) {
    }

    @Around("aroundTransactional(transactional)")
    public Object aroundTransactional(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        try {
            System.out.println("TRANSACTION START !!!!!!!!!!!!!!!!!!!!!!!!!!");
            TransactionPoolManager.getInstance().createTransaction();
            return pjp.proceed();
        }catch (Throwable th) {
            System.out.println("TRANSACTION ROLLBACK !!!!!!!!!!!!!!!!!!!!!!!!!!");
            TransactionPoolManager.getConnection().rollback();
            throw th;
        } finally {
            TransactionPoolManager.getInstance().closeTransaction();
            System.out.println("TRANSACTION STOP !!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }


//    @Around("callAt(jackpotOrmEnable) ")
//    public Object around(ProceedingJoinPoint pjp, JackpotOrmEnable jackpotOrmEnable) throws Throwable {
//        System.out.println("AROUND  " + pjp.getKind());
//        return pjp.proceed();
//    }

    @Before("callBefore(jackpotOrmEnable)")
    public void jackpot(JackpotOrmEnable jackpotOrmEnable) throws Throwable {
        System.out.println("Before");

        JackpotOrmInitializer.getInstance().init();
    }


//    @Pointcut("@annotation(transactional) && execution(@JackpotTransactional * *.*(..))")
//    public void aroundTransactional(JackpotTransactional transactional) {
//    }
//
//    @Around("aroundTransactional(transactional)")
//    public Object callAroundTransactional(ProceedingJoinPoint pjp, JackpotOrmEnable jackpotOrmEnable) throws Throwable {
//        System.out.println("AROUND  " + pjp.getKind());
//        try {
//            System.out.println("TRANSACTION START !!!!!!!!!!!!!!!!!!!!!!!!!!");
//            return pjp.proceed();
//        } finally {
//            System.out.println("TRANSACTION STOP !!!!!!!!!!!!!!!!!!!!!!!!!!");
//        }
//    }

}
