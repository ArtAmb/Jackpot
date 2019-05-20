package jackpot.entity;

import jackpot.orm.repository.JackpotRepository;

public interface TestTmpRepo extends JackpotRepository<Integer, TestTmpTable> {

    TestTmpTable[] findByTestCol(Integer intCol);

}
