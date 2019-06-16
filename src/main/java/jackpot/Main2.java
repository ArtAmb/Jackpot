package jackpot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jackpot.entity.*;
import jackpot.orm.JackpotRepositoryFactory;
import jackpot.orm.aop.JackpotOrmEnable;

import javax.transaction.Transactional;

public class Main2 {

    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    ExampleRepo exampleRepo = JackpotRepositoryFactory.createRepository(ExampleRepo.class);
    TestTmpRepo testTmpRepo = JackpotRepositoryFactory.createRepository(TestTmpRepo.class);
    GroupExampleTableRepo groupExampleTableRepo = JackpotRepositoryFactory.createRepository(GroupExampleTableRepo.class);
    ExampleManySameFKTableRepo exampleManySameFKTableRepo = JackpotRepositoryFactory.createRepository(ExampleManySameFKTableRepo.class);
    NestedTableRepo nestedTableRepo = JackpotRepositoryFactory.createRepository(NestedTableRepo.class);


    void doSth() {
        GroupExampleTable tmp = GroupExampleTable.builder().build();
        tmp.getTables();
//
//        try {
//            doSth1();
//        } catch (Throwable tx) {
//            ExampleTable[] res = exampleRepo.findByIntCol(10);
//            System.out.println("AFTER EXCEPTION EXCEPTION -> " + prettyGson.toJson(res));
//        }
    }

    @Transactional
    void doSth1() {
        exampleRepo.save(ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test2")
                .testCol(15)
                .groupExampleTable(null)
                .build());

        ExampleTable[] res = exampleRepo.findByIntCol(10);

        System.out.println("BEFORE EXCEPTION -> " + prettyGson.toJson(res));
        throw new IllegalStateException();
    }


    @JackpotOrmEnable
    static public void main(String[] args) {
        new Main2().doSth();
    }
}
