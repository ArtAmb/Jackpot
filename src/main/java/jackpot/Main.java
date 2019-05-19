package jackpot;

import jackpot.entity.ExampleRepo;
import jackpot.entity.ExampleTable;
import jackpot.orm.JackpotRepositoryFactory;
import jackpot.orm.aop.JackpotOrmEnable;

public class Main {

    ExampleRepo exampleRepo = JackpotRepositoryFactory.createRepository(ExampleRepo.class);

    public void doSth() {
        ExampleTable[] res = exampleRepo.findByIntCol(10);

        System.out.println("res == " + res);
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
