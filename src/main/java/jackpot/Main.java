package jackpot;

import jackpot.orm.aop.JackpotOrmEnable;

public class Main {

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

    }

}
