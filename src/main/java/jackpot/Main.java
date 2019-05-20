package jackpot;

import com.google.gson.Gson;
import jackpot.entity.ExampleRepo;
import jackpot.entity.ExampleTable;
import jackpot.entity.TestTmpRepo;
import jackpot.entity.TestTmpTable;
import jackpot.orm.JackpotRepositoryFactory;
import jackpot.orm.aop.JackpotOrmEnable;

public class Main {

    ExampleRepo exampleRepo = JackpotRepositoryFactory.createRepository(ExampleRepo.class);
    TestTmpRepo testTmpRepo = JackpotRepositoryFactory.createRepository(TestTmpRepo.class);

    public void doSth() {
        ExampleTable table = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test")
                .testCol(741)
                .groupExampleTable(null)
                .build();
        ExampleTable table1 = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test1")
                .testCol(20)
                .groupExampleTable(null)
                .build();
        ExampleTable table2 = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test2")
                .testCol(15)
                .groupExampleTable(null)
                .build();

        exampleRepo.save(table);
        exampleRepo.save(table1);
        exampleRepo.save(table2);

        ExampleTable[] res = exampleRepo.findByIntCol(10);

        System.out.println("res == " + new Gson().toJson(res));

        TestTmpTable tmpTable = TestTmpTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test2")
                .testCol(15)
                .build();

        testTmpRepo.save(tmpTable);

        TestTmpTable[] res2 = testTmpRepo.findByTestCol(15);
        System.out.println("res2 == " + new Gson().toJson(res2));
        TestTmpTable toUpdate = res2[0];
        toUpdate.setStringCol("UPDATED !!!!!");
        testTmpRepo.save(toUpdate);
        TestTmpTable[] res3 = testTmpRepo.findByTestCol(15);
        System.out.println("res3 == " + new Gson().toJson(res3));
    }

    @JackpotOrmEnable
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS warehouses (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	capacity real\n"
                + ");";

//            Statement stmt = conn.createStatement();
//            stmt.execute(sql);


//        new jackpot.Main().jackpot();
        new Main().doSth();
    }

}
