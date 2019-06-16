package jackpot.orm.aop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jackpot.orm.JackpotDatabase;
import jackpot.orm.JackpotOrmInitializer;
import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.orm.repository.JackpotQueryExecutor;
import jackpot.orm.repository.TransactionPoolManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.FieldSignature;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;


@Aspect
public class OrmAspect {
    private JackpotQueryExecutor jackpotQueryExecutor = new JackpotQueryExecutor();

    @Pointcut("@annotation(transactional) && execution(@javax.transaction.Transactional * *.*(..))")
    public void aroundTransactional(Transactional transactional) {
    }

    @Around("aroundTransactional(transactional)")
    public Object aroundTransactional(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        try {
            System.out.println("TRANSACTION START !!!!!!!!!!!!!!!!!!!!!!!!!!");
            TransactionPoolManager.getInstance().createTransaction();
            return pjp.proceed();
        } catch (Throwable th) {
            System.out.println("TRANSACTION ROLLBACK !!!!!!!!!!!!!!!!!!!!!!!!!!");
            TransactionPoolManager.getConnection().rollback();
            throw th;
        } finally {
            TransactionPoolManager.getInstance().closeTransaction();
            System.out.println("TRANSACTION STOP !!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    @Pointcut("@annotation(jackpotOrmEnable) && execution(@JackpotOrmEnable * *.*(..))")
    public void callBefore(JackpotOrmEnable jackpotOrmEnable) {
    }

    @Before("callBefore(jackpotOrmEnable)")
    public void jackpot(JackpotOrmEnable jackpotOrmEnable) throws Throwable {
        System.out.println("Before");

        JackpotOrmInitializer.getInstance().init();
    }

    @Pointcut("@annotation(oneToMany) && get(@javax.persistence.OneToMany * *..*)")
    public void aroundOneToMany(OneToMany oneToMany) {
    }

    @Around("aroundOneToMany(oneToMany)")
    public Object lazyLoading(ProceedingJoinPoint pjp, OneToMany oneToMany) throws Throwable {
        System.out.println("LAZY LOADING START !!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("THIS == " + pjp.getThis().getClass().getName());
        Signature signature = pjp.getSignature();
        Field field = ((FieldSignature) signature).getField();
        Class<?> fieldClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        System.out.println(fieldClass);


        TableMetadata ownerTableMetadata = JackpotDatabase.getInstance().getTableByClassName(field.getDeclaringClass().getName());

        TableMetadata relatedTableMetadata = JackpotDatabase.getInstance().getTableByClassName(fieldClass.getName());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

//        System.out.println(gson.toJson(relatedTableMetadata.getColumns().stream()
//                .filter(columnMetadata -> columnMetadata.getForeignKeyRelation() != null).toArray()));

        ColumnMetadata relatedCol = relatedTableMetadata.getColumns().stream()
                .filter(columnMetadata -> columnMetadata.getForeignKeyRelation() != null)
                .filter(columnMetadata -> ownerTableMetadata.getTableName().equals(columnMetadata.getForeignKeyRelation().getTableName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no relation between  " + fieldClass.getName() + " and " + field.getDeclaringClass().getName()));

        String queryString = "findBy" + relatedCol.getColumnName();

        Field pkField = field.getDeclaringClass().getDeclaredField(ownerTableMetadata.getPrimaryKeyColumn().getFieldName());
        boolean accessible = pkField.isAccessible();
        pkField.setAccessible(true);
        Object pkValue = pkField.get(pjp.getThis());
        pkField.setAccessible(accessible);

        return Arrays.asList(jackpotQueryExecutor.execute(queryString, relatedTableMetadata.getClassName(), pkValue));
//        return pjp.proceed();
    }

}
