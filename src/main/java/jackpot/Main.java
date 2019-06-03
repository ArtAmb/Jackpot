package jackpot;

import com.google.gson.Gson;
import jackpot.entity.*;
import jackpot.orm.JackpotRepositoryFactory;
import jackpot.orm.aop.JackpotOrmEnable;

public class Main {

    ExampleRepo exampleRepo = JackpotRepositoryFactory.createRepository(ExampleRepo.class);
    TestTmpRepo testTmpRepo = JackpotRepositoryFactory.createRepository(TestTmpRepo.class);
    GroupExampleTableRepo groupExampleTableRepo = JackpotRepositoryFactory.createRepository(GroupExampleTableRepo.class);


    public void doSth() {

        GroupExampleTable groupExampleTable = null;
//        groupExampleTableRepo.save(GroupExampleTable.builder()
//                .intCol(741)
//                .testStr("TEST")
//                .build());

        ExampleTable table = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test")
                .testCol(741)
                .groupExampleTable(groupExampleTable)
                .build();
        ExampleTable table1 = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test1")
                .testCol(20)
                .groupExampleTable(groupExampleTable)
                .build();
        ExampleTable table2 = ExampleTable.builder()
                .id(0)
                .intCol(10)
                .stringCol("Test2")
                .testCol(15)
                .groupExampleTable(groupExampleTable)
                .build();

        table = exampleRepo.save(table);
        table1 = exampleRepo.save(table1);
        exampleRepo.save(table2);

        ExampleTable findOneRes = exampleRepo.findOne(table1.getId(), ExampleTable.builder().build());

        System.out.println("findOne == " + new Gson().toJson(findOneRes));

        ExampleTable[] res = exampleRepo.findByIntCol(10);

        System.out.println("res == " + new Gson().toJson(res));
        exampleRepo.delete(table.getId(), ExampleTable.builder().build());

        res = exampleRepo.findByIntCol(10);
        System.out.println("AFTER DELETE res == " + new Gson().toJson(res));

        exampleRepo.deleteByIntCol(10);
        res = exampleRepo.findByIntCol(10);
        System.out.println("AFTER DELETE ALL == " + new Gson().toJson(res));


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

        System.out.println("\n\n######################################\n\n");

        if(groupExampleTable != null ) {
            GroupExampleTable groupExampleTable2 = groupExampleTableRepo.findOne(groupExampleTable.getId(), GroupExampleTable.builder().build());
            System.out.println("groupExampleTable == " + new Gson().toJson(groupExampleTable2));
        }
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
